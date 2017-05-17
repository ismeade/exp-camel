package com.ade.exp.camel.spring;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 *
 * Created by liyang on 2017/5/4.
 */
@Component("defineProcessor")
public class DefineProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        System.out.println("123");
    }

}
