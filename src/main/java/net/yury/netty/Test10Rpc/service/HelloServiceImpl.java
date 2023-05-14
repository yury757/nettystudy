package net.yury.netty.Test10Rpc.service;

import net.yury.netty.Test10Rpc.ComplicateClass;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloServiceImpl implements HelloService, RpcRegister {
    public static final AtomicInteger COUNT = new AtomicInteger(0);

    @Override
    public String sayHello(String msg) {
        return "hello world, " + msg + ", your count is " + COUNT.getAndIncrement();
    }

    @Override
    public ComplicateClass testComplicateReturnType() {
        return new ComplicateClass(1, new String[] {"123"}, new ComplicateClass.TestInnerClass(), new HashMap<>());
    }

    @Override
    public void testError() {
        int i = 1 / 0;
    }

    @Override
    public String addNewMethod() {
        return "123";
    }

    @Override
    public Object getInstance() {
        return new HelloServiceImpl();
    }
}
