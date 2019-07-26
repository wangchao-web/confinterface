package com.kedacom.confinterface.LogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTools {
    public static Logger logger = LoggerFactory.getLogger(LogTools.class);

    private LogTools() {}

    private static void doLog(LogOutputTypeEnum type,LogLevelEnum level, String logInfo) {
        LogOperation op = null;
        switch (type) {
            case LOG_OUTPUT_TYPE_STDOUT:
                op = new LogToStdoutOperation(level, logInfo);
                break;
            case LOG_OUTPUT_TYPE_FILE:
                op = new LogToFileOperation(level, logInfo);
                break;
            default:
                System.out.println("Wrong Log Output Type");
                break;
        }
        try {
            LogQueue.enqueue(op);
        } catch (LogQueueFullException e) {
            System.out.println("Log Queue is full");
        }
    }

    public static void debug(LogOutputTypeEnum type,String logInfo) {
        doLog(type, LogLevelEnum.LOG_LEVEL_DEBUG, logInfo);
    }

    public static void info(LogOutputTypeEnum type, String logInfo) {
        doLog(type, LogLevelEnum.LOG_LEVEL_INFO, logInfo);
    }

    public static void warn(LogOutputTypeEnum type,String logInfo) {
        doLog(type, LogLevelEnum.LOG_LEVEL_WARN, logInfo);
    }

    public static void error(LogOutputTypeEnum type,String logInfo) {
        doLog(type, LogLevelEnum.LOG_LEVEL_ERROR, logInfo);
    }

    public static void error(LogOutputTypeEnum type,Exception e) {
        LogOperation op = null;
        switch (type) {
            case LOG_OUTPUT_TYPE_STDOUT:
                op = new LogToStdoutOperation(e);
                break;
            case LOG_OUTPUT_TYPE_FILE:
                op = new LogToFileOperation(e);
                break;
            default:
                System.out.println("Wrong Log Output Type");
                break;
        }
        try {
            LogQueue.enqueue(op);
        } catch (LogQueueFullException ee) {
            System.out.println("Log Queue is full");
        }
    }
}
