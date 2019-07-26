package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseRequestMsg<T> {

    public BaseRequestMsg(String groupId) {
        super();
        this.groupId = groupId;
        this.responseMsg = new DeferredResult<>();
        this.waitMsg = null;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public DeferredResult<ResponseEntity<T>> getResponseMsg() {
        return responseMsg;
    }

    public List<String> getWaitMsg() {
        return waitMsg;
    }

    public void setWaitMsg(List<String> waitMsg) {
        this.waitMsg = waitMsg;
    }

    public void addWaitMsg(String msg) {
        synchronized (this) {
            if (null == waitMsg) {
                waitMsg = Collections.synchronizedList(new ArrayList<>());
            }
        }

        waitMsg.add(msg);
    }

    public void removeMsg(String msg) {
        synchronized (this) {
            if (null == waitMsg) {
                return;
            }
            waitMsg.remove(msg);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"正在移除removeMsg");
            System.out.println("正在移除removeMsg");
        }
    }

    public abstract void makeErrorResponseMsg(int code, HttpStatus status, String message);

    public abstract void makeSuccessResponseMsg();

    protected String groupId;
    protected DeferredResult<ResponseEntity<T>> responseMsg;
    protected List<String> waitMsg;
}
