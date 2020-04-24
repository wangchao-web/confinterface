package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

public class GetVmpsRequest extends BaseRequestMsg<GetVmpsInfoResponse> {
    public GetVmpsRequest(String groupId) {
        super(groupId);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public int getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(int broadcast) {
        this.broadcast = broadcast;
    }

    public int getVoiceHint() {
        return voiceHint;
    }

    public void setVoiceHint(int voiceHint) {
        this.voiceHint = voiceHint;
    }

    public int getShowMtName() {
        return showMtName;
    }

    public void setShowMtName(int showMtName) {
        this.showMtName = showMtName;
    }

    public MtNameStyle getMtNameStyle() {
        return mtNameStyle;
    }

    public void setMtNameStyle(MtNameStyle mtNameStyle) {
        this.mtNameStyle = mtNameStyle;
    }

    public ArrayList<VmpsMembersInfo> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<VmpsMembersInfo> members) {
        this.members = members;
    }

    public SingleChannelPollInfo getPoll() {
        return poll;
    }

    public void setPoll(SingleChannelPollInfo poll) {
        this.poll = poll;
    }

    public void getVmpsInfo(int mode, int layout ,int broadcast,int voiceHint, int showMtName , MtNameStyle mtNameStyle){
        this.mode = mode;
        this.layout = layout;
        this.broadcast = broadcast;
        this.voiceHint = voiceHint;
        this.showMtName = showMtName;
        this.mtNameStyle = mtNameStyle;
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        GetVmpsInfoResponse getVmpsInfoResponse = new GetVmpsInfoResponse(code, status.value(), message);
        ResponseEntity<GetVmpsInfoResponse> responseEntity = new ResponseEntity<>(getVmpsInfoResponse, status);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        GetVmpsInfoResponse getVmpsInfoResponse = new GetVmpsInfoResponse(ConfInterfaceResult.OK.getCode(),
                HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage(),mode,layout,broadcast,voiceHint,showMtName,mtNameStyle,members);
        if(mode ==3 || mode ==4){
            getVmpsInfoResponse.setPoll(poll);
        }
        ResponseEntity<GetVmpsInfoResponse> responseEntity = new ResponseEntity<>(getVmpsInfoResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("mode:").append(mode)
                .append(", layout:").append(layout)
                .append(", broadcast:").append(broadcast)
                .append(", voiceHint:").append(voiceHint)
                .append(", showMtName:").append(showMtName)
                .append(", mtNameStyle:").append(mtNameStyle.toString())
                .append(", members:").append(members.toString())
                .append(", poll:").append(poll.toString())
                .toString();
    }

    private int mode; // 1定制画面合成2-自动画面合成；3-自动画面合成批量轮询；4-定制画面合成批量轮询；
    private int layout; // 画面合成风格
    private int broadcast; //是否广播 0-否；1-是；
    private int voiceHint; //是否识别声音来源 0-否；1-是
    private int showMtName; //是否显示别名 0-否；1-是；
    private MtNameStyle mtNameStyle; //画面合成台标参数
    private ArrayList<VmpsMembersInfo> members = new ArrayList<>(); //画面合成成员数组
    private SingleChannelPollInfo poll; //画面合成轮询信息,仅mode为3、4时有效

}
