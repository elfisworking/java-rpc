package com.deepexplore.handler;

import com.deepexplore.nettychannel.RPCRequest;
import com.deepexplore.nettychannel.RPCResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

// 用于接收异步的返回结果
public class RpcFuture implements Future<Object> {
    private static final Logger logger = LoggerFactory.getLogger(RpcFuture.class);
    private Sync sync;
    private RPCRequest request;
    private RPCResponse response;
    private long startTime;
    private long responseTimeThreshold = 5000;
    private List<AsyncRpcCallback> pendingCallbacks = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();

    public RpcFuture(RPCRequest request) {
        this.sync = new Sync();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }



    @Override
    public boolean cancel(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(1);
        if(this.response != null) {
            return response.getResult();
        }
        return null;
    }

    @Override
    public Object get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(1, timeUnit.toNanos(l));
        if(success) {
            if(this.response != null) {
                return response.getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception, Request id: " + this.request.getRequestId()
                                        +  ".Request class name: "  + this.request.getClassName()
                                        +  ".Request method: " + this.request.getMethodName());
        }

    }

    public void done(RPCResponse response) {
        this.response = response; // 这个处理很不优雅
        sync.release(1); // why ?
        invokeCallbacks();
        long responseTime = System.currentTimeMillis() - startTime;
        if(responseTime > this.responseTimeThreshold) {
            logger.warn("Service response time is too slow. Request id = {}. Response time = {} ms", response.getResult(), responseTime);
        }
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRpcCallback callback:
                 pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    public RpcFuture addCallback(AsyncRpcCallback callback) {
        lock.lock();
        try {
            if(isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);

            }
        } finally {
            lock.unlock();
        }
        return this; // ? ? ?
    }

    private void runCallback(AsyncRpcCallback callback) {
        final RPCResponse res = this.response;
        // TODO
    }



    // https://www.cnblogs.com/yufeng218/p/13090453.html
    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1L;
        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;
        }
        // 我觉得这里可能写的有问题
        @Override
        protected boolean tryRelease(int arg) {
            if(getState() == pending) {
                if(compareAndSetState(pending, done)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
        protected boolean isDone() {return getState() == done;}


    }
}
