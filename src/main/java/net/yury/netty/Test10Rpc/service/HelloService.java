package net.yury.netty.Test10Rpc.service;

import net.yury.netty.Test10Rpc.ComplicateClass;

public interface HelloService {
    /**
     * 普通方法测试
     * @param msg
     * @return
     */
    public String sayHello(String msg);

    /**
     * 返回值为复杂类型方法测试
     * @return
     */
    public ComplicateClass testComplicateReturnType();

    /**
     * 异常测试
     */
    public void testError();

    /**
     * 新增一个方法后不需要对架构进行调整，只需要：
     * 1、新增接口方法
     * 2、实现类新增方法实现
     * 3、重启客户端
     * 4、客户端调用新方法，重启客户端
     */
    public String addNewMethod();
}