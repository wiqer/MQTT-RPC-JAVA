package com.wiqer.rpc.nettyiml;

import com.wiqer.rpc.serialize.SuperMsgMulti;
import lombok.Data;

@Data
public class NettyMsg {
    private String queName;
    private SuperMsgMulti superMsgMulti;
}
