package com.kedacom.confinterface.redis;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;

import java.util.*;

public class RedisClient {

    private RedisTemplate<String, Object> redisTemplate;

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean ping(){
        try {
            Jedis jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
            String pingRsp = jedis.ping();
            if ("PONG".equalsIgnoreCase(pingRsp)) {
                jedis.close();
                return true;
            }

            jedis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean keyExist(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Object getValue(String key) {
        return null == key ? null : redisTemplate.opsForValue().get(key);
    }

    public List<? extends Object> getMany(String keyPattern) {
        try {
            Set<String> keySet = redisTemplate.keys(keyPattern);

            if (keySet.isEmpty())
                return null;

            List<Object> objectList = new ArrayList<>();
            for (String key : keySet) {
                objectList.add(redisTemplate.opsForValue().get(key));
            }
            return objectList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean setValue(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delValue(String key) {
        try {
            if (null != key && !key.isEmpty()) {
                return redisTemplate.delete(key);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<? extends Object> listGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean listRemove(String key, long count, Object value){
        try {
            redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean listRemoveAll(String key){
        try {
            redisTemplate.opsForList().trim(key, 1, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean listPush(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<? extends Object, ? extends Object> hashGet(String key){
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean hashPut(String key, Object hashKey, Object value){
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean hashRemove(String key, Object hashKey){
        try {
            redisTemplate.opsForHash().delete(key, hashKey);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
