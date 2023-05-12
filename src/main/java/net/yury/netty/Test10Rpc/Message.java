package net.yury.netty.Test10Rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Message {
    public static final int RPC_MESSAGE_TYPE_REQUEST = 0;
    public static final int RPC_MESSAGE_TYPE_RESPONSE = 1;
    @JsonIgnore
    int getMessageType();
}
