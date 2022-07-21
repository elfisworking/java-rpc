package com.deepexplore.handler;

import com.deepexplore.nettychannel.*;
import com.deepexplore.serializer.Serializer;
import com.deepexplore.serializer.protostuff.ProtostuffSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        Serializer serializer = ProtostuffSerializer.class.newInstance();
        ChannelPipeline cp = ch.pipeline();
        cp.addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS));
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast(new RPCDecode(RPCRequest.class, serializer));
        cp.addLast(new RPCEncode(RPCResponse.class, serializer));
        cp.addLast(new RpcClientHandler());
    }
}
