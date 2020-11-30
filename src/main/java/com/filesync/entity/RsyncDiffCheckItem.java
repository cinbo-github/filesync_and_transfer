package com.filesync.entity;

public class RsyncDiffCheckItem {

    /**
     * 顺序
     */
    private long index;

    /**
     * 是否匹配
     */
    private boolean isMatch;

    /**
     * 匹配的目标文件的index号
     */
    private long destFileBlockCheckSumIndex;

    /**
     * 如果不匹配，则自带的数据
     */
    private byte[] diffData;

}
