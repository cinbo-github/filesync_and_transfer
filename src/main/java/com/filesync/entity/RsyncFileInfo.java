package com.filesync.entity;

import java.io.Serializable;

/**
 * 同步两端的文件交互信息
 */
public class RsyncFileInfo implements Serializable {

    /**
     * 文件同步模式,同步模式决定了以下参数哪些是可用，哪些是不可用。
     */
    private RsyncMode rsyncMode;

    //===========================================Rsync模式===================================//
    /**
     * 每次读取文件的block大小
     */
    private int blocksize;

    /**
     * 滑动检验的block大小，默认值是1024;
     */
    private int rsyncRollingCheckBlockSize = 1024;



    /**
     * 同步文件，目标文件的文件路径包含文件名
     */
    private String destFilePath;

    /**
     * 同步文件，目标文件的文件名
     */
    private String destFileName;

    private String destFileCreateTime;
    private String destFileModifyTime;


}
