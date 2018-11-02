package com.beidahuang.register;

import org.apache.zookeeper.KeeperException;

public interface IRegisterCenter {

    /**
     *
     * @param serviceName 注册的接口名称，包名+类名
     * @param url 服务的url地址
     */
    public  void register(String serviceName, String url) throws KeeperException, InterruptedException;
}
