package com.wiqer.rpc.serialize;

import lombok.Data;

@Data
public class SuperMsgMulti extends  BaseMsg {
    public Object[] msg ;

    public Object response ;


    public SuperMsgMulti setMsg(Object[] msg)
    {
        this.msg = msg;
        return this;
    }
    public SuperMsgMulti setReq(Object response)
    {
        this.msg = null;
        this.response = response;
        return this;
    }


}
