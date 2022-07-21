package com.deepexplore.registry;

import com.deepexplore.config.Constant;
import com.deepexplore.core.Server;
import com.deepexplore.protocol.RpcProtocol;
import com.deepexplore.protocol.RpcServiceInfo;
import com.deepexplore.util.ServiceUtil;
import com.deepexplore.zookeeper.CuratorClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private CuratorClient client;

    private List<String> pathList = new ArrayList<>();

    public ServiceRegistry(String registryAddress) {
        this.client = new CuratorClient(registryAddress, 5000);
    }

    public void registerService(String host, int port, Map<String, Object> serviceMap) {
        List<RpcServiceInfo> serviceInfoList = new ArrayList<>();
        for(String key : serviceMap.keySet()) {
            String[] serviceInfo = key.split(ServiceUtil.SERVICE_CONCAT_TOKEN);
            if(serviceInfo.length > 0) {
                RpcServiceInfo rpcServiceInfo = new RpcServiceInfo();
                rpcServiceInfo.setServiceName(serviceInfo[0]);
                if(serviceInfo.length == 2) {
                    rpcServiceInfo.setVersion(serviceInfo[1]);
                } else {
                    rpcServiceInfo.setVersion("");
                }
                logger.info("Register new service {}", key);
                serviceInfoList.add(rpcServiceInfo);
            } else {
                logger.warn("Can not get service name and version {}", key);
            }
        }
        try {
            RpcProtocol rpcProtocol = new RpcProtocol();
            rpcProtocol.setHost(host);
            rpcProtocol.setPort(port);
            rpcProtocol.setServiceInfoList(serviceInfoList);
            String serviceData = rpcProtocol.toJson();
            byte[] bytes = serviceData.getBytes();
            String path = Constant.ZK_DATA_PATH + '-' + rpcProtocol.hashCode();
            String pathData = this.client.createPathData(path, bytes);
            pathList.add(path);
            logger.info("Register {} new service, host:{}, port:{}", serviceInfoList.size(), host, port);
        } catch (Exception e) {
            logger.error("Register service failed, exception: {}", e.getMessage());
        }
        client.addConnectionStateListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if(newState == ConnectionState.RECONNECTED) {
                    logger.info("Connection state: {}, register  service after reconnected,", newState);
                    registerService(host, port, serviceMap);
                }
            }
        });
    }

    public void unregisterService() {
        logger.info("Unregister all service");
        for(String path : pathList) {
            try {
                client.deletePath(path);
            } catch (Exception e) {
                logger.error("Delete service path error: {}", e.getMessage());
            }
        }
        client.close();
    }
}
