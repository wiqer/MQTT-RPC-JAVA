package com.wiqer.rpc.impl.proxy;

import com.wiqer.rpc.impl.core.BaseMsgFun;
import com.wiqer.rpc.impl.producerImpl.MsgProducerMap;
import com.wiqer.rpc.impl.util.ServiceUtil;
import com.wiqer.rpc.serialize.SuperMsgMulti;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Created by luxiaoxun on 2016-03-16.
 */
public abstract class ObjectProxy<T, P> extends MsgProducerMap implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);
    private Class<T> clazz;
    protected String version;
    public ObjectProxy(Class<T> clazz, String version){
        this.clazz = clazz;
        this.version = version;
        getMathsInfoMulti();
    }
    protected void getMathsInfoMulti(){
        Arrays.stream(clazz.getMethods()).forEach(method -> {

            //方法名
            String methodName = method.getName();
            if (methodName.equals("toString") || methodName.equals("equals") || methodName.equals("hashCode") ||
                    methodName.equals("getClass")|| methodName.equals("notify") || methodName.equals("notifyAll")
                    || methodName.equals("wait") ) return;
            BaseMsgFun baseMsgFun =new BaseMsgFun();
            baseMsgFun.Name = methodName;
            baseMsgFun.responseType =method.getReturnType();
            baseMsgFun.methodInfo = method;
            baseMsgFun.ReqFullName = version + "." + FullName + "." + baseMsgFun.Name;
            baseMsgFun.reqTypes=method.getParameterTypes();
            String serviceName = ServiceUtil.makeServiceKey(baseMsgFun.Name, version);
            AtomicReference<String> queName= new AtomicReference<>(serviceName + "." + method.getName());
            Arrays.stream(baseMsgFun.reqTypes).forEach(classType->{
                queName.set(queName.get()+classType.toString().hashCode()%100+"");
            });
            baseMsgFun.FullName =queName.get();
            this.put(queName.get(), baseMsgFun);
        });
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        SuperMsgMulti msg = new SuperMsgMulti();
        msg.setMsg(args);
        String serviceName = ServiceUtil.makeServiceKey(method.getName(), version);
        AtomicReference<String> queName= new AtomicReference<>(serviceName + "." + method.getName());
        Arrays.stream(method.getParameterTypes()).forEach(classType->{
            queName.set(queName.get()+classType.toString().hashCode()%100+"");
        });
        BaseMsgFun baseMsgFun= (BaseMsgFun)this.get(queName.get());
        if (baseMsgFun != null)
        {
            sendMsg( proxy,method,msg,queName.get());
            if (method.getReturnType() != Void.class)
            {
                baseMsgFun.acquire(msg.Id);
                msg = baseMsgFun.getAndRemoveMsg(msg);
                if (null == msg)
                {
                    return null;
                }
                else
                {// this.serializer.DeSerializeString(method.ReturnType, new object().ToString());
                    return this.serializer.DeSerializeString(method.getReturnType(), msg.getResponse().toString());
                }
            }
        }
        else {
            throw new NoSuchMethodError("未成功加载到方法,请仔细排查一下");
        }
        // Debug
        if (logger.isDebugEnabled()) {
            logger.debug(method.getDeclaringClass().getName());
            logger.debug(method.getName());
            for (int i = 0; i < method.getParameterTypes().length; ++i) {
                logger.debug(method.getParameterTypes()[i].getName());
            }
            for (int i = 0; i < args.length; ++i) {
                logger.debug(args[i].toString());
            }
        }
        return null;//Java 真牛B，不用转空值类型
    }

    protected abstract boolean sendMsg(Object proxy, Method method,SuperMsgMulti superMsgMulti,String markName) throws IOException;

    private Class<?> getClassType(Object obj) {
        Class<?> classType = obj.getClass();

        return classType;
    }

}
