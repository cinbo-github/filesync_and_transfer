package com.filesync.nettyhandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @description: 传输输出时，需要转成对应的BUFFER内容
 * 如果是对应的数据，则
 * @author: cinbo，chenyinbo
 * @create: 2020-10-26 11:45
 */
public class DataTransferLayerEncoderHandler extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(msg instanceof String){

        }else{
            throw new RuntimeException("unsupport msg");
        }
    }
}
