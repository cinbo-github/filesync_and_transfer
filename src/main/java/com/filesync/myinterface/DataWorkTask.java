package com.filesync.myinterface;

/**
 * @description: 一个任务接口
 * @author: cinbo，chenyinbo
 * @create: 2020-10-26 09:42
 */
public interface DataWorkTask {
    /**
     *  任务名称
     */
    String taskName="null";

    /**
     * 任务类型，文件传输（包括传输和同步),hdfs,
     * @return
     */
    String getType();

    /**
     *
     */



}
