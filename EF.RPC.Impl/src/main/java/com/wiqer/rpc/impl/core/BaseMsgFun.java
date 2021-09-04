package com.wiqer.rpc.impl.core;

import com.wiqer.rpc.serialize.SuperMsgMulti;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BaseMsgFun extends MsgFun {

    //分散储存减少压力

    public ConcurrentHashMap<String, CountDownLatch> uscd;//= new ConcurrentDictionary<int, UnsafeSynchronizer>();
    public ConcurrentHashMap<String, SuperMsgMulti> msgcd;//= new ConcurrentDictionary<int, UnsafeSynchronizer>();

    public void initUnsafeSynchronizer() {
        uscd = new ConcurrentHashMap<String, CountDownLatch>();
        msgcd = new ConcurrentHashMap<String, SuperMsgMulti>();
    }
    public void setMsg(SuperMsgMulti superMsgMulti) {
        msgcd.get(superMsgMulti.getId());
    }
    public SuperMsgMulti getAndRemoveMsg( SuperMsgMulti superMsgMulti)
    {
        return msgcd.remove(superMsgMulti.getId());
    }
    public void acquire(String id) throws InterruptedException {
        CountDownLatch unsafeSynchronizer = new CountDownLatch(1);
        uscd.put(id, unsafeSynchronizer).await(10, TimeUnit.SECONDS);
    }
    public Boolean release(String id) {
        CountDownLatch unsafeSynchronizer = null;
        try
        {

            uscd.get(id);
            if (null != unsafeSynchronizer)
            {
                 unsafeSynchronizer.countDown();
                return true;

            }
            else
            {
                return false;
            }
        }
        finally {
            uscd.remove(id, unsafeSynchronizer);
        }
    }
}
