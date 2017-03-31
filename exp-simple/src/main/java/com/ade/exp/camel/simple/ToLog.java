package com.ade.exp.camel.simple;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMessage;
import org.apache.camel.impl.DefaultCamelContext;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * Created by liyang on 2017/3/31.
 */
public class ToLog {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext(); // 1. 创建 CamelContext.
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("jetty:http://localhost:8011/toLog")
                        .process(exchange -> {
//                            HttpServletRequest req = exchange.getIn().getBody(HttpServletRequest.class);
//                            String str = req.getParameter("abc");
//                            System.out.println(str);
//                            exchange.getOut().setBody(str);
                            String str = exchange.getIn().getBody(String.class);
                            exchange.getOut().setBody(str);
                        })
                        .multicast() // 发给多个目标，没有这行只会顺序发，一个接收成功就会停止
                        .to("log:ToLog?level=INFO", "stream:out", "file://c:/logs?fileName=test.txt"); // 2. 为路由配置组件或终端节点.
            }
        }); // 3. 添加路由到CamelContext
        context.setTracing(true);
        context.start(); // 4. 启动CamelContext.
        Thread.sleep(Integer.MAX_VALUE);  // 为了保持CamelContext处于工作状态，这里需要sleep主线程
        context.stop(); // 最后停止CamelContext
    }

}
