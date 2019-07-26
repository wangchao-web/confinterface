package com.kedacom.confinterface.LogService;

import java.util.concurrent.LinkedBlockingQueue;

public class LogQueue extends LinkedBlockingQueue<LogOperation> {
    private static class LogQueueHolder {
        private static final LogQueue INSTANCE = new LogQueue();
    }
    private LogQueue(){}
    private LogQueue(int queueSize) {super(queueSize);}

    private static final LogQueue getInstance(){
        return LogQueueHolder.INSTANCE;
    }

    /** Enqueue */
    public static void enqueue(LogOperation t) throws LogQueueFullException{
        try {
            getInstance().add(t);
        } catch (IllegalStateException e) {
            throw new LogQueueFullException("event queue is full");
        }
    }

    /** Dequeue */
    public static LogOperation dequeue() {
        LogOperation t = null;
        try {
            t = getInstance().take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return t;
    }

}
