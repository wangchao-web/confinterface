package com.kedacom.confinterface.LogService;


public class LogHandler implements Runnable {
    private LogOperation fetch() { return LogQueue.dequeue(); }

    private void handleLogOperation(){
        LogOperation op = this.fetch();
        if(op.exception != null) {
            op.printExceptionStackTrace();
        } else {
            op.writeLog();
        }
    }

    @Override
    public void run() {
        while(true) {
            handleLogOperation();
        }
    }
}
