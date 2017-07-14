package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.Date;

/**
 * Created by liyang on 2017/3/31.
 */
public class FormJetty {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("jetty:http://192.168.101.139:8998")
//                        .from("jetty:http://localhost:8088")
                        .process(exchange -> {
                            Message message = exchange.getIn();
//                            System.out.println("---");
//                            System.out.println(message.getHeaders());
//                            System.out.println(message.getHeader("CamelHttpServletRequest").getClass());
//                            System.out.println(message.getHeader("CamelHttpServletResponse").getClass());
//                            System.out.println("---");
//                            System.out.println("in:" + exchange.getIn().getHeaders());
//                            System.out.println("out:" + exchange.getOut().getHeaders());
                            System.out.println("in:" + exchange.getIn().getBody());
//                            System.out.println("out:" + exchange.getOut().getBody());
//                            System.out.println("===");
//                            exchange.getIn().setBody("in body");
//                            exchange.getOut().setBody("out body");
//                            exchange.getIn().setHeader("TEST", "in body header");
//                            exchange.getOut().setHeader("TEST", "out body header");
//                            exchange.setOut(exchange.getIn());
                            exchange.getOut().setBody("success");
                        })

//                        .to("direct:t1", "stream:out")
                ;
            }
        });
//        context.addRoutes(new RouteBuilder() {
//            @Override
//            public void configure() throws Exception {
//                from("direct:t1")
//                        .process(exchange -> {
//                            System.out.println("in:" + exchange.getIn().getHeaders());
//                            System.out.println("out:" + exchange.getOut().getHeaders());
//                            System.out.println("in:" + exchange.getIn().getBody());
//                            System.out.println("out:" + exchange.getOut().getBody());
//                            exchange.getOut().setBody("direct");
//                        });
//            }
//        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }

}
