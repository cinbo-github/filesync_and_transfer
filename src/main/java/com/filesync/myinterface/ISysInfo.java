package com.filesync.myinterface;

/**
 *@description: 描述系统信息的接口，主要是用户主机和开放交互时
 *@author: cinbo，chenyinbo
 *@create: 2020-10-15 11:30
 */
public interface ISysInfo {
    /**
     * 获取系统的类型，主要是linux，windows，aix，zos
     * @return
     */
    public String getSystemType();

    /**
     * 获取系统的ip地址，主要是ipv4;
     * @return
     */
    public String getSystemIpAddress();
}