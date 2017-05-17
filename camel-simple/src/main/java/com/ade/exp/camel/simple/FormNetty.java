package com.ade.exp.camel.simple;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.concurrent.TimeUnit;

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
//                from("netty4:tcp://localhost:8999")
                        .process(
                                exchange -> {
                                    Object obj = exchange.getIn().getHeader("CamelNettyChannelHandlerContext");
                                    ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) obj;
                                    System.out.println("hashCode [" + channelHandlerContext.hashCode() + "]");
                                    channelHandlerContext.pipeline()
                                            .addLast(new LineBasedFrameDecoder(1024))
                                            .addLast("idle", new IdleStateHandler(12, 12, 20, TimeUnit.SECONDS))
                                            .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                            .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                            .addLast(new NettyServerHandler());

                                    String str = exchange.getIn().getBody(String.class);
                                    System.out.println("== " + str + " ==");
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
//                        .choice()
//                        .when(exchange -> false)
//                        .to("direct:start")
//                        .otherwise()
//                        .to()
//                        .endChoice()
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
                        .to("direct:start")
                ;
            }
        });
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .process(exchange -> {
                            System.out.println("direct:start");
//                            NettyChannelBufferStreamCache cache = (NettyChannelBufferStreamCache) exchange.getIn().getBody();
//                            cache.writeTo(System.out);
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

    private static class NettyServerHandler extends ChannelInboundHandlerAdapter {

        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            System.out.println("channelRead");
            String body = (String) msg;
            System.out.println(ctx.channel().remoteAddress().toString());
            System.out.println("server received data :" + body);
            ctx.writeAndFlush("response.\n");//写回数据，
        }

        public void channelReadComplete(ChannelHandlerContext ctx) {
            System.out.println("channelReadComplete [" + ctx.hashCode() + "]");
//        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER) //flush掉所有写回的数据
//                .addListener(ChannelFutureListener.CLOSE); //当flush完成后关闭channel
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.out.println("exceptionCaught");
            ctx.close();//出现异常时关闭channel
        }

        // NettyServer中设置的 .addLast(new IdleStateHandler(12, 12, 10, TimeUnit.SECONDS)) 值，触发
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                switch (event.state()) {
                    case READER_IDLE:
                        System.out.println("READER_IDLE 读超时");
                        ctx.disconnect();
                        break;
                    case WRITER_IDLE:
                        System.out.println("WRITER_IDLE 写超时");
                        ctx.disconnect();
                        break;
                    case ALL_IDLE:
                        System.out.println("ALL_IDLE 总超时");
                        ctx.disconnect();
                        break;
                }
            }
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            // 释放掉该context相关资源
            System.out.println("channelUnregistered [" + ctx.hashCode() + "]");
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            System.out.println("channelActive");
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            super.channelRegistered(ctx);
            System.out.println("channelRegistered");
        }
    }

}
