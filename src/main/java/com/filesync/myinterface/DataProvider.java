package com.filesync.myinterface;

import java.io.InputStream;
import java.util.function.Function;

/**
 * @description: 数据提供者，可能是hdfs，可能是ftp，可能是file，可能是本地的文件服务，可能是文件系统,
 *
 * @author: cinbo，chenyinbo
 * @create: 2020-10-28 09:24
 */
public interface DataProvider {

    /**
     * 数据提供的类型
     * @return
     */
    String getProviderType();

    /**
     * 获取数据的流
     * @return
     */
    InputStream getDataStream(String url, String user, String pwd);

    <T> T setDataHandlerAction(Function<DataProvider, T> func);

}
