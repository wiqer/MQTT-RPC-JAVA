package com.wiqer.rpc.impl.core;

import com.wiqer.rpc.impl.util.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public abstract class BaseServer  extends Server {
    private static final Logger logger = LoggerFactory.getLogger(BaseServer.class);
    private Thread thread;
    protected String serverAddress;
    private int corePoolSize=16;
    private int maxPoolSize=32;
    private String serviceName=BaseServer.class.getSimpleName();
    protected Map<String, Object> serviceMap = new HashMap<>();


    public BaseServer(String serverAddress ) {
        this.serverAddress = serverAddress;
    }

    public void addService(String interfaceName, String version, Object serviceBean) {
        logger.info("Adding service, interface: {}, version: {}, bean：{}", interfaceName, version, serviceBean);
        String serviceKey = ServiceUtil.makeServiceKey(interfaceName, version);
        serviceMap.put(serviceKey, serviceBean);
    }

    public void start() {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                serverRun();
            }
        });
        thread.start();
    }

    public void stop() {
        // destroy server thread
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
    public abstract void serverRun();
}
