package com.wiqer.rpc.impl.core;

import com.wiqer.rpc.serialize.JsonSerializer;
import com.wiqer.rpc.serialize.SerializerInterface;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

@Data
public abstract class MsgControllersMap {
    public SerializerInterface serializer  ;

    public String packageName ;
    public String FullName ;
    public String interfaceFullName ;

    public String version;

    public String className ;
    //这里改成了ConcurrentHashMap，因为C#版不习惯自带的map，所以自己写的一个map
    public ConcurrentHashMap<String , MsgFun> linkMap;
    public MsgControllersMap() {
        serializer = JsonSerializer.getSerializer();
        linkMap = new ConcurrentHashMap<String, MsgFun>();
    }

    protected MsgFun get(String key)
    {
        return linkMap.get(key);
    }

    protected boolean isEmpty()
    {
        return linkMap.isEmpty();
    }

    protected Collection<String> keys()
    {
        return linkMap.keySet();
    }

    protected MsgFun put(String key, MsgFun value)
    {
        return linkMap.put(key,  value);
    }
    protected int Size()
    {
        return linkMap.size();
    }

    protected Collection<MsgFun> values()
    {
        return linkMap.values();
    }
    protected void clear()
    {
        linkMap.clear();
    }

     protected abstract void getMathsInfoMulti();


}
