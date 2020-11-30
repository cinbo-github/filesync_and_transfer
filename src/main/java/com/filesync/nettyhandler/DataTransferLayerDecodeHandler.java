package com.filesync.nettyhandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @description: 作为传输底层的handler,
 * 接受的数据，分为几种类型
 * 消息类型：即表示这个消息是message，类型以及相关数据。
 * 数据类型，即表示这个消息是raw数据。raw数据是什么内容，是根据之前的消息交互得出的。
 * @author: cinbo，chenyinbo
 * @create: 2020-10-26 11:25
 */
public class DataTransferLayerDecodeHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    }


}
