package com.filesync.msg;

import java.io.Serializable;

/**
 * @description: 数据交互类型
 * @author: cinbo，chenyinbo
 * @create: 2020-10-26 13:35
 */
public class DataTransferMsg implements Serializable {
    public enum MsgType{
        /**
         * 表示这个传输使用的交互使用的MessageTtype
         */
        MESSAGE(1,"交互的是MESSAGE类型")
        /**
         * 这个主要是交互的裸数据，比如，文件传输，那么这里这个数据就是文件数据
         */
        ,APPDATA(2,"交互的是Application的原始数据");
        private int code;
        private String desc;
        private MsgType(int code,String desc){
            this.code = code;
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "MsgType{" +
                    "code=" + code +
                    ", desc='" + desc + '\'' +
                    '}';
        }
    }

    /**
     * 消息对象，
     * 如果是raw数据，那么应该是ByteBuff，
     * 如果是消息数据，那么应该是一个消息对象。
     */
    Object MsgObject;

}
