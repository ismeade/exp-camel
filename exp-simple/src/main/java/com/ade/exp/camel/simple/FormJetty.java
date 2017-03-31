package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMessage;
import org.apache.camel.impl.DefaultCamelContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 *
 * Created by liyang on 2017/3/31.
 */
public class FormJetty {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext(); // 1. 创建 CamelContext.
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("jetty:http://localhost:8080")
                        .process(exchange -> exchange.getOut().setBody(new Date()))
//                        .process(new ReviveProcess())
                        .to("stream:out"); // 2. 为路由配置组件或终端节点.
            }
        }); // 3. 添加路由到CamelContext
        context.setTracing(true);
        context.start(); // 4. 启动CamelContext.
        Thread.sleep(Integer.MAX_VALUE);  // 为了保持CamelContext处于工作状态，这里需要sleep主线程
        context.stop(); // 最后停止CamelContext
    }

}
