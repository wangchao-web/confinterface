package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class JoinDiscussionGroupRequest extends BaseRequestMsg<JoinDisscussionGroupResponse> {
    public JoinDiscussionGroupRequest(String groupId, List<Terminal> mts) {
        super(groupId);
        this.mts = mts;
        this.terminalMediaResources = null;
        this.mapIsStopInspection= new ConcurrentHashMap<>();
    }

    public List<Terminal> getMts() {
        return mts;
    }

    public void setMts(List<Terminal> mts) {
        this.mts = mts;
    }

    public void setIsStopInspection(String mtE164, boolean isStopInspection){
        mapIsStopInspection.put(mtE164, isStopInspection);
    }

    public boolean isStopInspection(String mtE164){
        if (!mapIsStopInspection.containsKey(mtE164))
            return false;

        return mapIsStopInspection.get(mtE164);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        JoinDisscussionGroupResponse joinDisscussionGroupResponse = new JoinDisscussionGroupResponse(code, status.value(), message);
        if (null != terminalMediaResources && !terminalMediaResources.isEmpty()){
            joinDisscussionGroupResponse.setMtMediaResources(terminalMediaResources);
        }
        ResponseEntity<JoinDisscussionGroupResponse> responseEntity = new ResponseEntity<>(joinDisscussionGroupResponse, status);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        JoinDisscussionGroupResponse joinDisscussionGroupResponse = new JoinDisscussionGroupResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        joinDisscussionGroupResponse.setMtMediaResources(terminalMediaResources);
        ResponseEntity<JoinDisscussionGroupResponse> responseEntity = new ResponseEntity<>(joinDisscussionGroupResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    public void addTerminalMediaResource(TerminalMediaResource terminalMediaResource) {
        synchronized (this) {
            if (null == terminalMediaResources) {
                terminalMediaResources = Collections.synchronizedList(new ArrayList<>());
            }
        }

        terminalMediaResources.add(terminalMediaResource);

        if (waitMsg.isEmpty()) {
            makeSuccessResponseMsg();
            waitMsg = null;
        }
    }

    private List<Terminal> mts;
    private List<TerminalMediaResource> terminalMediaResources;
    private ConcurrentHashMap<String, Boolean>  mapIsStopInspection;
}
