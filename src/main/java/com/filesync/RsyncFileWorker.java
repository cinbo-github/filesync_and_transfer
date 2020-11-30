package com.filesync;


import com.filesync.entity.RsyncBlockCheckSum;
import org.apache.commons.codec.binary.Hex;

import java.io.EOFException;
import java.io.File;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RsyncFileWorker {


    private static int BLOCKSIZE =1024*1024*16;
    private static RandomAccessFile srcFile;
    private static RandomAccessFile outputFile;

    private static int remainder=0;
    private static int offsetInside=0;
    private static MessageDigest messageDigest;

    private static byte[] header = null;

    /**
     * 这个标志非常重要，匹配的时候，一旦有差异就需要设置false;
     * 当匹配到下一个匹配的时候，如果mathflag是false，就需要把之前的数据都写入到文件。
     */
    private static boolean matchFlag=true;
    private static int diffBytes=0;

    /**
     * 专门用于写入差异数据长度的位置
     */
    private static long diffBytesOffset=0;


    private static boolean fileMismatchFlag=false;


    private static int numberMatch=0;


    private static int totalfilelength=0;



    private static String srcFilePath="f:\\temp\\b.xls";
    private static String destFilePath="f:\\temp\\a.xls";

    private static String afterSyncFile = "f:\\temp\\aftersync.data.xls";
    private static String syncDataFile = "f:\\temp\\rsync.data";


    public static void myfunc() throws Exception{

        byte[] buff = new byte[RsyncConstants.BLKSIZE];

        try {
             messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }

        //目标文件的 检验和
        long t1 = System.currentTimeMillis();
        List<RsyncBlockCheckSum> destSrcChecksums = new ArrayList<>();
        //RandomAccessFile rf = new RandomAccessFile(new File("d:\\tmp\\a.pptx"), "r");
        RandomAccessFile rf = new RandomAccessFile(new File(destFilePath), "r");
        int re = 0;
        int order=0;
        int offset=0;
        do{
            re = rf.read(buff);
            if(re>0){
                RsyncBlockCheckSum  rsyncBlockCheckSum = new RsyncBlockCheckSum();
                rsyncBlockCheckSum.setOffset(offset);
                rsyncBlockCheckSum.setBlkSize(re);
                rsyncBlockCheckSum.setIndex(order++);
                rsyncBlockCheckSum.setWeakChecksum(RsyncUtil.get_checksum1(buff,re));
                messageDigest.update(buff,0,re);
                rsyncBlockCheckSum.setStrongChecksum(messageDigest.digest());
                messageDigest.reset();
                destSrcChecksums.add(rsyncBlockCheckSum);
            }
            if(re>0 && re<buff.length){
                remainder = re;
            }
            offset += re;
        }while(re>0);

        Map<Long,List<RsyncBlockCheckSum>> sum1HashMap = convert2sum1HashMap(destSrcChecksums);
        rf.close();


        long t2 = System.currentTimeMillis();
        System.out.println("生成检验和字符串时间："+(t2-t1));


        //开始从源头对比同步目标的文件的内容，产生差异内容。
        //差异内容，目前测试阶段，输出格式为1个字节表示匹配和不培培，
        //                            8个字节表示，目标端，检验和的序列块号。
        //                            8个字节表示差异长度，如果匹配则没有这个数据。
        //                            如果匹配则没有这个数据，上4个字节表示的byte长度。，然后继续下一组内容。

        // so，匹配，就是 ‘#’+0x01+8字节匹配序列号。
        //     不匹配，就是 '#'+0x00+8字节长度+长度文件
        //srcFile = new RandomAccessFile(new File("d:\\tmp\\b.pptx"), "r");
        srcFile = new RandomAccessFile(new File(srcFilePath), "r");

        if(new File(syncDataFile).exists())
        {
            new File(syncDataFile).delete();
        }
        outputFile = new RandomAccessFile(new File(syncDataFile), "rw");

        byte[] buff2 = new byte[BLOCKSIZE];

        int leftbyteoffset=0;
        int buff2Offset = 0;

        int len = BLOCKSIZE;
        t1 = System.currentTimeMillis();
        do{
            len = BLOCKSIZE - buff2Offset;
            re = srcFile.read(buff2,buff2Offset,len);

            int currentBlksize = buff2Offset+re;
            if(re>0 && (currentBlksize)>= BLOCKSIZE){
                //表示后面可能还有更多的数据。读取了一个完整的buff2 数据块。

                leftbyteoffset = search_match_sum1(buff2,BLOCKSIZE,sum1HashMap);
                int leftBytesLength = (currentBlksize) - leftbyteoffset;
                byte[] leftBytes = new byte[leftBytesLength];

                System.arraycopy(buff2,leftbyteoffset, leftBytes,0,leftBytesLength);

                System.arraycopy(leftBytes,0,buff2,0,leftBytesLength);

                buff2Offset =leftBytesLength;

            } else if(re > 0 && (currentBlksize) < BLOCKSIZE){
                //表示后面没有数据了。
                leftbyteoffset = search_match_sum1(buff2,buff2Offset+re,sum1HashMap);

                if(leftbyteoffset>=0 & leftbyteoffset<currentBlksize){

                    if(matchFlag) {
                        outputFile.writeByte('#');
                        outputFile.writeByte(0x00);
                        outputFile.writeLong(currentBlksize - leftbyteoffset);
                        totalfilelength += (currentBlksize - leftbyteoffset);
                        outputFile.write(buff2, leftbyteoffset, currentBlksize - leftbyteoffset);
                        outputFile.writeByte('#');

                    }else{
                        int tmpdifflength = currentBlksize - leftbyteoffset;
                        outputFile.write(buff2, leftbyteoffset, tmpdifflength);
                        long endpos = outputFile.getFilePointer();
                        diffBytes += tmpdifflength;
                        outputFile.seek(diffBytesOffset);
                        outputFile.writeLong(diffBytes);
                        totalfilelength += tmpdifflength;
                        outputFile.seek(endpos);
                        outputFile.writeByte('#');
                    }
                }

                break;

            }else{
                //没数据了。
            }
        }while(true);
        t2 = System.currentTimeMillis();
        System.out.println("滚动校验时间："+(t2-t1));
        System.out.println("number match times:"+numberMatch);
        System.out.println("目标文件长度："+totalfilelength);
        if(fileMismatchFlag){
            System.out.println("文件有差异");
        }else{
            System.out.println("文件没有差异");
        }

        outputFile.close();
        srcFile.close();


        combineDeltaDataInto(destSrcChecksums);




//        long t1 = System.currentTimeMillis();
//       for(int i=0;i<1024*1024*10-2048;i++) {
//
//            long csum = RsyncUtil.get_checksum1(buff, i, RsyncConstants.BLKSIZE);
//
//            long csum1 = RsyncUtil.get_checksum1(buff, i+1, RsyncConstants.BLKSIZE);
//
//            long csum2 = RsyncUtil.simpleChecksum((int) csum, RsyncConstants.BLKSIZE, buff[i], buff[i+RsyncConstants.BLKSIZE]);
//
//            if(csum1 != csum2 ){
//                System.out.println(csum);
//                System.out.println(csum1);
//                System.out.println(csum2);
//            }
//        }
//        long t2 = System.currentTimeMillis();
//        System.out.println(t2-t1);














    }

    private static void combineDeltaDataInto(List<RsyncBlockCheckSum> destSrcChecksums) throws Exception{
        RandomAccessFile syncDataFile = new RandomAccessFile(RsyncFileWorker.syncDataFile, "r");
        RandomAccessFile destDataFile = new RandomAccessFile(destFilePath, "r");
        if(new File(afterSyncFile).exists()){
            new File(afterSyncFile).delete();
        }
        RandomAccessFile afterSyncDataFile = new RandomAccessFile(afterSyncFile, "rw");
        int i=0;
        byte flag;
        byte[]  tmpHeader = new byte[1];
        while(true){
             try {
                 flag = syncDataFile.readByte();
             }catch (Exception e){
                 if(e instanceof EOFException){
                     break;
                 }
                 throw  e;
             }
//             if(i>499){
//                 System.out.println("Ok");
//             }
//             boolean isOk=true;
//             for(int j=0;j< 1;j++){
//                 if(tmpHeader[j] != (byte)'#'){
//                     isOk = false;
//                     break;
//                 }
//             }
             if(flag != (byte)'#'){
                 System.out.println("第几个："+i);
                 System.out.println(syncDataFile.getFilePointer());
                 throw new RuntimeException("错误的头，第一个不是'#'号");
             }
             byte isMatch = syncDataFile.readByte();
             if(isMatch == 0x01){
                 //匹配
                 long index = syncDataFile.readLong();
                 flag =  syncDataFile.readByte();
                 assert flag == (byte)'#';
                 RsyncBlockCheckSum rsyncBlockCheckSum = destSrcChecksums.get((int)index);
                 destDataFile.seek(rsyncBlockCheckSum.getOffset());
                 byte[] tmpData = new byte[rsyncBlockCheckSum.getBlkSize()];
                 destDataFile.read(tmpData);
                 afterSyncDataFile.write(tmpData);


             }else if(isMatch == 0x00){
                 //不匹配
                 long diffBytesLength = syncDataFile.readLong();
                 byte[] diffData = new byte[(int)diffBytesLength];
                 int re = syncDataFile.read(diffData);
                 flag =  syncDataFile.readByte();
                 assert flag == (byte)'#';
                 if(re != diffData.length){
                     System.out.println("ok");
                 }
                 afterSyncDataFile.write(diffData);
             }else{
                 System.out.println("第几个："+i);
                 throw new RuntimeException("错误的头，匹配位不是0x00，也不是0x01");
             }
             i++;
        }
        syncDataFile.close();
        destDataFile.close();
        afterSyncDataFile.close();

//        File tmpFile = new File(destFilePath);
//        if( tmpFile.exists()){
//            tmpFile.delete();
//            System.out.println(destFilePath+" deleted");
//        }
//        new File(afterSyncFile).renameTo(new File(destFilePath));
        
    }

    private static int search_match_sum2(byte[] buff2, int i, Map<Long, List<RsyncBlockCheckSum>> sum1HashMap) {
        return 0;
    }

    /**
     *  返回 检测到的位置
     * @param buff2
     * @param blocksize
     * @param sum1HashMap
     * @return
     */
    private static int search_match_sum1(byte[] buff2, int blocksize, Map<Long, List<RsyncBlockCheckSum>> sum1HashMap) throws Exception{
        offsetInside=0;
        int diffStart=0;

        long checkSum = 0;
        do {

            if( (blocksize - offsetInside) < remainder){
                if(matchFlag){
                    //上一次是匹配的，则直接写入差异的数据来
                }else{

                    outputFile.write(buff2, diffStart, offsetInside - diffStart);
                    diffBytes += (offsetInside - diffStart);
                    totalfilelength += diffBytes;

                }

                return  offsetInside;
            }

//            if(totalfilelength>offsetInside){
//                System.out.println("输出的文件大于源文件了");
//            }

            if(numberMatch>499){
                int a=0;
                a++;
            }

            /**
             * 检查sum1
             */
            int blen =  (blocksize-offsetInside)>RsyncConstants.BLKSIZE?RsyncConstants.BLKSIZE:remainder;
            if(matchFlag){
                checkSum = RsyncUtil.get_checksum1(buff2, offsetInside, blen);
            }else {
                if (checkSum == 0) {
                    checkSum = RsyncUtil.get_checksum1(buff2, offsetInside, blen);
                } else {
                    checkSum = RsyncUtil.simpleChecksum((int) checkSum, blen, buff2[offsetInside - 1], buff2[offsetInside + blen - 1]);
                }
            }

//            if(offsetInside>13330000){
//                System.out.println("ok");
//            }

            /**
             * sum1相同
             */
            if(sum1HashMap.containsKey(checkSum)){
                messageDigest.update(buff2,offsetInside,blen);
                String srcHexString = String.valueOf(Hex.encodeHex(messageDigest.digest()));
                boolean isMatch=false;
                if(sum1HashMap.get(checkSum).size()>1){
                    System.out.println("sum1 size bigger than 1");
                }
                for(RsyncBlockCheckSum rsyncBlockCheckSum:sum1HashMap.get(checkSum)){
                    //里面的 md5相同。

                    if(rsyncBlockCheckSum.getHexStrongChecksum().equals(srcHexString)){

                        if(rsyncBlockCheckSum.getBlkSize()!=blen){
                            System.out.println("不匹配的记录长度");
                        }
                        isMatch = true;
                        /**
                         * 先检查匹配标志，如果是 true,表示上一次也是匹配的
                         */
                        if(matchFlag){
                            //继续match
                            offsetInside += blen;
                            outputFile.writeByte('#');
                            outputFile.writeByte(0x01);
                            outputFile.writeLong(rsyncBlockCheckSum.getIndex());
                            totalfilelength += rsyncBlockCheckSum.getBlkSize();
                            outputFile.writeByte('#');

                            numberMatch++;


                        }
                        else{
                            //设置为匹配，同时将数据写入进去。
                            matchFlag = true;
                            //把不同的数据写入
                            //
                            outputFile.write(buff2,diffStart,offsetInside-diffStart);
                            long endpos = outputFile.getFilePointer();

                            //写入差异数据长度
                            diffBytes += offsetInside - diffStart;
                            totalfilelength += offsetInside - diffStart;
                            outputFile.seek(diffBytesOffset);
                            outputFile.writeLong(diffBytes);

                            diffBytes = 0;
                            diffBytesOffset = -1;

                            //设置到文件末尾
                            outputFile.seek(endpos);
                            outputFile.writeByte('#');

                            offsetInside += blen;
                            outputFile.writeByte('#');
                            outputFile.writeByte(0x01);
                            outputFile.writeLong(rsyncBlockCheckSum.getIndex());
                            totalfilelength += rsyncBlockCheckSum.getBlkSize();
                            outputFile.writeByte('#');
                            numberMatch++;

                        }

                    }

                }
                if(!isMatch){

                    if(matchFlag) {
                        matchFlag = false;
                        diffStart = offsetInside;
                        outputFile.writeByte('#');
                        outputFile.writeByte(0x00);
                        diffBytesOffset = outputFile.getFilePointer();
                        outputFile.writeLong(0);
                        numberMatch++;
                    }
                    offsetInside++;
                }
            }
            else{
                if(matchFlag) {
                    matchFlag = false;
                    diffStart = offsetInside;

                    outputFile.writeByte('#');
                    outputFile.writeByte(0x00);
                    diffBytesOffset = outputFile.getFilePointer();
                    outputFile.writeLong(0);
                    numberMatch++;
                }
                offsetInside++;
            }

            if(!fileMismatchFlag){
                if(!matchFlag){
                    fileMismatchFlag = true;
                }
            }
        }while(true);

    }

    private static Map<Long, List<RsyncBlockCheckSum>> convert2sum1HashMap(List<RsyncBlockCheckSum> destSrcChecksums) {
        Map<Long,List<RsyncBlockCheckSum>> sum1HashMap = new HashMap<>();
        for(RsyncBlockCheckSum rsyncBlockCheckSum:destSrcChecksums){
            if(sum1HashMap.get(rsyncBlockCheckSum) == null) {
                List<RsyncBlockCheckSum> sum1List = new ArrayList<>();
                sum1List.add(rsyncBlockCheckSum);
                sum1HashMap.put(rsyncBlockCheckSum.getWeakChecksum(),sum1List);
            }else{
                sum1HashMap.get(rsyncBlockCheckSum).add(rsyncBlockCheckSum);
            }

        }
        return sum1HashMap;
    }
}
