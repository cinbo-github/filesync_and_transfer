package com.filesync;


import com.filesync.exceptions.RsyncException;

public class RsyncUtil {



    private static int CHAR_OFFSET = 0;
    public static long get_checksum1(byte[] buf, int offset, int len)
    {
        if(buf.length <= offset ){
            throw new RsyncException("指定的offset长度大于 buf的长度.");
        }

        int i;
        int s1, s2;

        s1 = s2 = 0;
        for (i = offset; i < (len + offset -4); i+=4) {
            s2 += 4*(s1 + buf[i]) + 3*buf[i+1] + 2*buf[i+2] + buf[i+3] + 10*CHAR_OFFSET;
            s1 += (buf[i+0] + buf[i+1] + buf[i+2] + buf[i+3] + 4*CHAR_OFFSET);
        }
        for (; i < len + offset; i++) {
            s1 += (buf[i]+CHAR_OFFSET); s2 += s1;
        }
        return (long)((s1 & 0xffff) + (s2 << 16))&0xffffffffL;
    }

    public static long get_checksum1(byte[] buf, int len)
    {


        int i;
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

    public static long simpleChecksum(int csum,int size,byte c1, byte c2){
        int s1, s2;
        s1 = csum & 0xffff;
        s2 = csum >> 16;
        s1 = s1-(c1-c2);
        s2 = s2-(size*c1 - s1);
        return  (long)((s1 & 0xffff)|(s2<<16))&0xffffffffL;
    }

}
