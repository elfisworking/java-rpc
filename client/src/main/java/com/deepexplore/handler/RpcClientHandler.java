package com.deepexplore.handler;

import com.deepexplore.nettychannel.RPCRequest;
import com.deepexplore.nettychannel.RPCResponse;
import com.deepexplore.protocol.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClientHandler extends SimpleChannelInboundHandler<RPCResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

    private ConcurrentHashMap<String, RpcFuture> pendingRPC = new ConcurrentHashMap<>();

    private volatile Channel channel;

    private SocketAddress remotePeer;

    private RpcProtocol rpcProtocol;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponse msg) throws Exception {

    }
}
