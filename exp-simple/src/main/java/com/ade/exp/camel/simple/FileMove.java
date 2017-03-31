package com.ade.exp.camel.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.io.*;

/**
 *
 * 文件移动
 * Created by liyang on 2017/3/31.
 */
public class FileMove {

    public static void main(String args[]) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                // 每10秒读取c:/logs/1文件夹中文件
                from("file:c:/logs/1?delay=10000&delete=true")
//                        .multicast()
                        // 移动到c:/logs/2
                        .to("file:c:/logs/2")
                        // 模拟筛选，找出所有包含WARN的行
                        .process(exchange -> {
                            InputStream in = exchange.getIn().getBody(InputStream.class);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            StringBuilder builder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.contains("WARN")) {
                                    builder.append(line).append("\n");
                                }
                            }
                            exchange.getOut().setBody(builder.toString());
                            in.close();
                            reader.close();
                        })
                        // 在控制台输出
                        .to("stream:out");
            }
        });
        context.start();
        Thread.sleep(Integer.MAX_VALUE);  // 为了保持CamelContext处于工作状态，这里需要sleep主线程
        context.stop();
    }

}
