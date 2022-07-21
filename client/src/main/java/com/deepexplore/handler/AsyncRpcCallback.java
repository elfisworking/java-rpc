package com.deepexplore.handler;

public interface AsyncRpcCallback {
    void success(Object result);
    void fail(Exception e);
}
