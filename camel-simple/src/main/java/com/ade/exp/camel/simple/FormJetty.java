package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.Date;

/**
 *
 * Created by liyang on 2017/3/31.
 */
public class FormJetty {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("jetty:http://localhost:8080")
                        .from("jetty:http://localhost:8088")
                        .process(exchange -> exchange.getOut().setBody(new Date()))
                        .to("stream:out");
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }

}