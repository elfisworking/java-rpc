package com.deepexplore.proxy;

import com.deepexplore.handler.RpcFuture;

public interface RpcService<T, P, FN extends SerializableFunction<T>> {
    RpcFuture call(String functionName, Object ... args) throws Exception;
    RpcFuture call(FN fn, Object ...args) throws  Exception;
}
