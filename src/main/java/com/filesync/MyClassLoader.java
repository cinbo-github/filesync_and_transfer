package com.filesync;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MyClassLoader extends ClassLoader{

    private String classLoaderName;
    private String fileExtension = ".class";

    /**
     * 默认的情况下，自定义类的加载器会以SystemClassLoader为父类加载器，如果要改变这种机制，调第二种生成方法
     */

    public MyClassLoader(String classLoaderName) {
        super();
        this.classLoaderName = classLoaderName;
    }

    public MyClassLoader(ClassLoader classLoader, String classLoaderName) {
        super(classLoader);
        this.classLoaderName = classLoaderName;
    }

    /**
     * 该方法会在底层调用
     */
    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        byte[] data = this.loadClassData(className);
        return this.defineClass(className, data, 0, data.length);
    }

    // 在该示例里，不会执行该方法，也就是说，由于双亲委托机制，会由应用类加载器加载
    // 如果加载的类，不在classpath里，意思就是应用类加载器加载不了，才会由此加载器加载
    private byte[] loadClassData(String name) {

        byte[] data = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;

        try {
            this.classLoaderName = this.classLoaderName.replace(".", "/");

            is = new FileInputStream(new File(name + this.fileExtension));
            baos = new ByteArrayOutputStream();

            int ch = 0;

            while (-1 != (ch = is.read())) {
                baos.write(ch);
            }
            data = baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return data;
    }

    public static void test(ClassLoader classLoader) throws Exception {
        Class<?> clazz = classLoader.loadClass("com.cinbo.filesync.TestClass1");
        TestClass o = (TestClass)clazz.newInstance();
        o.println();
        System.out.println(o);
    }

    public static void main(String[] args) throws Exception {
        MyClassLoader loader1 = new MyClassLoader("loader1");
        Thread.currentThread().setContextClassLoader(loader1);
        test(loader1);
    }
}
