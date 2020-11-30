package com.filesync;

public class TestJniMain {

    public native static void HelloWord();

    public native static String cToJava();

    static{
        System.loadLibrary( "MyNative" );

    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.library.path"));
        HelloWord();
        System.out.println(cToJava());

    }
}
