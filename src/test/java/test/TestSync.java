package test;


import com.filesync.rysnc.checksums.BlockChecksums;
import com.filesync.rysnc.checksums.DiffCheckItem;
import com.filesync.rysnc.checksums.FileChecksums;
import com.filesync.rysnc.checksums.RollingChecksum;
import com.filesync.rysnc.util.Constants;
import com.filesync.rysnc.util.RsyncFileUtils;
import org.junit.Test;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Adler32;

public class TestSync {
	@Test
	public void testCheckSum() throws Exception {
//		File srcFile = new File("C:\\Users\\lz578\\Desktop\\worldwind.jar");
//		File updateFile = new File("D:\\server\\apache-tomcat-8.0.36-2\\webapps\\SMS\\com\\sunsheen\\jfids\\demo\\worldwind\\worldwind.jar");
//		System.out.println(QuickMD5.getFileMD5Buffer(srcFile));
//		System.out.println(RsyncFileUtils.checkFileSame(updateFile, srcFile));
	}
	@Test
	public void testRolling() throws Exception {

		File srcFile = new File("f:\\temp\\a.xls");
		File updateFile = new File("f:\\temp\\b.xls");
		File tmp = new File("f:\\temp\\1.txt_tmp");
		File newFile = new File("f:\\temp\\1.txt_new");
		long t1 = System.currentTimeMillis();
		if (!tmp.exists()) {
			tmp.createNewFile();
		}
		List<DiffCheckItem> dciList = roll(srcFile, updateFile);

		long t2 = System.currentTimeMillis();

		System.out.println("滚动计算： spend time :" + (long) (t2 - t1) + "ms");
		RsyncFileUtils.createRsyncFile(dciList, tmp, Constants.BLOCK_SIZE);

		System.out.println("实际需要传输的大小  :" + tmp.length() + " byte ");

		long t3 = System.currentTimeMillis();

		System.out.println("生成临时文件，耗时 :" + (long) (t3 - t2) + "ms");

		RsyncFileUtils.combineRsyncFile(srcFile, newFile, tmp);

		long t4 = System.currentTimeMillis();

		System.out.println("合并文件  耗时:" + (long) (t4 - t3) + "ms");

		System.out.println("all spend time :" + (long) (t4 - t1) + "ms");

		System.out.println(RsyncFileUtils.checkFileSame(updateFile, newFile));
	}

	private List<DiffCheckItem> roll(File srcFile, File updateFile) {

		long t1 = System.currentTimeMillis();
		FileChecksums fc = new FileChecksums(srcFile);
		long t2 = System.currentTimeMillis();
		System.out.println("读取文件的检验码:"+(t2-t1));
		List<DiffCheckItem> diffList = new ArrayList<DiffCheckItem>();

		RollingChecksum rck = new RollingChecksum(fc, updateFile, diffList);

		rck.rolling();

		return diffList;
	}


	@Test
	public void checksum(){
		String str = "12345";
		String str1 = "23456";
		BlockChecksums blk1 = new BlockChecksums(str.getBytes(), 0, 5);
		BlockChecksums blk2 = new BlockChecksums(str1.getBytes(), 0, 5);

		BlockChecksums blk3 = new BlockChecksums(str1.getBytes(), (int)blk1.getWeakChecksum(),0,5, (byte)'1',(byte)'6');

		System.out.println(blk1.getWeakChecksum());
		System.out.println(blk2.getWeakChecksum());
		System.out.println(blk3.getWeakChecksum());
	}
	@Test
	public void checksum1() throws  Exception{
		File srcFile = new File("d:\\tmp\\lorem.txt");
		File updateFile = new File("d:\\tmp\\lorem2.txt");
		byte[] buf1 = new byte[Constants.BLOCK_SIZE];
		RandomAccessFile raf1 = new RandomAccessFile(srcFile, "r");
		int re1 = raf1.read(buf1, 0, Constants.BLOCK_SIZE);
		raf1.close();


		byte[] buf2 = new byte[Constants.BLOCK_SIZE];
		RandomAccessFile raf2 = new RandomAccessFile(updateFile, "r");
		int re2 = raf2.read(buf2, 0, Constants.BLOCK_SIZE);
		raf2.close();

		byte[] buf3 = new byte[Constants.BLOCK_SIZE];
		RandomAccessFile raf3 = new RandomAccessFile(updateFile, "r");
		raf3.seek(1);
		int re3 = raf3.read(buf3, 0, Constants.BLOCK_SIZE);
		raf3.close();



		BlockChecksums blk1 = new BlockChecksums(buf1, 0, re1);
		long t0 = System.currentTimeMillis();
		BlockChecksums blk2 = new BlockChecksums(buf2, 0, re2);
		long t1 = System.currentTimeMillis();
		BlockChecksums blk3 = new BlockChecksums(buf3, (int)blk2.getWeakChecksum(),0,re2, buf2[0],buf3[re3-1]);
		BlockChecksums blk4 = new BlockChecksums(buf3, 0, re3);
		long t2 = System.currentTimeMillis();
		System.out.println(t1-t0);
		System.out.println(t2-t1);

		System.out.println(blk1.getWeakChecksum());
		System.out.println(blk2.getWeakChecksum());
		System.out.println(blk3.getWeakChecksum());
		System.out.println(blk4.getWeakChecksum());
	}

	@Test
	public void testchecksum3(){
		String mytest = "123456789";
		Adler32 adler32 = new Adler32();
		adler32.update(mytest.getBytes(),0,mytest.getBytes().length);
		System.out.println(adler32.getValue());

		System.out.println(get_checksum1(mytest.getBytes(),mytest.getBytes().length));


	}
	int CHAR_OFFSET = 0;
	long get_checksum1(byte[] buf, int len)
	{
		int i=0;
		int s1, s2;

		s1 = s2 = 0;
		for (i = 0; i < (len-4); i+=4) {
			s2 += 4*(s1 + buf[i]) + 3*buf[i+1] + 2*buf[i+2] + buf[i+3] + 10*CHAR_OFFSET;
			s1 += (buf[i+0] + buf[i+1] + buf[i+2] + buf[i+3] + 4*CHAR_OFFSET);
		}
		for (; i < len; i++) {
			s1 += (buf[i]+CHAR_OFFSET); s2 += s1;
		}
		return (long)((s1 & 0xffff) + (s2 << 16))&0xffffffffL;
	}


	private long simpleChecksum(int csum,int size,byte c1, byte c2){
		int s1, s2, s11, s22;
		s1 = csum & 0xffff;
		s2 = csum >> 16;
		s1 = s1-(c1-c2);
		s2 = s2-(size*c1 - s1);
		return (long)((s1 & 0xffff)|(s2<<16))&0xffffffffL;
	}

	@Test
	public void checksum11() throws  Exception{
		File srcFile = new File("d:\\tmp\\lorem.txt");
		File updateFile = new File("d:\\tmp\\lorem2.txt");
		byte[] buf1 = new byte[Constants.BLOCK_SIZE];
		RandomAccessFile raf1 = new RandomAccessFile(srcFile, "r");
		int re1 = raf1.read(buf1, 0, Constants.BLOCK_SIZE);
		raf1.close();


		byte[] buf2 = new byte[Constants.BLOCK_SIZE];
		RandomAccessFile raf2 = new RandomAccessFile(updateFile, "r");
		int re2 = raf2.read(buf2, 0, Constants.BLOCK_SIZE);
		raf2.close();

		byte[] buf3 = new byte[Constants.BLOCK_SIZE];
		RandomAccessFile raf3 = new RandomAccessFile(updateFile, "r");
		raf3.seek(1);
		int re3 = raf3.read(buf3, 0, Constants.BLOCK_SIZE);
		raf3.close();

		System.out.println(get_checksum1(buf1,re1));
		long chk1 = get_checksum1(buf2,re2);
		System.out.println(chk1);

		System.out.println(simpleChecksum((int)chk1,re2,buf2[0],buf3[re3-1]));


	}

}
