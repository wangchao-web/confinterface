package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;


public class P2PCallRequest extends BaseRequestMsg<BaseResponseMsg> {

    public P2PCallRequest(String groupId, String account) {
        super(groupId);
        this.account = account;
    }

    public void addForwardResource(MediaResource mediaResource) {
        if (null == forwardResources)
            forwardResources = new ArrayList<>();

        forwardResources.add(mediaResource);
    }

    public void addReverseResource(MediaResource mediaResource) {
        if (null == reverseResources)
            reverseResources = new ArrayList<>();

        reverseResources.add(mediaResource);
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public List<MediaResource> getForwardResources() {
        return forwardResources;
    }

    public void setForwardResources(List<MediaResource> forwardResources) {
        this.forwardResources = forwardResources;
    }

    public List<MediaResource> getReverseResources() {
        return reverseResources;
    }

    public void setReverseResources(List<MediaResource> reverseResources) {
        this.reverseResources = reverseResources;
    }

    public boolean isSuccessResponseMsg() {
        return SuccessResponseMsg;
    }

    public void setSuccessResponseMsg(boolean successResponseMsg) {
        SuccessResponseMsg = successResponseMsg;
    }

    @Override
    public void removeMsg(String msg) {
        if (null == waitMsg) {
            return;
        }
        synchronized (this) {
            waitMsg.remove(msg);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequest, remove msg : " + msg);
            System.out.println("P2PCallRequest, remove msg: " + msg);

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequest, waitMsg.isEmpty() : " + waitMsg.isEmpty());
            System.out.println("P2PCallRequest, waitMsg.isEmpty() : " + waitMsg.isEmpty());
            if (waitMsg.isEmpty()) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequest, has no msg wait for dealing!");
                System.out.println("P2PCallRequest, has no msg wait for dealing!");
                //makeSuccessResponseMsg();
                SuccessResponseMsg = true;
                /*TerminalStatusNotify terminalStatusNotify = new TerminalStatusNotify();
                TerminalStatus terminalStatus = new TerminalStatus(account, "MT", 1, forwardResources, reverseResources);
                terminalStatusNotify.addMtStatus(terminalStatus);
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "terminalService.getE164() " + account + ",terminalService.getGroupId() : " + groupId + ", forwardResources" + forwardResources.toString() + ", reverseResources" + reverseResources.toString());
                System.out.println("terminalService.getE164() " + account + ",terminalService.getGroupId() : " + groupId + ", forwardResources" + forwardResources.toString() + ", reverseResources" + reverseResources.toString());

                System.out.println("confInterfacePublishService : "+confInterfacePublishService);
                confInterfacePublishService.publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, groupId, terminalStatusNotify);*/
            }
        }
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        /*P2PCallResponse p2PCallResponse = new P2PCallResponse(code, status.value(), message);
        ResponseEntity<P2PCallResponse> responseEntity = new ResponseEntity<>(p2PCallResponse, status);
        responseMsg.setResult(responseEntity);*/
        BaseResponseMsg p2PCallResponse = new BaseResponseMsg(code, status.value(), message);
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(p2PCallResponse, status);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        /*P2PCallResponse p2PCallResponse = new P2PCallResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        p2PCallResponse.setAccount(account);
        p2PCallResponse.setForwardResources(forwardResources);
        p2PCallResponse.setReverseResources(reverseResources);
        ResponseEntity<P2PCallResponse> responseEntity = new ResponseEntity<>(p2PCallResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);*/
        BaseResponseMsg p2PCallResponse = new BaseResponseMsg(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(p2PCallResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    private String account;
    private List<MediaResource> forwardResources;
    private List<MediaResource> reverseResources;

    public boolean SuccessResponseMsg = false;

}
