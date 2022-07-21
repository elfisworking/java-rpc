package com.deepexplore.nettychannel;

import com.deepexplore.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPCEncode extends MessageToByteEncoder {
    private static final Logger logger = LoggerFactory.getLogger(RPCDecode.class);
    private Class<?> genericClass;
    private Serializer serializer;

    public RPCEncode(Class<?> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(genericClass.isInstance(msg)) {
            try {
                byte[] bytes = serializer.serialize(msg);
                out.writeInt(bytes.length);
                out.writeBytes(bytes);
            } catch (Exception e) {
                logger.error("Encode error : " + e.toString());
            }
        }
    }
}
