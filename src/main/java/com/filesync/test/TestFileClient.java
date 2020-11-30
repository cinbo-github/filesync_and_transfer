package com.filesync.test;


import com.filesync.nettyhandler.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @description: 文件传输客户端
 * @author: cinbo，chenyinbo
 * @create: 2020-10-15 11:39
 */
public class TestFileClient {
    private String host;
    private int port;
    private static Logger logger = LoggerFactory.getLogger(TestFileClient.class);
    public TestFileClient(String host,int port) {
        this.host = host;
        this.port = port==0?8990:port;
    }
    private Channel channel;
    private NioEventLoopGroup group;
    public NioEventLoopGroup getGroup() {
        return group;
    }
    public Channel getChannel() {
        return channel;
    }
    public void start() {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        this.group = group;
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new MessageEncoder());

                    }
                });

        ChannelFuture future;
        try {
            future = bootstrap.connect(host, port).sync();
            if (future.isSuccess()) {
                logger.info("文件服务器连接成功");
                this.channel = future.channel();
                long t1 = System.currentTimeMillis();
                sendFile(this.channel);
                long t2 = System.currentTimeMillis();
                logger.info("文件传输时间："+(t2-t1));
            } else {
                logger.info("文件服务器连接失败");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

    private void sendFile(Channel channel) {

        try {
//            if (channel != null && channel.isActive()) {
//                File file = new File("D:\\tmp\\sortfilein1");
//                logger.info("my总共传输差异文件容量= " + FileSyncUtil.getDoubleValue((double) file.length() / 1024 / 1024)
//                        + "m");
//                DefaultFileRegion fileRegion = new DefaultFileRegion(file, 0, file.length());
//                FileInfo info = new FileInfo();
//                info.setFilename(file.getName());
//                info.setLength(file.length());
//                channel.writeAndFlush(info);
//                System.out.println("线程名：" + Thread.currentThread().getName());
//                channel.writeAndFlush(fileRegion).addListener(future -> {
//                    if (future.isSuccess()) {
//                        String checksum = Coder.encryptBASE64(FileSyncUtil.generateFileDigest(file));
//                        logger.info(file.getAbsolutePath() + "文件传输完成。检验码:"+checksum);
//                        // 通知服务端进行md5验证传输完整性，并进行文件合并
//                        DiffFilesSyncMsg msg = new DiffFilesSyncMsg();
//                        msg.setFileDigest(checksum);
//                        msg.setLength(file.length());
//                        msg.setFileName(file.getName());
//                        channel.writeAndFlush(msg);
//                    }
//                });
//            } else {
//                logger.error("连接文件服务器失败");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logger.info("关闭与文件传输服务端连接");
        }

    }

    public static void main(String[] args) {
        new TestFileClient("127.0.0.1",8990).start();
    }
}
