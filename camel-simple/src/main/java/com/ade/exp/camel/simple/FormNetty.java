package com.ade.exp.camel.simple;

import io.netty.channel.ChannelHandlerContext;
import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.netty4.http.NettyChannelBufferStreamCache;
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
                        .process(
                                exchange -> {
                                    Object obj = exchange.getIn().getHeader("CamelNettyChannelHandlerContext");
                                    ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) obj;
                                    String str = exchange.getIn().getBody(String.class);
                                    System.out.println("=== " + str + " ===");
                                    if (null != str) {
                                        switch (str) {
                                            case "exit":
                                            case "quit":
                                                channelHandlerContext.disconnect();
                                                break;
                                            default:
                                                channelHandlerContext.writeAndFlush("command [" + str + "]\n");
                                        }
                                    }
//                                    exchange.getOut().setBody("response");
                                }
                        )
//                        .multicast()
                        .to("direct:start")
                ;
            }
        });
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("netty4-http:http://localhost:8998")
                        .process(
                                exchange -> {
                                    Message message = exchange.getIn();
                                    System.out.println(message.getHeader("CamelHttpMethod"));
                                    System.out.println(message.getHeader("CamelHttpUri"));
                                    System.out.println(message.getHeader("CamelHttpUrl"));

//                                    ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) message.getHeader("CamelNettyChannelHandlerContext");
//                                    channelHandlerContext.disconnect();

//                                    NettyChannelBufferStreamCache cache = (NettyChannelBufferStreamCache) message.getBody();
//                                    cache.writeTo(System.out);
//                                    exchange.getOut().setBody("response");
                                }
                        )
//                        .multicast()
                        .to("direct:start")
                ;
            }
        });
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .process(exchange -> {
                            System.out.println("direct:start");
                            NettyChannelBufferStreamCache cache = (NettyChannelBufferStreamCache) exchange.getIn().getBody();
                            cache.writeTo(System.out);
                            exchange.getOut().setBody("response");
                        });
            }
        });
        context.setTracing(true);
        context.start();
        synchronized (FormNetty.class) {
            FormNetty.class.wait();
        }
        context.stop();
    }

//    private static class TestProcessor implements Processor {
//
//        @Override
//        public void process(Exchange exchange) throws Exception {
//            System.out.println("headers: " + exchange.getIn().getHeaders());
//            System.out.println("Body: " + exchange.getIn().getBody());
//            exchange.getOut().setBody("response");
//        }
//
//    }

}
