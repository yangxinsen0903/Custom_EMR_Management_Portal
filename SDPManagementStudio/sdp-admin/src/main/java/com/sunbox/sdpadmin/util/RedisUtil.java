package com.sunbox.sdpadmin.util;

import com.sunbox.util.DESUtil;
import com.sunbox.util.RedisCommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;
import redis.clients.jedis.params.geo.GeoRadiusParam;

import java.util.*;

@Component
@ConfigurationProperties("mredis.config")
public class RedisUtil extends RedisCommonUtil {

    @Value("${mredis.lock.address}")
    private String redisLcokAddress;

    @Value("${mredis.lock.port}")
    private String port;

    @Value("${mredis.lock.password}")
    private String redisLockPassword;

    private JedisPool jedisPool = null;

    /**
     * 初始化Redis连接池
     */
    private void initialPool() {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(1000);
            config.setMaxIdle(50);
            config.setMaxWaitMillis(2000);
            config.setMinIdle(200);
            config.setTestOnBorrow(true);
            config.setLifo(true);
            //config.setMinEvictableIdleTimeMillis(1000*1800);
            config.setTestOnReturn(true);
            config.setTestWhileIdle(true);
            config.setJmxNamePrefix("pool");

            jedisPool = new JedisPool(config, DESUtil.decrypt(this.redisLcokAddress),
                    Integer.valueOf(DESUtil.decrypt(port)),
                    3000,
                    DESUtil.decrypt(this.redisLockPassword));
        } catch (Exception e) {
            /*try{
                //如果第一个IP异常，则访问第二个IP
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(MAX_ACTIVE);
                config.setMaxIdle(MAX_IDLE);
                config.setMaxWaitMillis(MAX_WAIT);
                config.setTestOnBorrow(TEST_ON_BORROW);
                jedisPool = new JedisPool(config, ADDR_ARRAY.split(",")[1], PORT, TIMEOUT,AUTH);
            }catch(Exception e2){
                logger.error("Second create JedisPool error : "+e2);
            }*/
        }
    }


    /**
     * 在多线程环境同步初始化
     */
    private synchronized void poolInit() {
        if (jedisPool == null) {
            initialPool();
        }
    }


    /**
     * 同步获取Jedis实例
     *
     * @return Jedis
     */
    public Jedis getJedis() {
        if (jedisPool == null) {
            poolInit();
        }
        Jedis jedis = null;
        try {
            if (jedisPool != null) {
                jedis = jedisPool.getResource();
            }
        } catch (Exception e) {
        }/*finally {
            returnResource(jedis);
        }*/
        return jedis;
    }


    /**
     * 释放jedis资源
     *
     * @param jedis
     */
    public void returnResource(final Jedis jedis, boolean flag) {
        try {
            if (jedis != null && jedisPool != null && flag) {
                //jedisPool.returnResource(jedis);
                jedis.close();
            }
            if (jedis != null && jedisPool != null && !flag) {
                //jedisPool.returnBrokenResource(jedis);
                jedis.close();
            }
        } catch (Exception e) {
        }
    }

    /**
     * 设置 String
     *
     * @param key
     * @param value
     */
    public void save(String key, String value) {

        if (!chkkey(key)) {
            return;
        }

        Jedis jedis = null;
        try {
            jedis = getJedis();
            value = StringUtils.isEmpty(value) ? "" : value;
            jedis.set(key, value);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
    }

    /**
     * 设置 过期时间
     *
     * @param key
     * @param seconds 以秒为单位
     * @param value
     */
    public void save(String key, String value, int seconds) {
        if (!chkkey(key)) {
            return;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            value = StringUtils.isEmpty(value) ? "" : value;
            jedis.setex(key, seconds, value);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
    }

    public void delete(String... keys) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.del(keys);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
    }

    public void sadd(String key, String... value) {
        if (!chkkey(key)) {
            return;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.sadd(key, value);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
    }

    //删除集合中的某个值
    public Long srem(String key, String[] members) {
        if (!chkkey(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis.exists(key)) {
                return jedis.srem(key, members);
            } else {
                return null;
            }
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }


    public Set<String> getSet(String key) {
        if (!chkkey(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis.exists(key)) {
                return jedis.smembers(key);
            } else {
                return null;
            }
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }


    public Long hashIncrby(String key, String field, Integer increment) {

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hincrBy(key, field, increment);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }

    public Long delHashField(String key, String field) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis.hexists(key, field)) {
                return jedis.hdel(key, field);
            } else {
                return (long) 0;//hash的field不存在
            }
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return (long) 0;
    }

    public List<String> hmget(String key, String filedname) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis.exists(key)) {
                return jedis.hmget(key, filedname);
            } else {
                return new ArrayList<String>();
            }
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return new ArrayList<String>();
    }

    public boolean hmset(String key, Map map) {
        Jedis jedis = null;
        boolean result = false;
        try {
            jedis = getJedis();
            if (jedis != null) {
                jedis.hmset(key, map);
                result = true;
            }
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return result;
    }

    public boolean hmset(String key, Map map, int timeout) {
        Jedis jedis = null;
        boolean result = false;
        try {
            jedis = getJedis();
            if (jedis != null) {
                jedis.hmset(key, map);
                jedis.expire(key, timeout);
                result = true;
            }
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return result;
    }

    public String getValue(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis.exists(key)) {
                return jedis.get(key);
            } else {
                return null;
            }
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }

    /**
     * 判断KEY是否存在
     *
     * @param key
     * @return 存在返回true 不存在返回false
     */
    public boolean isExists(String key) {
        boolean rebool = false;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            rebool = jedis.exists(key);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return rebool;
    }

    public boolean isRedisOK() {
        Jedis jedis = null;
        boolean bRet = false;
        // 捕捉异常
        try {
            jedis = getJedis();
            bRet = true;
        } catch (Exception e) {
            bRet = false;
        } finally {
            returnResource(jedis, true);
        }
        return bRet;
    }

    /**
     * 获取String值
     *
     * @param key
     * @return value
     */
    public String getString(String key) {

        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis == null) {
                return "";
            }
            if (jedis.exists(key)) {
                return jedis.get(key);
            }
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return "";
    }


    public Long hashIncrby(String key, String field) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis.exists(key)) {
                return jedis.hincrBy(key, field, 1);
            } else {
                return null;
            }
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }

        return null;
    }

    public List<String> lrange(String key, Integer beg, Integer end) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis.exists(key)) {
                return jedis.lrange(key, beg, end);
            } else {
                return new ArrayList<String>();
            }
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return new ArrayList<String>();
    }

    public long lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lpush(key, value);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return 0L;
    }

    /*
     * 更新key的过期时间
     * */
    public long expire(String key, int timeout) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.expire(key, timeout);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return 0L;
    }


    public long del(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.del(key);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return 0L;
    }

    public Set<String> smembers(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.smembers(key);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return new HashSet<String>();
    }

    public long hdel(String key, String field) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hdel(key, field);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return 0L;
    }

    public long ttl(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.ttl(key);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return 0L;
    }

    public Set keys(String key) {
        if (StringUtils.isEmpty(key) || ("*").equals(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.keys(key);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }

    public List<String> scan(String key) {
        if (StringUtils.isEmpty(key) || ("*").equals(key)) {
            return null;
        }
        Jedis jedis = null;
        ScanParams param = new ScanParams();
        param.match(key);
        param.count(100);
        try {
            jedis = getJedis();
            ScanResult<String> tt = jedis.scan("0", param);
            if (tt != null && tt.getResult() != null) {
                return tt.getResult();
            } else {
                return null;
            }

        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }

    private boolean chkkey(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        if (key.contains("*")) {
            return false;
        }
        return true;
    }

    //添加地理位置
    public Long geoadd(String key, GeoCoordinate coordinate, String memberName) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.geoadd(key, coordinate.getLongitude(), coordinate.getLatitude(), memberName);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }

    /**
     * 批量添加地理位置
     *
     * @param key
     * @param memberCoordinateMap
     * @return
     */

    public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.geoadd(key, memberCoordinateMap);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }


    /**
     * 根据给定地理位置坐标获取指定范围内的地理位置集合（返回匹配位置的经纬度 + 匹配位置与给定地理位置的距离 + 从近到远排序，）
     *
     * @param key
     * @param coordinate
     * @param radius
     * @return List<GeoRadiusResponse>
     */

    public List<GeoRadiusResponse> geoRadius(String key, GeoCoordinate coordinate, double radius) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.georadius(key, coordinate.getLongitude(), coordinate.getLatitude(), radius, GeoUnit.KM, GeoRadiusParam.geoRadiusParam().withDist().withCoord().sortAscending());
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }


    /**
     * 根据给定地理位置获取指定范围内的地理位置集合（返回匹配位置的经纬度 + 匹配位置与给定地理位置的距离 + 从近到远排序，）
     *
     * @param key
     * @param member
     * @param radius
     * @return List<GeoRadiusResponse>
     */

    List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.georadiusByMember(key, member, radius, GeoUnit.KM, GeoRadiusParam.geoRadiusParam().withDist().withCoord().sortAscending());
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }


    /**
     * 查询两位置距离
     *
     * @param key
     * @param member1
     * @param member2
     * @param unit
     * @return
     */

    public Double geoDist(String key, String member1, String member2, GeoUnit unit) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.geodist(key, member1, member2, unit);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }

    /**
     * 可以获取某个地理位置的geohash值
     *
     * @param key
     * @param members
     * @return
     */

    public List<String> geohash(String key, String... members) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.geohash(key, members);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }

    /**
     * 获取地理位置的坐标
     *
     * @param key
     * @param members
     * @return
     */

    public List<GeoCoordinate> geopos(String key, String... members) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.geopos(key, members);
        } catch (Exception e) {
            returnResource(jedis, false);
        } finally {
            returnResource(jedis, true);
        }
        return null;
    }
}
