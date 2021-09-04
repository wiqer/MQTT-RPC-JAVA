package com.wiqer.rpc.serialize;

import lombok.Data;

@Data
public class SuperMsg extends BaseMsg {
    public Object msg ;
    public SuperMsg(Object msg)
    {
        this.msg = msg;
    }
    public SuperMsg setMsg(Object msg)
    {
        this.msg = msg;
        return this;
    }
}
