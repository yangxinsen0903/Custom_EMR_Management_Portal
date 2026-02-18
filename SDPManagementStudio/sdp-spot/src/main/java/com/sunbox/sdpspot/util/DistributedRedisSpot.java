package com.sunbox.sdpspot.util;

import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class DistributedRedisSpot {
    private static final Logger logger = LoggerFactory.getLogger(DistributedRedisSpot.class);

    private RedissonClient redissonClient;

    public DistributedRedisSpot(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    //lock(), 拿不到lock就不罢休，不然线程就一直block
    public RLock lock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        return lock;
    }

    //leaseTime为加锁时间，单位为秒
    public RLock lock(String lockKey, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, TimeUnit.SECONDS);
        return lock;
    }

    //timeout为加锁时间，时间单位由unit确定

    public RLock lock(String lockKey, TimeUnit unit, long timeout) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(timeout, unit);
        return lock;
    }

    //带时间限制的tryLock()，拿不到lock，就等一段时间，超时返回false.
    public boolean tryLock(String lockKey, TimeUnit unit, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    //tryLock()，马上返回，拿到lock就返回true，不然返回false。
    public boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock();
        } catch (Exception e) {
            return false;
        }
    }


    public void unlock(String lockKey) {
        try {
            RLock lock = redissonClient.getLock(lockKey);
            if (lock.isLocked()) {
                lock.unlock();
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    //判断要解锁的key是否已被锁定
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    //判断要解锁的key是否被当前线程持有
    public boolean isHeldByCurrentThread(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }

    public void unlock(RLock lock) {
        lock.unlock();
    }


    public String getValue(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        if (bucket.get() != null) {
            return bucket.get().toString();
        } else {
            return null;
        }
    }

    public String getValueAsString(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key, new StringCodec());
        Object getValue = bucket.get();
        if (getValue != null) {
            return getValue.toString();
        } else {
            return null;
        }
    }

    public String tryGetValueAsString(String key, String defaultValue) {
        try {
            RBucket<Object> bucket = redissonClient.getBucket(key, new StringCodec());
            Object getValue = bucket.get();
            if (getValue != null) {
                return getValue.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public void expire(String key, long time, TimeUnit timeUnit) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.expire(time, timeUnit);
    }

    public void save(String key, String value) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    public void save(String key, String value, long expire) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);
        bucket.expire(expire, TimeUnit.SECONDS);
    }

    public void saveString(String key, String value, long expire) {
        RBucket<Object> bucket = redissonClient.getBucket(key, new StringCodec());
        bucket.set(value);
        bucket.expire(expire, TimeUnit.SECONDS);
    }

    public void trySaveString(String key, String value, long expire) {
        try {
            RBucket<Object> bucket = redissonClient.getBucket(key, new StringCodec());
            bucket.set(value);
            bucket.expire(expire, TimeUnit.SECONDS);
        } catch (Exception ignore) {

        }
    }

    public void save(String key, String value, long expire, TimeUnit timeUnit) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);
        bucket.expire(expire, timeUnit);
    }

    public void saveExpireAt(String key, String value, Date expireTime) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);
        bucket.expire(Instant.ofEpochMilli(expireTime.getTime()));
    }

    public void delete(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.delete();
    }

    public void addList(String key, String value) {
        RList<String> rList = redissonClient.getList(key);
        rList.add(value);
    }

    public List<String> getList(String key) {
        RList<String> rList = redissonClient.getList(key);
        return rList.readAll();
    }

    public int listSize(String key) {
        RList<String> rList = redissonClient.getList(key);
        return rList.size();
    }

    public List<String> getContainValueList(String key, String value) {
        List<String> rList = getList(key);
        List<String> containValueList = new ArrayList<>();
        for (String s : rList) {
            if (s.contains(value)) {
                containValueList.add(s);
            }
        }
        return containValueList;
    }

    public Integer listContainValue(String key, String value) {
        List<String> rList = getList(key);
        for (int i = 0; i < rList.size(); i++) {
            String s = rList.get(i);
            if (s.contains(value)) {
                return i;
            }
        }
        return null;
    }

    public void listSetValue(String key, String oldValue, String newValue) {
        RList<String> rList = redissonClient.getList(key);
        List<String> strings = rList.readAll();
        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            if (Objects.equals(s, oldValue)) {
                rList.set(i, newValue);
                break;
            }
        }
    }

    public void listSetValue(String key, Integer index, String newValue) {
        RList<String> rList = redissonClient.getList(key);
        rList.set(index, newValue);
    }

    public String listGetValue(String key, Integer index) {
        RList<String> rList = redissonClient.getList(key);
        return rList.get(index);
    }

    public void removeValueFromList(String key, String value) {
        RList<String> rList = redissonClient.getList(key);
        rList.remove(value);
    }

    public void removeValueFromListByKeys(String key, String... value) {
        RList<String> rList = redissonClient.getList(key);
        Iterator<String> iterator = rList.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            List<Boolean> contains = new ArrayList<>();
            for (String s : value) {
                contains.add(next.contains(s));
            }
            Boolean reduce = contains.stream().reduce(Boolean.TRUE, Boolean::logicalAnd);
            if (reduce) {
                iterator.remove();
            }
        }
    }

    public Boolean addBlockingQueue(String key, String value) {
        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(key);
        return blockingQueue.add(value);
    }

    public RBlockingQueue<String> getBlockingQueue(String key) {
        return redissonClient.getBlockingQueue(key);
    }

    public Long getAndIncrement(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.getAndIncrement();
    }

    public boolean haveKey(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.isExists();
    }

    public void mapSetValue(String key, String itemKey, String itemValue, long expire, TimeUnit timeUnit) {
        RMap<String, String> map = redissonClient.getMap(key);
        map.put(itemKey, itemValue);
        map.expire(expire, timeUnit);
    }

    public Map<String, String> mapGet(String key) {
        RMap<String, String> map = redissonClient.getMap(key, new CompositeCodec(new StringCodec(), new StringCodec()));
        return map.readAllMap();
    }

    public void mapRemove(String key, String itemKey) {
        RMap<String, String> map = redissonClient.getMap(key);
        map.remove(itemKey);
    }
}
