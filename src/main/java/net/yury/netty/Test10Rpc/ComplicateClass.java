package net.yury.netty.Test10Rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ComplicateClass implements Serializable {
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class TestInnerClass {
        private Class<?> clazz = TestInnerClass.class;
    }

    private int id;
    private String[] array;
    private TestInnerClass obj;
    private Map<Long, Long> map;
}
