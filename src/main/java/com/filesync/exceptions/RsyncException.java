package com.filesync.exceptions;

public class RsyncException extends RuntimeException {
   public RsyncException(String msg){
        super(String.format("Rsync Exception:%s",msg));
    }
    public RsyncException(Throwable e){
        super(String.format("Rsync Exception",e));
    }
}
