package com.ade.exp.camel.simple;

import io.netty.channel.ChannelHandlerContext;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 * Created by liyang on 2017/4/7.
 */
public class FormNetty {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("netty4:tcp://localhost:8999?textline=true")
                        .process(exchange -> {
                            Object obj = exchange.getIn().getHeader("CamelNettyChannelHandlerContext");
                            ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) obj;
//                            channelHandlerContext.
                            System.out.println(channelHandlerContext.name());
                            System.out.println(channelHandlerContext.handler());
                            channelHandlerContext.writeAndFlush("test\n");
                            System.out.println(obj.getClass());
                            String str = exchange.getIn().getBody(String.class);
                            System.out.println("=== " + str + " ===");
                            exchange.getOut().setBody("success");
                        })
//                        .multicast()
//                        .to("stream:out")
                ;
            }
        });
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }
}
