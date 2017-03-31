package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.Date;

/**
 *
 * Created by liyang on 2017/3/31.
 */
public class ToFile {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext(); // 1. 创建 CamelContext.
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("jetty:http://localhost:8012/toFile")
                        .process(exchange -> exchange.getOut().setBody("53252353"))
                        .multicast() // 发给多个目标，没有这行只会顺序发，一个接收成功就会停止
//                        .process(new ReviveProcess())
                        .to("file://c:/logs?fileName=test.txt", "stream:out", "http4:localhost:8011/toLog"); // 2. 为路由配置组件或终端节点.
            }
        }); // 3. 添加路由到CamelContext
        context.setTracing(true);
        context.start(); // 4. 启动CamelContext.
        Thread.sleep(Integer.MAX_VALUE);  // 为了保持CamelContext处于工作状态，这里需要sleep主线程
        context.stop(); // 最后停止CamelContext
    }

}
