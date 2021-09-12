package com.wiqer.rpc.nettyiml;

import com.wiqer.rpc.impl.core.BaseMsgFun;

import java.lang.reflect.Method;

public class NettyMsgFun  extends BaseMsgFun {
    Object bean;
    Method method;

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
