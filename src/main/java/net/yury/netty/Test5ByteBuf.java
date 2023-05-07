package net.yury.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Iterator;

@Slf4j
public class Test5ByteBuf {
    public static void main(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(1);
        System.out.println(buffer);
        byte[] bytes = {2, 2};
        buffer.writeBytes(bytes);
        System.out.println(buffer);
        buffer.readBytes(bytes);
        System.out.println(buffer);

        buffer.writeInt(123);

        System.out.println("==============================");
        buffer = ByteBufAllocator.DEFAULT.buffer(5);
        buffer.writeBytes(new byte[]{1,2,3});
        System.out.println(buffer);
        ByteBuf buffer2 = ByteBufAllocator.DEFAULT.buffer(5);
        buffer2.writeBytes(new byte[]{6,7,8});
        System.out.println(buffer2);
        CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        compositeByteBuf.addComponents(true, buffer, buffer2);
        compositeByteBuf.retain();
        byte[] bytes2 = new byte[10];
        compositeByteBuf.writeByte(4);
        compositeByteBuf.readBytes(bytes2, 0, 7);
        Iterator<ByteBuf> iterator = compositeByteBuf.iterator();
        while (iterator.hasNext()) {
            ByteBuf next = iterator.next();
            System.out.println(next);
        }
        log.debug(Arrays.toString(bytes2));
    }
}
