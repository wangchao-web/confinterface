package com.kedacom.confinterface.LogService;

public class LogToStdoutOperation extends LogOperation {
    public LogToStdoutOperation(LogLevelEnum logLevel, String logInfo){
        super(logLevel, logInfo);
    }

    public LogToStdoutOperation(Exception e){
        super(e);
    }

    public void writeLog() {
        System.out.println(logLevel.toString() + ":" + logInfo);
    }

    public void printExceptionStackTrace() {
        exception.printStackTrace();
    }
}
