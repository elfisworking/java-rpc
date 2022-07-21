package com.deepexplore.util;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {
    //创建线程池
    public static ThreadPoolExecutor makeServerThreadPool(final String serviceName, int corePoolSize, int maxPoolSize) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60L,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        return new Thread(runnable, "netty-rpc" + serviceName + " - " + runnable.hashCode());
                    }
                },
                new ThreadPoolExecutor.AbortPolicy());
        return threadPoolExecutor;
    }

}
