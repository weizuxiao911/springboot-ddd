package io.github.weizuxiao911.springboot.ddd.common.id;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 雪花算法 ID 生成器
 * 基于 IP 地址自动计算机器 ID，确保多实例部署时 ID 唯一。
 */
public class ID {

    private static final long START_TIMESTAMP = 1288834974657L;

    private static final long SEQUENCE_BIT = 12;
    private static final long MACHINE_BIT = 5;
    private static final long DATACENTER_BIT = 5;

    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private static final long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);

    private static final long MACHINE_LEFT = SEQUENCE_BIT;
    private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private final long datacenterId;
    private final long machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static class Holder {
        private static final ID INSTANCE = new ID();
    }

    /**
     * 私有构造函数，基于 IP 自动计算 ID
     */
    private ID() {
        long ipLong = getLocalIpLong();
        // 取 IP 的低 10 位作为机器标识
        // 高 5 位作为 datacenterId，低 5 位作为 machineId
        this.datacenterId = (ipLong >> 8) & MAX_DATACENTER_NUM;
        this.machineId = ipLong & MAX_MACHINE_NUM;
    }

    /**
     * 获取指定参数的实例（用于测试）
     */
    public ID(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than " + MAX_DATACENTER_NUM + " or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    public static ID getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 生成唯一 Long 类型 ID
     */
    public synchronized Long generate() {
        long timestamp = timeGen();
        
        // 处理时钟回拨
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                // 如果回拨很小（5ms 内），等待补偿
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException("Clock moved backwards, refusing to generate id");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("Clock moved backwards, interrupted", e);
                }
            } else {
                throw new RuntimeException("Clock moved backwards, refusing to generate id for " + offset + " milliseconds");
            }
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT)
                | (datacenterId << DATACENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
    }

    /**
     * 生成指定长度的短 ID (数字+字母组合)
     * 基于 ID 的唯一性，通过 Hash 混淆后编码为 Base62 字符串。
     *
     * @param length 生成的字符串长度
     * @return 短 ID 字符串
     */
    public synchronized String generate(int length) {
        Long id = generate();
        long hash = murmurHash3(id);
        return encodeBase62(hash, length);
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 获取本机 IP 并转换为 long
     */
    private long getLocalIpLong() {
        try {
            InetAddress ip = getLocalInetAddress();
            if (ip != null) {
                byte[] addr = ip.getAddress();
                if (addr.length == 4) { // IPv4
                    long result = 0;
                    for (byte b : addr) {
                        result = (result << 8) | (b & 0xFF);
                    }
                    return result;
                }
            }
        } catch (Exception e) {
            // 忽略异常，使用默认值
        }
        return 1L;
    }

    /**
     * 获取本机 IP 地址
     */
    private InetAddress getLocalInetAddress() throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : java.util.Collections.list(nets)) {
            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : java.util.Collections.list(inetAddresses)) {
                if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(':') < 0) {
                    return inetAddress;
                }
            }
        }
        try {
            return InetAddress.getLocalHost();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * MurmurHash3 最终混淆步骤
     */
    private static long murmurHash3(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 将长整型编码为指定长度的 Base62 字符串
     */
    private static String encodeBase62(long number, int length) {
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            int index = (int) (number % 62);
            if (index < 0) index = -index;
            buf[i] = BASE62.charAt(index);
            number /= 62;
        }
        return new String(buf);
    }
}
