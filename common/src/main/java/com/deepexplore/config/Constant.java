package com.deepexplore.config;

public interface Constant {
    // ZK is abbreviation of zookeeper
    int ZK_SESSION_TIMEOUT = 5000;
    int ZK_CONNECTION_TIMEOUT = 5000;
    // zookeeper namespace
    String ZK_NAMESPACE = "netty-rpc";
    // zookeeper registry path
    String ZK_REGISTRY_PATH = "/registry";
    // zookeeper data path
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
}
