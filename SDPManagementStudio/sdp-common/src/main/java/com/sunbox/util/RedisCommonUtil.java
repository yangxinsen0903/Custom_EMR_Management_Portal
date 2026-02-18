package com.sunbox.util;

//import redis.clients.jedis.params.geo.GeoRadiusParam;
public class RedisCommonUtil {

    private String redisip /*= "SDc67ktucIRfdUb3pQVbLQ=="*/;
    private String redisport /*= "669rCH01m2w="*/;
    private String redispass /*= "AZ2J08Y2bX5lZuE5OtRPDg=="*/;
    private String redismaxactive /*= "hG1qUcOObxo="*/;
    private String redismaxidle /*= "BganOA8tCtU="*/;
    private String redismaxwait /*= "BganOA8tCtU="*/;
    private String redistimeout /*= "BganOA8tCtU="*/;
    private String redistestonborrow /*= "+RjsaaaVJnw="*/;

    public String getRedisip() {
        return redisip;
    }

    public void setRedisip(String redisip) {
        this.redisip = redisip;
    }

    public String getRedisport() {
        return redisport;
    }

    public void setRedisport(String redisport) {
        this.redisport = redisport;
    }

    public String getRedispass() {
        return redispass;
    }

    public void setRedispass(String redispass) {
        this.redispass = redispass;
    }

    public String getRedismaxactive() {
        return redismaxactive;
    }

    public void setRedismaxactive(String redismaxactive) {
        this.redismaxactive = redismaxactive;
    }

    public String getRedismaxidle() {
        return redismaxidle;
    }

    public void setRedismaxidle(String redismaxidle) {
        this.redismaxidle = redismaxidle;
    }

    public String getRedismaxwait() {
        return redismaxwait;
    }

    public void setRedismaxwait(String redismaxwait) {
        this.redismaxwait = redismaxwait;
    }

    public String getRedistimeout() {
        return redistimeout;
    }

    public void setRedistimeout(String redistimeout) {
        this.redistimeout = redistimeout;
    }

    public String getRedistestonborrow() {
        return redistestonborrow;
    }

    public void setRedistestonborrow(String redistestonborrow) {
        this.redistestonborrow = redistestonborrow;
    }
}
