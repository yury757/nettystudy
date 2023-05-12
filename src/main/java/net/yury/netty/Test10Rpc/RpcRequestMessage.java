package net.yury.netty.Test10Rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RpcRequestMessage implements Message {
    private String interfaceName;
    private String methodName;
    private Class<?> returnType;
    private Class[] paramsTypes;
    private Object[] paramsValues;

    @Override
    public int getMessageType() {
        return Message.RPC_MESSAGE_TYPE_REQUEST;
    }
}