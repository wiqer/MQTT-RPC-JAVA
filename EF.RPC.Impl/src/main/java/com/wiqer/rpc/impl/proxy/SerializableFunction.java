package com.wiqer.rpc.impl.proxy;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

public interface SerializableFunction<T> extends Serializable {
    default String getName() throws Exception {
        Method write = this.getClass().getDeclaredMethod("writeReplace");
        write.setAccessible(true);
        SerializedLambda serializedLambda = (SerializedLambda) write.invoke(this);
        return serializedLambda.getImplMethodName();
    }
}
