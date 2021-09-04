package com.wiqer.rpc.impl;

import com.wiqer.rpc.impl.annotation.EFRpcService;
import com.wiqer.rpc.impl.core.BaseServer;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public abstract class RpcServer extends BaseServer implements ApplicationContextAware, InitializingBean, DisposableBean {
    public RpcServer(String serverAddress) {
        super(serverAddress);
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(EFRpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                EFRpcService rpcService = serviceBean.getClass().getAnnotation(EFRpcService.class);
                String interfaceName = rpcService.strategyType().getName();
                String version = rpcService.version();
                super.addService(interfaceName, version, serviceBean);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.start();
    }

    @Override
    public void destroy() {
        super.stop();
    }
}
