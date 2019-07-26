package com.kedacom.confinterface.LogService;

public abstract class LogOperation {
    protected LogLevelEnum logLevel;
    protected String logInfo;
    protected Exception exception;

    public LogOperation(LogLevelEnum logLevel, String logInfo){
        this.logLevel = logLevel;
        this.logInfo = logInfo;
    }

    public LogOperation(Exception exception) {
        this.exception= exception;
    }

    public void writeLog() {}

    public void printExceptionStackTrace() {}
}
