package net.yury.netty.Test10Rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RpcResponseMessage implements Message {
    private Object returnValue;
    private Throwable cause;

    @Override
    public int getMessageType() {
        return Message.RPC_MESSAGE_TYPE_RESPONSE;
    }
}