package com.kedacom.confinterface.util;

import javax.management.Query;

public enum ConfInterfaceResult {
    OK(0, "Ok"),
    NOT_SUPPORT_METHOD(1, "not support this method!"),
    CREATE_CONFERENCE(50000, "create conference failed!"),
    ADD_TERMINAL_INTO_CONFERENCE(50001, "add terminal into conference failed!"),
    GROUP_NOT_EXIST(50002, "group not exist!"),
    LEFT_CONFERENCE(50003, "del terminal from conference failed!"),
    NO_FREE_VMT(50004, "reach max interface capacity!!"),
    INSPECTION(50005, "inspection terminal failed!"),
    CANCEL_INSPECTION(50006, "cancel inspection terminal failed!"),
    CONTROL_CAMERA(50007, "control camera failed!"),
    REACH_MAX_JOIN_MTS(50008, "reach max join terminal numbers!"),
    SET_SPEAKER(50009, "set speaker failed!"),
    CANCEL_SPEAKER(50010, "cancel speaker failed!"),
    OFFLINE(50011, "terminal is offline!"),
    INVALID_PARAM(50012, "invalid param!"),
    INSPECTION_OTHER_TERMINAL(50013, "inspect other terminal!"),
    SENDIFRAME(50014, "send i frame!"),
    TERMINAL_NOT_EXIST(50015, "terminal not exist in this conference!"),
    CTRL_VOLUME(50016, "control terminal volume failed!"),
    CTRL_SILENCE_OR_MUTE(50017, "control silence or mute failed!"),
    VMT_NOT_ONLINE(50018, "no vmt online, please wait a minute!"),
    CTRL_DUALSTREAM(50019, "control dual stream failed!"),
    ADD_EXCHANGENODE_FAILED(50020, "add exchange node failed!"),
    UPDATE_EXCHANGENODE_FAILED(50021, "update exchange node failed!"),
    EXIST_DUALSTREAM(50022, "exist dual stream!"),
    MAINSTREAM_NOT_EXIST(50026,"mainstream doesn't exist"),
    CLOSE_CHANNEL(50023, "close logical channel failed!"),
    P2PCALL(50024, "p2p call failed!"),
    P2PCANCELCALL(50025, "p2p cancelCallMt failed!"),
    TERMINALHASBEENCALLED(50026,"The terminal has been called"),
    QUERY_CONFS_INFO_IS_NULL(50027,"query confs info is null"),
    QUERY_CONFS_CASCADES_INFO_IS_NULL(50028,"query confs cascades info is null"),
    CONF_NOT_EXIT(50029,"confid not exit"),
    QUERY_CONFS_CASCADES_MT_INFO_IS_NULL(50030,"query confs cascades mt info is null"),
    SEND_SMS_FAILED(50031,"send message failed");

    public int getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }

    ConfInterfaceResult(int code, String message){
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;
}
