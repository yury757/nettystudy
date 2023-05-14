package net.yury.netty.Test10Rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RpcResponseMessage implements Message {
    private long sequenceID;
    private Object returnValue;
    private Throwable cause;
}