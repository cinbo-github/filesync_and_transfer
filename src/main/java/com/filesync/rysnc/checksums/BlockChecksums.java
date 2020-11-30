package com.filesync.rysnc.checksums;


import com.filesync.exceptions.RsyncException;
import org.apache.commons.codec.binary.Hex;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件块校验
 * 
 * @author jiuyuehe
 *
 */
public class BlockChecksums implements Serializable{
	private static final long serialVersionUID = 6881512258509956424L;
	private int index;
	private long offset;
	private long size;
	private long weakChecksum;
	private byte[] strongChecksum = null;
	private byte[] dataPtr=null;

	public BlockChecksums(byte[] buf, long offset, long size) {
		this.offset = offset;
		this.size = size;
		this.weakChecksum = generateWeakChecksum(buf,0,(int)size);
		this.dataPtr = buf;
		//this.strongChecksum = generateStrongChecksum(buf,0,(int)size);
	}

	public byte[] getDataPtr(){
		return dataPtr;
	}
	public BlockChecksums(byte[] buf,int csum,long offset, long size , byte c1, byte c2){
		this.offset = offset;
		this.size = size;
		this.weakChecksum = simpleChecksum(csum,(int)size,c1,c2);
		this.dataPtr = buf;
	}
	private long simpleChecksum(int csum,int size,byte c1, byte c2){
		int s1, s2;
		s1 = csum & 0xffff;
		s2 = csum >> 16;
		s1 = s1-(c1-c2);
		s2 = s2-(size*c1 - s1);
		return  (long)((s1 & 0xffff)|(s2<<16))&0xffffffffL;
	}
	public BlockChecksums(int index,byte[] buf, long offset, long size) {
		this.index = index;
		this.offset = offset;
		this.size = size;
		this.weakChecksum = generateWeakChecksum(buf,0,(int)size);
		this.strongChecksum = generateStrongChecksum(buf,0,(int)size);
	}
	
	/**
	 * md5 校验
	 * @param buf
	 * @return
	 */
	private byte[] generateStrongChecksum(byte[] buf,int offset,int len) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(buf,offset,len);
			return messageDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RsyncException(e);
		}
	}
	

	
	/**
	 * adler32 校验
	 * @param buf
	 * @return
	 */
	private long generateWeakChecksum(byte[] buf, int offset , int length ) {
		return get_checksum1(buf,length);
	}
	private int CHAR_OFFSET = 0;
	private long get_checksum1(byte[] buf, int len)
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
	
	
	
	
	
	
	
	
	
	
	
	

	public long getOffset() {
		return offset;
	}

	public long getSize() {
		return size;
	}

	public long getWeakChecksum() {
		return weakChecksum;
	}

	public byte[] getStrongChecksum() {
		return strongChecksum;
	}

	public String getHexStrongChecksum() {
		if(strongChecksum == null){
			strongChecksum = generateStrongChecksum(dataPtr,0,(int)size);
		}
		return new String(Hex.encodeHex(strongChecksum));
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "offset: " + offset + " size: " + size + " weak sum: "
				+ weakChecksum + " strong sum: " + getHexStrongChecksum();
	}
}
