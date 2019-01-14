package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CtrlSilenceOrMuteRequest extends BaseRequestMsg<BaseResponseMsg> {

    public  CtrlSilenceOrMuteRequest(String groupId, String mtE164, boolean silence, SilenceOrMuteParam silenceOrMuteParam){
        super(groupId);
        this.silence = silence;
        this.mtE164 = mtE164;
        this.silenceOrMuteParam = silenceOrMuteParam;
    }

    public boolean isSilence() {
        return silence;
    }

    public String getMtE164() {
        return mtE164;
    }

    public SilenceOrMuteParam getSilenceOrMuteParam() {
        return silenceOrMuteParam;
    }

    @Override
    public void makeSuccessResponseMsg() {
        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(code, status.value(), message);
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(baseResponseMsg, status);
        responseMsg.setResult(responseEntity);
    }

    private boolean silence;
    private String mtE164;
    private SilenceOrMuteParam silenceOrMuteParam;
}
