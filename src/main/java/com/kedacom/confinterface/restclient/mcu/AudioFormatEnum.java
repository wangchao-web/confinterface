package com.kedacom.confinterface.restclient.mcu;

public enum AudioFormatEnum {
    G722(1), G711U(2), G711A(3), G729(4), G728(5), G7221C(6),
    MP3(7), G719(8), MPEG4_AACLC(9), MPEG4_AACLD(10), MPEG4_AACLC_STEREO(11),
    MPEG4_AACLD_STEREO(12), OPUS(13), UNKNOWN(255);

    public int getCode() {
        return code;
    }

    AudioFormatEnum(int code){
        this.code = code;
    }

    public static AudioFormatEnum resolve(int code) {
        AudioFormatEnum[] audioFormatEnums = values();
        int statusNum = audioFormatEnums.length;

        for(int index = 0; index < statusNum; ++index) {
            AudioFormatEnum audioFormatEnum = audioFormatEnums[index];
            if (audioFormatEnum.code == code) {
                return audioFormatEnum;
            }
        }

        return AudioFormatEnum.UNKNOWN;
    }

    private int code;
}
