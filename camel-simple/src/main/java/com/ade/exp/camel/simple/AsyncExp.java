package com.ade.exp.camel.simple;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.concurrent.TimeUnit;

/**
 *
 * Created by liyang on 2017/7/1.
 */
public class AsyncExp {

    public static void main(String[] args) throws Exception {
        RouteBuilder builder = new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("jetty:http://localhost:8088/test").process(exchange -> {
                    System.out.println(Thread.currentThread().getName());
                    System.out.println("process1");
                    exchange.getOut().setBody("async-processor");
                }).process(new AsyncProcessor() {

                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("process");
                    }

                    @Override
                    public boolean process(Exchange exchange, AsyncCallback callback) {
                        System.out.println(Thread.currentThread().getName());
                        System.out.println("async process");
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        callback.done(false);
                        return false;
                    }
                }).process(exchange -> {
                    System.out.println(Thread.currentThread().getName());
                    System.out.println("process2");
                }).to("file://c:/logs?fileName=test.txt", "stream:out").process(exchange -> {
                    TimeUnit.SECONDS.sleep(5);
                    System.out.println(Thread.currentThread().getName());
                    System.out.println("process3");
                    exchange.getOut().setBody("process3");
                }).to("stream:out");
            }
        };

        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(builder);
        camelContext.start();

//        Thread.sleep(100000);

    }

}
