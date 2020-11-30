package com.filesync.entity;

/**
 * RsyncMode 同步的模式，有些模式是可以或的。
 */
public enum RsyncMode {
    /**
     * 文件增量追加模式，该文件只能不停变大，而且新增数据都是追加都是追加到文件末尾。
     */
    APPEND_MODE(1,"文件增量追加模式"),

    /**
     * 文件增量rsync模式，该文件可以任意改动。同步时只发送改动部分。
     */
    RSYNC_MODE(2,"文件改动增量追加模式"),

    /**
     * 文件通过覆盖传输进行同步
     */
    OVERWRITE_MODE(4,"文件同步覆盖模式，每次都是全量传输"),

    /**
     *  源的删除操作，影响目标端，目标端也会把删除动作操作。
     */
    DELETE_MODE(8,"同步时，源端的删除动作，反应到目标端");


    private int modeCode;
    private String modeDesc;

    private RsyncMode(int modeCode,String msgDesc){
        this.modeCode = modeCode;
        this.modeDesc = msgDesc;
    }


}
