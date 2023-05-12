package net.yury.netty.Test10Rpc.service;

import java.util.concurrent.atomic.AtomicInteger;

public class HelloServiceImpl implements HelloService, RpcRegister {
    public static final AtomicInteger COUNT = new AtomicInteger(0);

    @Override
    public String sayHello(String msg) {
        return "hello world, " + COUNT.getAndIncrement();
    }

    @Override
    public Object getInstance() {
        return new HelloServiceImpl();
    }
}
