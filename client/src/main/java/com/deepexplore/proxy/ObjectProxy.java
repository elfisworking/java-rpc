package com.deepexplore.proxy;

import com.deepexplore.handler.RpcFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ObjectProxy<T, P> implements InvocationHandler, RpcService<T, P, SerializableFunction<T>> {
    private static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);
    private Class<T> cls;
    private String version;
    public ObjectProxy(Class<T> cls, String version) {
        this.cls = cls;
        this.version = version;
    }
    @Override
    public RpcFuture call(String functionName, Object... args) throws Exception {
        return null;
    }

    @Override
    public RpcFuture call(SerializableFunction<T> tSerializableFunction, Object... args) throws Exception {
        return null;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        return null;
    }
}
