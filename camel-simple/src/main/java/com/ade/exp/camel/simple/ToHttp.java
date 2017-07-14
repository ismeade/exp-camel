package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.io.InputStream;

/**
 * Created by liyang on 2017/7/6.
 */
public class ToHttp {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("timer://foo?fixedRate=true&period=5000")
                        .to("http://192.169.101.139:8080/service")
                        .process(exchange -> {
                            InputStream inputStream = (InputStream) exchange.getIn().getBody();
                            byte[] buf = new byte[1024];
                            int length = inputStream.read(buf);
                            System.out.println(new String(buf, 0, length, "UTF-8"));
                            System.out.println(exchange.getIn().getHeaders());
                        })
                ;
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }
}
