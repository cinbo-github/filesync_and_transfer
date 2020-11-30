package com.filesync.entity;

import org.apache.commons.codec.binary.Hex;

public class RsyncBlockCheckSum {
    private long index;
    private long offset;
    private int  blkSize;
    private long weakChecksum;
    private byte[] strongChecksum = null;


    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getBlkSize() {
        return blkSize;
    }

    public void setBlkSize(int blkSize) {
        this.blkSize = blkSize;
    }

    public long getWeakChecksum() {
        return weakChecksum;
    }

    public void setWeakChecksum(long weakChecksum) {
        this.weakChecksum = weakChecksum;
    }

    public byte[] getStrongChecksum() {
        return strongChecksum;
    }

    public void setStrongChecksum(byte[] strongChecksum) {
        this.strongChecksum = strongChecksum;
    }

    public String getHexStrongChecksum() {
        return new String(Hex.encodeHex(strongChecksum));
    }
}
