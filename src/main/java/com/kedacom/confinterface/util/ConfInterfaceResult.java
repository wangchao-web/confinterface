package com.kedacom.confinterface.util;

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
    P2P_CALL(50024, "p2p call failed!"),
    P2P_CANCEL_CALL(50025, "p2p cancelCallMt failed!"),
    TERMINAL_HAS_BEEN_CALLED(50026,"The terminal has been called"),
    ACCOUNT_E164_INVALID(50027, "Unused GK , Use E164 call failed"),
    QUERY_CONFS_INFO_IS_NULL(50028,"query confs  info is null"),
    CONF_NOT_EXIT(50029,"confid not exit"),
    QUERY_CONFS_CASCADES_MT_INFO_IS_NULL(50030,"query confs cascades mt info is null"),
    SEND_SMS_FAILED(50031,"send message failed"),
    GET_MT_INFO_FAILED(50032,"get mt info failed"),
    OPERATE_VMPS(50033,"operate vmps failed"),
    END_VMPS(50034,"end vmps failed!"),
    START_MIXS(50035,"start mixs failed !"),
    OPERATE_MIXS_MEMBERS_FAILED(50036,"operate mixs members failed !"),
    END_MIXS(50037,"end mixs failed !"),
    START_MONITORS(50038,"start monitors failed"),
    GET_VMPS_INFO_FAILED(50039,"get vmps info failed"),
    GET_MIXS_INFO_FAILED(50040,"get mixs info failed"),
    MONITORS_MEMBERS_NOT_EXIST(50041,"monitors Members not exist"),
    END_MONITORS(50042,"end monitors failed"),
    QUERY_CONFS_CASCADES_INFO_IS_NULL(50043,"query confs cascades  info is null"),
    QUERY_CONF_INFO_IS_NULL(50044,"query conf  info is null");

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
