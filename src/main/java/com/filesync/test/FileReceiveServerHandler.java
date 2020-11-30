package com.filesync.test;


import com.filesync.util.Coder;
import com.filesync.util.FileSyncUtil;
import com.filesync.util.FileUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;

public class FileReceiveServerHandler extends ChannelInboundHandlerAdapter {
	private static Logger logger = LoggerFactory.getLogger(FileReceiveServerHandler.class);
	private long start;
	private RandomAccessFile randomAccessFile=null;
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		System.out.println("----------------active-------------");
		start = 0;
	}
	private long fileLen;
	private String fileName; 
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf byteBuf = (ByteBuf) msg;
		if (FileSyncUtil.STAERT_FLAG == byteBuf.getInt(0)) {
			byteBuf.readInt();
			int nameLen = byteBuf.readInt();
			byte[] b = new byte[nameLen];
			byteBuf.readBytes(b);
			fileName = new String(b);
			fileLen = byteBuf.readLong();
			logger.info(fileLen+" "+new String(b));
			logger.info("fileName"+fileName);
		}
		if (fileName==null) {
			logger.error("无法获取同步包文件名"+fileName);
			return ;
		}
		if(randomAccessFile == null) {
			File file = new File("d:\\tmp\\tmp\\" + fileName);
			randomAccessFile = new RandomAccessFile(file, "rw");
		}
		int length = byteBuf.readableBytes();
		byte[] bytes = new byte[length];
		byteBuf.readBytes(bytes);
		randomAccessFile.write(bytes);// 调用了seek（start）方法，是指把文件的记录指针定位到start字节的位置。也就是说程序将从start字节开始写数据
		byteBuf.release();
		if (start>=fileLen) {
			logger.info("文件接收完成");
			File file = new File("d:\\tmp\\tmp\\" + fileName);
			String digest = Coder.encryptBASE64(FileSyncUtil.generateFileDigest(file));
			FileUtil.countDown(digest);
			ctx.close();
		}
	}



	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		randomAccessFile.close();
		System.out.println("----------------channelInactive-------------");
	}
}
