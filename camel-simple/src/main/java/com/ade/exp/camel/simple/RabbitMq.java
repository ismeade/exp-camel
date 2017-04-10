package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 * Created by liyang on 2017/4/5.
 */
public class RabbitMq {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("rabbitmq://192.168.101.112:5672/topic.test?username=test&password=test&exchangeType=topic&autoDelete=false&routingKey=test")
                        .process(exchange -> {
                            String str = exchange.getIn().getBody(String.class);
                            exchange.getOut().setBody(str + "\r\n");
                        })
                        .multicast() // 发给多个目标，没有这行只会顺序发，一个接收成功就会停止
//                        .to("stream:out");
                        .to("file://c:/logs?fileName=test.txt&fileExist=Append", "stream:out");
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }

}
