package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 * Created by liyang on 2017/3/31.
 */
public class FileMove {

    public static void main(String args[]) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("file:c:/logs/1?delay=10000")
                        .multicast()
                        .to("file:c:/logs/2", "stream:out", "");
            }
        });
        context.start();
        Thread.sleep(Integer.MAX_VALUE);  // 为了保持CamelContext处于工作状态，这里需要sleep主线程
        context.stop();
    }

}
