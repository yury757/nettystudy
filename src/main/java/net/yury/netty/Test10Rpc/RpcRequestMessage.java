package net.yury.netty.Test10Rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RpcRequestMessage implements Message {
    private long sequenceID;
    private String interfaceName;
    private String methodName;
    private Class<?> returnType;
    private Class[] paramsTypes;
    private Object[] paramsValues;
}