package com.ade.exp.camel.simple;

import com.ade.exp.camel.route.HelloWorldRoute;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 * Created by liyang on 2017/3/31.
 */
public class HelloWorld {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new HelloWorldRoute());
        context.setTracing(true);
        context.start();
        Thread.sleep(Integer.MAX_VALUE);
        context.stop();
    }

}
