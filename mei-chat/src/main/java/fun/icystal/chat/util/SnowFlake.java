package fun.icystal.chat.util;

public class SnowFlake {
    // 基准时间戳
    private static final long START_TIMESTAMP = 1577836800000L;
    // 各部分位数配置
    private static final long DATA_CENTER_BITS = 5L;
    private static final long WORKER_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    // 最大值计算
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    // 左移位数
    private static final long WORKER_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_SHIFT = SEQUENCE_BITS + WORKER_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_BITS + DATA_CENTER_BITS;

    private final long dataCenterId;

    private final long workerId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    public SnowFlake(long dataCenterId, long workerId) {
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("DataCenter 超出范围");
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker 超出范围");
        }
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();
        // 时钟回拨检查
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("雪花ID时钟异常");
        }
        // 同一毫秒内序列号递增
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) { // 当前毫秒序列号耗尽
                currentTimestamp = waitNextMillis();
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = currentTimestamp;
        // 组合各部分生成ID
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_SHIFT)
                | (workerId << WORKER_SHIFT)
                | sequence;
    }

    private long waitNextMillis() {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    private static final SnowFlake snowFlake =  new SnowFlake(0, 0);

    public static long id() {
        return snowFlake.nextId();
    }
}

