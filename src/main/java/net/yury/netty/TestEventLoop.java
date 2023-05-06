package net.yury.netty;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EventLoopGroup是一个事件循环组，可以包含多个事件循环器，每一个事件循环器实际上是一个单线程的执行器，因此也可以像线程池一样执行普通任务
 * 在事件循环器中，每个IO事件的处理都被当作一个IOTask来运行，因此才需要执行器
 */
public class TestEventLoop {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        // 1、初始化事件循环组
        // IO事件、普通任务、定时任务
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        // 普通任务、定时任务
        EventLoopGroup group2 = new DefaultEventLoopGroup();

        // 2、获取下一个事件循环对象
        System.out.println(bossGroup.next());
        System.out.println(bossGroup.next());
        System.out.println(bossGroup.next());

        // 3、执行普通任务
        Future<?> future = bossGroup.next().submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(123);
            return 234;
        });
        System.out.println("普通任务执行中...");
        Object o = future.get();
        System.out.println(o);

        // 4、执行定时任务。在设置了keepalive时，定时任务会发挥作用
        AtomicInteger count = new AtomicInteger(0);
        bossGroup.next().scheduleAtFixedRate(
                () -> System.out.println("count: " + count.getAndIncrement()),
                0,
                1,
                TimeUnit.SECONDS);

        // 5、用于网络IO的事件循环，详见netty其他example
    }
}
