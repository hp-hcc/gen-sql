package com.gensql.generator.generator;

import com.gensql.generator.model.dto.FieldConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeGenerator implements ValueGenerator {
    private static final Logger log = LoggerFactory.getLogger(SnowflakeGenerator.class);

    private static final long TW_EPOCH = 1288834974657L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_BITS = 12L;

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private final long workerId;
    private final long datacenterId;
    private final long maxBackwardMillis;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeGenerator(
            @Value("${gensql.snowflake.worker-id:1}") long workerId,
            @Value("${gensql.snowflake.datacenter-id:1}") long datacenterId,
            @Value("${gensql.snowflake.max-backward-millis:5}") long maxBackwardMillis) {
        validateIdRange(workerId, MAX_WORKER_ID, "worker");
        validateIdRange(datacenterId, MAX_DATACENTER_ID, "datacenter");
        if (maxBackwardMillis < 0) {
            throw new IllegalArgumentException("max backward millis cannot be negative");
        }

        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.maxBackwardMillis = maxBackwardMillis;

        log.info("GenSQL Snowflake initialized with workerId={}, datacenterId={}, maxBackwardMillis={}",
                this.workerId, this.datacenterId, this.maxBackwardMillis);
    }

    @Override
    public synchronized Long generate(FieldConfig config) {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            timestamp = handleClockBackward(timestamp);
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - TW_EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long handleClockBackward(long currentTimestamp) {
        long offset = lastTimestamp - currentTimestamp;
        if (offset > maxBackwardMillis) {
            throw new IllegalStateException("Clock moved backwards by " + offset + " ms");
        }

        try {
            Thread.sleep(offset);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for clock recovery", e);
        }

        long recoveredTimestamp = timeGen();
        if (recoveredTimestamp < lastTimestamp) {
            throw new IllegalStateException("Clock did not recover after waiting " + offset + " ms");
        }
        return recoveredTimestamp;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    private void validateIdRange(long value, long maxValue, String name) {
        if (value < 0 || value > maxValue) {
            throw new IllegalArgumentException(
                    String.format("%s id can't be greater than %d or less than 0", name, maxValue));
        }
    }
}
