package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.restclient.mcu.MtVideos;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetVideoSourceRequest extends BaseRequestMsg<VideoSourceResponse> {

    public GetVideoSourceRequest(String groupId , String account) {
        super(groupId);
        this.account = account;
    }

    public int getCurVideoIdx() {
        return curVideoIdx;
    }

    public void setCurVideoIdx(int curVideoIdx) {
        this.curVideoIdx = curVideoIdx;
    }

    public List<MtVideos> getMtVideos() {
        return mtVideos;
    }

    public void setMtVideos(List<MtVideos> mtVideos) {
        this.mtVideos = mtVideos;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        VideoSourceResponse videoSourceResponse = new VideoSourceResponse(code, status.value(), message);
        ResponseEntity<VideoSourceResponse> responseResponseEntity = new ResponseEntity<>(videoSourceResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        VideoSourceResponse videoSourceResponse = new VideoSourceResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        videoSourceResponse.setCurVideoIdx(curVideoIdx);
        videoSourceResponse.setMtVideos(mtVideos);
        ResponseEntity<VideoSourceResponse> responseResponseEntity = new ResponseEntity<>(videoSourceResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    private int curVideoIdx;  //当前终端的视频源通道号

    private List<MtVideos> mtVideos; //终端视频源数组，最多返回10个视频源

    private String account;
}
