package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.mina.common.IoSession;

/**
 *
 * Created by liyang on 2017/4/7.
 */
public class FormMina {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("mina:tcp://localhost:8999?textline=true&sync=true")
                        .process(exchange -> {
                            System.out.println(exchange.getIn().getHeaders());
                            Object sess = exchange.getIn().getHeader("CamelMinaIoSession");
                            IoSession session = (IoSession) sess;
                            session.write("test");
                            String str = exchange.getIn().getBody(String.class);
                            System.out.println("=== " + str + " ===");
                            exchange.getOut().setBody("success");
                        })
//                        .multicast()
//                        .to("stream:out")
                ;
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }

}
