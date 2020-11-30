package com.filesync.localsrv;

import com.filesync.nettyhandler.MessageEncoder;
import com.filesync.test.FileClient;
import com.filesync.test.FileReceiveHandler;
import com.filesync.test.TelnetClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @description: 连接本地服务器
 * @author: cinbo，chenyinbo
 * @create: 2020-11-03 17:01
 */
public class DataConnectClient {
    private static Logger logger = LoggerFactory.getLogger(FileClient.class);
    public void start() {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new LineBasedFrameDecoder(8192),
                                new StringDecoder(),
                                new StringEncoder(),
                                new TelnetClientHandler());

                    }
                });

        ChannelFuture future;
        try {
            future = bootstrap.connect("127.0.0.1",50000).sync();
            if (future.isSuccess()) {
                logger.info("文件服务器连接成功");
                //ByteBuf tmpbuf = UnpooledByteBufAllocator.DEFAULT.buffer(100);
                //tmpbuf.writeBytes("d:\\tmp\\sortfilein1\n".getBytes());
                future.channel().writeAndFlush("this is first message.\n");
                InputStream in;
                InputStreamReader isr = new InputStreamReader(System.in);
                BufferedReader reader = new BufferedReader(isr);
                while(true){
                    System.out.print("client===>");
                    String inputMsg = reader.readLine();
                    future.channel().writeAndFlush(inputMsg+"\n");
                    if(inputMsg.equalsIgnoreCase("quit") || inputMsg.equalsIgnoreCase("bye")){
                        break;
                    }
                }
                future.channel().close().sync();
                System.out.println("退出程序!");
            } else {
                logger.info("文件服务器连接失败");
            }
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }
    public static void main(String[] args) {
        new DataConnectClient().start();
    }
}
