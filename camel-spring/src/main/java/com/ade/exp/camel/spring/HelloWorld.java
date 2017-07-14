package com.ade.exp.camel.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * Created by liyang on 2017/5/4.
 */
public class HelloWorld {

    public static void main(String[] args) throws Exception {

        ApplicationContext context = new ClassPathXmlApplicationContext("application-config.xml");

        System.out.println(context.getBean("defineProcessor"));

        synchronized (HelloWorld.class) {
            HelloWorld.class.wait();
        }
    }


}
