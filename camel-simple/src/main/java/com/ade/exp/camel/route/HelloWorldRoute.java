package com.ade.exp.camel.route;

import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;

import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * Created by liyang on 2017/7/3.
 */
public class HelloWorldRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer://foo?fixedRate=true&period=2000")
                .process(exchange -> {
                            exchange.getOut().setBody(new Date());
                            exchange.getOut().setHeader("a", "1");
                        }
                )
//                        .choice()
//                        .when(header("a").isEqualTo("123"))
//                        .to()
//                        .otherwise()
//                        .to()
//                        .endChoice()
//                        .toD("rabbitmq://192.168.101.112:5672/topic.test?username=test&password=test&exchangeType=topic&autoDelete=true&routingKey=test." + num[0])
//                        .to("stream:out")
                .routingSlip(method(this.getClass(), "slip")) // 动态路由
        ;
    }

    int cum = 0;

    public String slip(@Headers Map<String, Object> headers) {
        System.out.println(headers);
        if (cum == 5) cum = 0;
        return "rabbitmq://192.168.101.112:5672/topic.test?username=test&password=test&exchangeType=topic&autoDelete=true&routingKey=test." + cum++;
    }
}
