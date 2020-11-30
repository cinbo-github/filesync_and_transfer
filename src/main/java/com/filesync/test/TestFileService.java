package com.filesync.test;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: 文件传输服务端
 * @author: cinbo，chenyinbo
 * @create: 2020-10-15 11:38
 */
public class TestFileService {
    private int port;
    private static Logger logger = LoggerFactory.getLogger(TestFileService.class);

    public TestFileService(int port) {
        this.port = port;
    }

    public void start() {
        // 处理连接线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 处理io线程组
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap server = new ServerBootstrap();
        // 指定处理客户端的通道
        server.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FileReceiveServerHandler());

                    }
                });// 通道初始化
        try {
            logger.info("---------------------文件传输端口启动--------------------");
            ChannelFuture future = server.bind(port).sync();
            future.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TestFileService(8990).start();
    }
}
