package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.concurrent.TimeUnit;

/**
 *
 * Created by liyang on 2017/4/5.
 */
public class RabbitMq {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
//        context.addRoutes(new RouteBuilder() { // 每5秒向topic.test推送消息
//            public void configure() {
//                from("timer://foo?fixedRate=true&period=5000")
//                        .process(exchange -> exchange.getIn().setBody("test_message"))
//                        .to("rabbitmq://192.168.101.112:5672/topic.test?username=test&password=test&exchangeType=topic&autoDelete=false")
//                ;
//            }
//        });
        context.addRoutes(new RouteBuilder() {
            public void configure() {
//                from("rabbitmq://192.168.101.112:5672/topic.test?username=test&password=test&exchangeType=topic&autoDelete=false")
//                from("rabbitmq://192.168.101.112:5672/topic.test?username=test&password=test&exchangeType=topic&autoDelete=true&routingKey=test.#")
                from("rabbitmq://192.168.101.225:5672/open-api?username=admin&password=admin&exchangeType=topic&autoDelete=false&routingKey=request.#")
                        .process(exchange -> {
                            String str = exchange.getIn().getBody(String.class);
                            System.out.println(str);
                            exchange.getOut().setHeader("rabbitmq.EXCHANGE_NAME", "open-api");
                            exchange.getOut().setHeader("rabbitmq.ROUTING_KEY", "response.t1");
                            exchange.getOut().setBody(str);
                            TimeUnit.SECONDS.sleep(2);
                        })
//                        .multicast() // 发给多个目标，没有这行只会顺序发，一个接收成功就会停止
//                        .to("stream:out");
//                      由rabbitMQ -> rabbitMQ 时，要在process中修改Headers中的 rabbitmq.EXCHANGE_NAME=topic.test, rabbitmq.ROUTING_KEY=xxx 参数，否则会直接to到入口from处
//                        .to("rabbitmq://192.168.101.112:5672/topic.test2?username=test&password=test&exchangeType=topic&autoDelete=false")
                        .to("rabbitmq://192.168.101.225:5672/open-api?username=admin&password=admin&exchangeType=topic&autoDelete=false&routingKey=response.t1")
                ;
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }


}
