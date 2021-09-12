package com.wiqer.rpc.impl.core;

import com.wiqer.rpc.serialize.SerializerInterface;
import com.wiqer.rpc.serialize.SuperMsgMulti;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class RpcServerHandler implements ServerHandler {
    SerializerInterface serializer;
    public RpcServerHandler( SerializerInterface serializer){
        this.serializer=serializer;
    }
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
    @Override
    public String handle(String request, Object serviceBean, Method method) throws Throwable {

        SuperMsgMulti superMsg =serializer.DeSerializeString(request,SuperMsgMulti.class);
        return handle( superMsg,  serviceBean,  method);
    }
    @Override
    public String handle(SuperMsgMulti superMsg, Object serviceBean, Method method) throws Throwable {
        if (serviceBean == null) {
            logger.error("Can not find service implement ");
            return null;
        }

        Class<?>[] parameterTypes = method.getParameterTypes();


        if (null != superMsg.msg&& superMsg.msg.length>0)
        {
            Object[] objs = new Object[superMsg.msg.length];
            for (int j = 0; j < superMsg.msg.length; j++)
            {
                //mfs.reqs.Length
                objs[j] = this.serializer.DeSerializeString(superMsg.msg[j].toString(),parameterTypes[j]);
            }
            Object response =method.invoke(serviceBean, objs);
            if (method.getReturnType() != Void.class)
            {
                superMsg.setResponse(response);
                return serializer.SerializeString(response);
            }
        }
        return null;
    }
}
