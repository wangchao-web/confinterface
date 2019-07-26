package com.kedacom.confinterface.LogService;

public class LogQueueFullException extends Exception {
    public LogQueueFullException(String exceptionInfo) {
        super(exceptionInfo);
    }
}
