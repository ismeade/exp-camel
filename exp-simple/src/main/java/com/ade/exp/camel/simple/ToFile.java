package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * Created by liyang on 2017/3/31.
 */
public class ToFile {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("jetty:http://localhost:8011/toFile")
                        .process(exchange -> {
                            HttpServletRequest req = exchange.getIn().getBody(HttpServletRequest.class);
                            String str = req.getParameter("abc");
                            System.out.println(str);
                            exchange.getOut().setBody(str);
                        })
                        .multicast() // 发给多个目标，没有这行只会顺序发，一个接收成功就会停止
                        .to("file://c:/logs?fileName=test.txt", "stream:out");
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }

}
