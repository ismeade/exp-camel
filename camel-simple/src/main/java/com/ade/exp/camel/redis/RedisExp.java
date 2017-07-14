package com.ade.exp.camel.redis;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Date;

/**
 * 还没调通
 * Created by liyang on 2017/4/7.
 */
public class RedisExp {

    public static void main(String[] args) throws Exception {
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(); // 创建connectionFactory
        connectionFactory.setHostName("192.168.101.234");
        connectionFactory.setPassword("redis123");
        connectionFactory.setPort(6379);
        SimpleRegistry registry = new SimpleRegistry();
        connectionFactory.afterPropertiesSet(); // 必须要调用该方法来初始化connectionFactory
        registry.put("connectionFactory", connectionFactory); //注册connectionFactory
        registry.put("serializer", new StringRedisSerializer()); //注册serializer
        CamelContext context = new DefaultCamelContext(registry);
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("timer://foo?fixedRate=true&period=5000").
                        setHeader("CamelRedis.Command", constant("SET")).
                        setHeader("CamelRedis.Key", constant("camel_test_1")).
                        setHeader("CamelRedis.Value", constant(new Date().toString())).
                        to("spring-redis://192.168.101.234:6379?connectionFactory=#connectionFactory&serializer=#serializer");
//                from("spring-redis://192.168.101.234:6379?command=SUBSCRIBE&channels=testChannel")
//                        .to("stream:out");
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }

}
