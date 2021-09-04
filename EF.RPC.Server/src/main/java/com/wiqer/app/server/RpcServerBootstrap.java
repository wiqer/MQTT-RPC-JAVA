package com.wiqer.app.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RpcServerBootstrap {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("server-spring.xml");
    }
}

