package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class StartMonitorsRequest extends BaseRequestMsg<MonitorsResponse> {
    public StartMonitorsRequest(String groupId, MonitorsParams monitorsParams) {
        super(groupId);
        this.monitorsParams = monitorsParams;
    }

    public StartMonitorsRequest(String groupId) {
        super(groupId);
    }

    public MonitorsParams getMonitorsParams() {
        return monitorsParams;
    }

    public void setMonitorsParams(MonitorsParams monitorsParams) {
        this.monitorsParams = monitorsParams;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void addMonitorsResponse(String type, String id, int mode){
        this.type = type;
        this.id = id;
        this.mode = mode;
    }
    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        MonitorsResponse monitorsResponse = new MonitorsResponse(code, status.value(), message);
        ResponseEntity<MonitorsResponse> responseEntity = new ResponseEntity<>(monitorsResponse, status);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        MonitorsResponse monitorsResponse = new MonitorsResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(),
                ConfInterfaceResult.OK.getMessage(),type , id ,mode);
        ResponseEntity<MonitorsResponse> responseEntity = new ResponseEntity<>(monitorsResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("type:").append(type)
                .append(", id:").append(id)
                .append(", mode:").append(mode)
                .toString();
    }
    private  MonitorsParams monitorsParams;
    private String type;   //资源类型，"video","audio"
    private String id;      //资源号，由流媒体返回
    private int mode; //监控类型 1-终端；2-画面合成；3-混音；
}
