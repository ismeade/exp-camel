package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 * Created by liyang on 2017/4/7.
 */
public class FormMina {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("mina:tcp://localhost:8999?textline=true&sync=false")
                        .process(exchange -> {
                            String str = exchange.getIn().getBody(String.class);
                            System.out.println("=== " + str + " ===");
                            exchange.getOut().setBody(str + "\r\n");
                        })
                        .multicast()
                        .to("stream:out", "file://c:/logs?fileName=test.txt&fileExist=Append");
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }

}
