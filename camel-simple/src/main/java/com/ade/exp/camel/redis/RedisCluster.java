package com.ade.exp.camel.redis;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.*;

import java.util.*;

/**
 * Created by liyang on 2017/7/14.
 */
public class RedisCluster {

    public static void main(String[] args) throws Exception {
        JedisPoolConfig jpc = new JedisPoolConfig();
        jpc.setMaxIdle(100);
        jpc.setMaxTotal(100);
        jpc.setMaxWaitMillis(1000);
        jpc.setTestOnBorrow(true);
        jpc.setTestOnReturn(true);
        JedisClusterConnectionFactory connectionFactory = new JedisClusterConnectionFactory(jpc);
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
//                from("timer://foo?fixedRate=true&period=5000")
                from("jetty:http://localhost:8080")
                        .choice()
                        .when(header("CamelHttpMethod").isEqualTo("GET"))
                        .setHeader("CamelRedis.Command", constant("GET"))
                        .setHeader("CamelRedis.Key", constant("camel_test_1"))
//                        .setHeader("CamelRedis.Value", constant(new Date().toString())).
                        .to("spring-redis://192.168.101.234:6379?connectionFactory=#connectionFactory&serializer=#serializer")
                        .to("stream:out")
//                        .process(exchange -> {
//                            System.out.println(exchange.getIn().getHeaders());
//                            System.out.println(exchange.getIn().getBody());
//                            System.out.println(exchange.getOut().getBody());
//                            System.out.println(exchange.getOut().getBody());
//                        })
//                        .process(exchange -> {
//                            exchange.getOut().setBody("1111111");
//                        })
                        .endChoice()

                ;
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }

}
