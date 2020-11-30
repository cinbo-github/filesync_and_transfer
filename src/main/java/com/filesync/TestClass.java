package com.filesync;

import com.alibaba.fastjson.JSON;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Map;

public class TestClass {
    public void println(){
        System.out.println("打印我们的信息.");
    }
    public static void main(String[] args) throws Exception{
        Map<String,Object> attr =  Files.readAttributes(Paths.get("f:\\temp2\\"),"*");
        System.out.println(JSON.toJSONString(attr));
        BasicFileAttributeView basicView = Files.getFileAttributeView(Paths.get("f:\\temp2\\"), BasicFileAttributeView.class );
        BasicFileAttributes basicFileAttributes = basicView.readAttributes ();
        System.out.println(basicFileAttributes.getClass().getName());
        Object obj = basicFileAttributes.fileKey();
        System.out.println(JSON.toJSONString(basicFileAttributes));

        System.out.println( new Date(basicFileAttributes .creationTime ()  .toMillis ())+""+basicFileAttributes .creationTime ().toMillis ());
        System.out.println( new Date(basicFileAttributes.lastAccessTime ()  .toMillis ())+""+basicFileAttributes.lastAccessTime ()  .toMillis ());
        System.out.println( new Date(basicFileAttributes .lastModifiedTime ()  .toMillis ())+""+basicFileAttributes.lastModifiedTime ()  .toMillis ());
    }

    public static void test(){
        String fileName="d:\\tmp\\lorem2.txt";
        BufferedWriter out = null;

        try {

            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true)));
            while(true) {

                Thread.sleep(2000);
                out.write(new Date() + " 我的测试内容1\n");
                out.flush();
                break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
