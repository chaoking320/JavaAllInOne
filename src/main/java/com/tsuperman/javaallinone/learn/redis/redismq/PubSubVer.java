package com.tsuperman.javaallinone.learn.redis.redismq;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

/**
 * 基于PUBSUB的消息中间件的实现
 */
@Component
public class PubSubVer extends JedisPubSub {
    public final static String KEY = "key:";

    @Autowired
    private JedisPool jedisPool;

    /**
     * 消费消息
     * @param channel
     * @param message
     */
    @Override
    public void onMessage(String channel, String message) {
        System.out.println("Accept " + channel + " message:" + message);
    }

    /**
     * 注册
     * @param channel
     * @param subscribedChannels
     */
    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        System.out.println("Subscribe " + channel + " count:" + subscribedChannels);
    }

    /**
     * 发布消息
     * @param channel
     * @param message
     */
    public void pub(String channel, String message) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.publish(KEY + channel, message);
            System.out.println("publish message ：" + KEY + channel + " message=" + message);
        } catch (Exception e) {
            throw new RuntimeException("fail！");
        } finally {
            jedis.close();
        }
    }

    /**
     * 订阅消息
     * @param channels
     */
    public void sub(String... channels) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.subscribe(this, channels);
        } catch (Exception e) {
            throw new RuntimeException("fail！");
        } finally {
            jedis.close();
        }
    }

}
