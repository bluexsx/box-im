/* global BigInt */
/**
 * 与 MyBatis-Plus IdWorker.getIdStr() 同源：com.baomidou.mybatisplus.core.toolkit.Sequence#nextId
 * 输出为 64 位雪花 long 的十进制字符串（非分段拼接）。
 * @see https://github.com/baomidou/mybatis-plus/blob/v3.5.7/mybatis-plus-core/src/main/java/com/baomidou/mybatisplus/core/toolkit/Sequence.java
 */

const TW_EPOCH = 1288834974657n;
const WORKER_ID_BITS = 5n;
const DATACENTER_ID_BITS = 5n;
const SEQUENCE_BITS = 12n;
const WORKER_ID_SHIFT = SEQUENCE_BITS;
const DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
const TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
const SEQUENCE_MASK = (1n << SEQUENCE_BITS) - 1n;

const workerId = BigInt(Math.floor(Math.random() * 32));
const datacenterId = BigInt(Math.floor(Math.random() * 32));

let lastTs = -1n;
let sequence = 0n;

function nextSeqOnNewMillis() {
  // ThreadLocalRandom.current().nextLong(1, 3)
  return BigInt(Math.floor(Math.random() * 2) + 1);
}

const tilNextMillis = (lastTimestamp: bigint) => {
  let ts = BigInt(Date.now());
  while (ts <= lastTimestamp) {
    ts = BigInt(Date.now());
  }
  return ts;
};

/**
 * 与后端一致使用当前时间（SystemClock 等效为 currentTimeMillis）
 */
export default function nextSnowflakeId() {
  let timestamp = BigInt(Date.now());
  if (timestamp < lastTs) {
    const offset = lastTs - timestamp;
    if (offset > 5n) {
      throw new Error(`Clock moved backwards. Refusing to generate id for ${offset} milliseconds`);
    }
    const until = Date.now() + Number(offset << 1n);
    while (BigInt(Date.now()) < lastTs && Date.now() < until) {
      /* align with Sequence#nextId 闰秒分支 */
    }
    timestamp = BigInt(Date.now());
    if (timestamp < lastTs) {
      throw new Error(`Clock moved backwards. Refusing to generate id for ${offset} milliseconds`);
    }
  }

  if (lastTs === timestamp) {
    sequence = (sequence + 1n) & SEQUENCE_MASK;
    if (sequence === 0n) {
      timestamp = tilNextMillis(lastTs);
    }
  } else {
    sequence = nextSeqOnNewMillis();
  }
  lastTs = timestamp;

  const id = ((timestamp - TW_EPOCH) << TIMESTAMP_LEFT_SHIFT) | (datacenterId << DATACENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
  return id.toString();
}
