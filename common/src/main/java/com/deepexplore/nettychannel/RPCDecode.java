package com.deepexplore.nettychannel;

import com.deepexplore.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RPCDecode extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(RPCDecode.class);
    private Class<?> genericClass;
    private Serializer serializer;

    public RPCDecode(Class<?> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if(in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object obj = null;
        try {
            obj = serializer.deserialize(data, genericClass);
            out.add(obj);
        } catch (Exception e) {
            logger.debug("Decode error:" + e.toString());
        }
    }
}
