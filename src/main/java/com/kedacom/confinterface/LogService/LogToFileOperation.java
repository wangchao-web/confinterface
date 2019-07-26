package com.kedacom.confinterface.LogService;

public class LogToFileOperation extends LogOperation{
    public LogToFileOperation(LogLevelEnum logLevel, String logInfo){
        super(logLevel, logInfo);
    }

    public LogToFileOperation(Exception exception) {
        super(exception);
    }

    public void writeLog() {
        switch(logLevel) {
            case LOG_LEVEL_ERROR:
                LogTools.logger.error(logInfo);
                break;
            case LOG_LEVEL_WARN:
                LogTools.logger.warn(logInfo);
                break;
            case LOG_LEVEL_INFO:
                LogTools.logger.info(logInfo);
                break;
            case LOG_LEVEL_DEBUG:
                LogTools.logger.debug(logInfo);
                break;
            default:
                LogTools.logger.error("Wrong Log Level!");
                break;
        }
    }

    public void printExceptionStackTrace() {
        LogTools.logger.error(exception.getMessage(),exception);
    }
}
