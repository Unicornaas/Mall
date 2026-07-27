package edu.fjut.mall.common.util;

/**
 * 雪花算法 ID 生成器（分布式全局唯一ID）
 *
 * <pre>
 * 结构: 1bit(不用) | 41bit(时间戳) | 5bit(机房) | 5bit(机器) | 12bit(序列号)
 * </pre>
 */
public class SnowflakeIdGenerator {

    /** 起始时间戳 (2024-01-01 00:00:00) */
    private static final long START_TIMESTAMP = 1704067200000L;

    /** 每一部分占用的位数 */
    private static final long DATA_CENTER_ID_BITS = 5L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    /** 每一部分的最大值 */
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);   // 31
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);             // 31
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);               // 4095

    /** 每一部分向左的位移 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;                           // 12
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;     // 17
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;  // 22

    private final long dataCenterId;
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long dataCenterId, long workerId) {
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("dataCenterId 超出范围 [0, " + MAX_DATA_CENTER_ID + "]");
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId 超出范围 [0, " + MAX_WORKER_ID + "]");
        }
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    /**
     * 生成下一个 ID（线程安全）
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();

        // 时钟回拨处理：如果当前时间小于上次生成时间，说明时钟回拨了
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID。差值: " + (lastTimestamp - currentTimestamp) + "ms");
        }

        // 同一毫秒内，序列号递增
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 同一毫秒内序列号用完，等待下一毫秒
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 等待到下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
