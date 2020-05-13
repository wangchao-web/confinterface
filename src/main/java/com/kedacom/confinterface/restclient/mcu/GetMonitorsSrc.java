package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class GetMonitorsSrc {

    private int  type; //监控类型1-终端；2-画面合成；3-混音；
    private String  mt_id; //源终端id
    private MonitorsEncrypt encrypt; //加密信息
    private MonitorsPayloadFormats payload_formats;
    private MonitorsMediaFormats media_formats;
    private MonitorsRtcp rtcp;
}
