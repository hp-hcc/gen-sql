# GenSQL

GenSQL 是一个轻量级的 Spring Boot SQL 生成工具，用于根据 JSON 输入快速生成 `INSERT` 和 `UPDATE` SQL 语句。

它适合作为一个可发布的 Docker 工具使用，开箱即用，适合前端、测试、后端以及内部工具平台直接调用。

## GenSQL 可以做什么

- 直接通过 `data` JSON 对象生成 SQL
- 通过 `whereData` 快速生成 `UPDATE` 条件
- 通过结构化 `whereConditions` 构造更复杂的条件
- 自动把 `camelCase` 字段名转换成 `snake_case`
- 内置基础 SQL 注入防护
- 完整支持中文输入和中文 SQL 生成
- 提供健康检查和在线接口文档

## 适合的使用场景

- 前端页面根据表单直接生成 SQL
- 测试数据准备与初始化脚本辅助生成
- 数据修复场景快速生成 `UPDATE` 语句
- 内部平台封装成一个统一 SQL 工具服务
- 作为本地 Docker 工具供团队成员直接使用

## 快速开始

### 1. 使用 Docker 构建 GenSQL

在项目根目录执行：

```bash
docker build -t gensql:latest .
```

### 2. 使用 Docker 运行 GenSQL

```bash
docker run --name gensql -p 8111:8111 gensql:latest
```

启动后可访问：

- 服务地址：`http://localhost:8111`
- 健康检查：`http://localhost:8111/api/v1/health`
- 接口文档：`http://localhost:8111/doc.html`

### 3. 使用 Docker Compose 运行

```bash
docker compose up -d --build
```

## GenSQL 接口示例

### 示例 1：直接生成 INSERT

请求地址：

```text
POST /api/v1/sql/generate
```

请求参数：

```json
{
  "tableName": "user",
  "sqlType": "insert",
  "schema": "mydb",
  "data": {
    "userId": 10001,
    "userName": "张三",
    "orderNo": "ORD202603270001"
  }
}
```

GenSQL 生成结果示例：

```sql
INSERT INTO `mydb`.`user` (`user_id`, `user_name`, `order_no`)
VALUES (10001, '张三', 'ORD202603270001');
```

### 示例 2：直接生成 UPDATE

请求参数：

```json
{
  "tableName": "user",
  "sqlType": "update",
  "schema": "mydb",
  "data": {
    "userName": "李四",
    "updatedAt": "2024-01-01 12:00:00"
  },
  "whereData": {
    "userId": 1
  }
}
```

GenSQL 生成结果示例：

```sql
UPDATE `mydb`.`user`
SET `user_name` = '李四', `updated_at` = '2024-01-01 12:00:00'
WHERE `user_id` = 1;
```

### 示例 3：高级 UPDATE 条件

请求参数：

```json
{
  "tableName": "user",
  "sqlType": "update",
  "data": {
    "status": "DONE"
  },
  "whereConditions": [
    {
      "fieldName": "tenantId",
      "operator": "eq",
      "value": 1001
    },
    {
      "fieldName": "status",
      "operator": "in",
      "values": ["INIT", "READY"]
    }
  ]
}
```

## 字段命名兼容规则

GenSQL 默认开启驼峰转下划线：

- `userId -> user_id`
- `userName -> user_name`
- `updatedAt -> updated_at`
- `orderNo -> order_no`

如果你希望关闭自动转换，可以传：

```json
{
  "camelToSnake": false
}
```

例如：

```json
{
  "tableName": "user",
  "sqlType": "insert",
  "camelToSnake": false,
  "data": {
    "user_name": "王五"
  }
}
```

## whereConditions 支持的操作符

当前支持：

- `eq`
- `ne`
- `gt`
- `gte`
- `lt`
- `lte`
- `like`
- `in`
- `not_in`
- `is_null`
- `is_not_null`

## 将 GenSQL 发布为 Docker 工具

如果你想把 GenSQL 发布给其他人使用，推荐发布到 Docker Hub。

### 1. 构建镜像

```bash
docker build -t gensql:latest .
```

### 2. 给镜像打标签

把 `YOUR_DOCKERHUB_NAME` 替换成你的 Docker Hub 用户名：

```bash
docker tag gensql:latest YOUR_DOCKERHUB_NAME/gensql:latest
```

### 3. 登录 Docker Hub

```bash
docker login
```

### 4. 推送镜像

```bash
docker push YOUR_DOCKERHUB_NAME/gensql:latest
```

发布后，其他人可以直接运行：

```bash
docker run -p 8111:8111 YOUR_DOCKERHUB_NAME/gensql:latest
```

### 多实例部署时的 Snowflake 配置

如果你部署多个实例，请务必为每个实例分配不同的 `workerId`，避免雪花 ID 冲突。

示例 1：

```bash
docker run -p 8111:8111 \
  -e GENSQL_SNOWFLAKE_WORKER_ID=1 \
  -e GENSQL_SNOWFLAKE_DATACENTER_ID=1 \
  YOUR_DOCKERHUB_NAME/gensql:latest
```

示例 2：

```bash
docker run -p 8112:8111 \
  -e GENSQL_SNOWFLAKE_WORKER_ID=2 \
  -e GENSQL_SNOWFLAKE_DATACENTER_ID=1 \
  YOUR_DOCKERHUB_NAME/gensql:latest
```

启动日志中会打印当前实例使用的 Snowflake 配置，便于排查是否重复：

```text
GenSQL Snowflake initialized with workerId=1, datacenterId=1, maxBackwardMillis=5
```

## GenSQL 项目说明

- 服务端口：`8111`
- 健康检查：`/api/v1/health`
- 接口文档：`/doc.html`
- 默认开启驼峰转下划线
- `UPDATE` 请求必须提供 `whereData` 或 `whereConditions`
- 如果需要雪花 ID、流水号等自动生成能力，请继续使用 `fields` 模式
- 多实例部署时请为不同实例分配不同的 Snowflake `workerId`

## 适合作为团队工具的原因

GenSQL 适合做成团队内部共享工具，因为它：

- 部署轻
- 调用简单
- 不依赖数据库连接
- 不依赖表结构元数据
- 适合快速生成标准 SQL
- 支持 Docker 分发，团队成员一条命令即可启动
