package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 * Created by liyang on 2017/4/6.
 */
public class FormHttp {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("http://192.168.101.231:8011/mcs")
                        .to("stream:out");
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }

}
