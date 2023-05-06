package net.yury.netty;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestPromise {
    public static void main(String[] args) {
        NioEventLoopGroup executors = new NioEventLoopGroup();

        Callable<String> runnable = () -> {
            log.debug("处理任务中...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Thread.currentThread().getName();
        };

        for (int i = 0; i < 10; i++) {
            executors.submit(runnable).addListener((future) -> {
                DefaultPromise promise = (DefaultPromise) future;
                log.debug("得到结果:" + promise.getNow());
                executors.shutdownGracefully();
            });
        }
    }
}
