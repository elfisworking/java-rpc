package com.deepexplore.core;

import com.deepexplore.nettychannel.Beat;
import com.deepexplore.nettychannel.RPCRequest;
import com.deepexplore.nettychannel.RPCResponse;
import com.deepexplore.util.ServiceUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import net.sf.cglib.reflect.FastClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcServerHandler extends SimpleChannelInboundHandler<RPCRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
    private final Map<String, Object> handlerMap;
    private final ThreadPoolExecutor serverHandlerPool;

    public RpcServerHandler(Map<String, Object> handlerMap, ThreadPoolExecutor serverHandlerPool) {
        this.handlerMap = handlerMap;
        this.serverHandlerPool = serverHandlerPool;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCRequest msg) throws Exception {
        if(Beat.BEAT_ID.equalsIgnoreCase(msg.getRequestId())) {
            logger.info("Server receive heart beat");
            return;
        }
        serverHandlerPool.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("Receive request " + msg.getRequestId());
                RPCResponse response = new RPCResponse();
                response.setRequestId(msg.getRequestId());
                try {
                    Object res = handle(msg);
                    response.setResult(res);
                } catch (InvocationTargetException e) {
                    response.setError(e.toString());
                    logger.error("RPC server handle request error", e.toString());
                }
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.info("Send response for request" + response.getRequestId());
                    }
                });
            }
        });
    }

    private Object handle(RPCRequest rpcRequest) throws InvocationTargetException {
        String className = rpcRequest.getClassName();
        String version = rpcRequest.getVersion();
        String serviceKey = ServiceUtil.makeServiceKey(className, version);
        Object serviceBean = handlerMap.get(serviceKey);
        if(serviceBean == null) {
            logger.error("Can not find service implement with interface name: {} nad version: {}", className, version);
            return null;
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = rpcRequest.getMethodName();
        Object[] parameters = rpcRequest.getParameters();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        logger.debug(serviceClass.getName());
        logger.debug(methodName);
        FastClass fastClass = FastClass.create(serviceClass);
        int index = fastClass.getIndex(methodName, parameterTypes);
        return fastClass.invoke(index, parameterTypes, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Server caught exception: " + cause.getMessage());
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            ctx.channel().close();
            logger.warn("Channel idle in last {} seconds, close it", Beat.BEAT_TIMEOUT);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
