package com.pyy6.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;

public class FastDfsTest{
    public static void main(String[] args) throws IOException, MyException {

        //配置fdfs的全局信息（tracker的绑定）
        String file = FastDfsTest.class.getClassLoader().getResource("tracker.conf").getFile();
        ClientGlobal.init(file);
        //获得tracker
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = trackerClient.getConnection();
        //通过tracker获得storage
        StorageClient storageClient = new StorageClient(connection, null);
        //通过storage上传文件
        String[] jpgs = storageClient.upload_file("C:/Users/22291/Desktop/证件照1.jpg", "jpg", null);
        String url = "http://192.168.198.128";
        for(String jpg:jpgs){
            url = url+"/"+jpg;
        }
        System.out.println(url);
    }
}
