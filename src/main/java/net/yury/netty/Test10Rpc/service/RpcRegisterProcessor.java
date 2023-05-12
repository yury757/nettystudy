package net.yury.netty.Test10Rpc.service;

import lombok.extern.slf4j.Slf4j;
import net.yury.ClassUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class RpcRegisterProcessor {
    /**
     * 接口 => 对象
     */
    public static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Object>> RPC_SERVICE = new ConcurrentHashMap<>();

    /**
     * 往 {@link RpcRegisterProcessor#RPC_SERVICE } 注册一个类及其对象，会将这个类的所有实现接口抽取出来，并进行注册
     * @param clazz
     * @param obj
     */
    public static void register(Class<?> clazz, Object obj) {
        Class<?> objClass = obj.getClass();
        // 验证obj是否是clazz的实例，或者子类实例
        objClass.asSubclass(clazz);
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> var1 : interfaces) {
            if (RpcRegister.class.equals(var1)) {
                continue;
            }
            RPC_SERVICE.putIfAbsent(var1, new CopyOnWriteArrayList<>());
            RPC_SERVICE.get(var1).add(obj);
            log.debug("register success. interface: " + var1 + ", impl: " + clazz);
        }
    }

    /**
     * 初始化，将指定包下实现了 {@link RpcRegister} 接口的所有类初始化，并注册到 {@link RpcRegisterProcessor#RPC_SERVICE } 中
     */
    public static void init() {
        List<Class<? extends RpcRegister>> clazzList = ClassUtils.getAllClassByInterface("net.yury.netty.Test10Rpc.service", RpcRegister.class);
        for (Class<? extends RpcRegister> clazz : clazzList) {
            RpcRegister obj;
            try {
                obj = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            register(clazz, obj);
        }
    }
}
