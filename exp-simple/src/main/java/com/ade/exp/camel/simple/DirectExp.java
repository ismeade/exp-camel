package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.concurrent.TimeUnit;

/**
 *
 * Created by liyang on 2017/4/6.
 */
public class DirectExp {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .process(exchange -> TimeUnit.SECONDS.sleep(2))
                        .multicast()
                        .to("stream:out", "direct:start");
            }
        });
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("jetty:http://localhost:8080")
                        .to("stream:out", "direct:start");
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }

}
