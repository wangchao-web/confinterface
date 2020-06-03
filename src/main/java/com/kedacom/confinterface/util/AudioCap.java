package com.kedacom.confinterface.util;

import com.kedacom.confadapter.media.AudioMediaDescription;
import com.kedacom.confadapter.media.EncodingFormatEnum;
import com.kedacom.confadapter.media.MediaDescription;
import com.kedacom.confadapter.media.MediaDirectionEnum;

public class AudioCap {

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setChannelNum(int channelNum) {
        this.channelNum = channelNum;
    }

    public int getChannelNum() {
        return channelNum;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static EncodingFormatEnum convertAudioMediaTypeToEncodingFormat(int mediaType){
        switch (mediaType){
            case 1:
                return EncodingFormatEnum.PCMA;
            case 2:
                return EncodingFormatEnum.PCMU;
            case 3:
                return EncodingFormatEnum.G7231;
            case 4:
                return EncodingFormatEnum.G729;
            case 5:
                return EncodingFormatEnum.G7221;
            case 6:
                return EncodingFormatEnum.G7221C;
            case 7:
                return EncodingFormatEnum.OPUS;
            default:
                return EncodingFormatEnum.PCMA;
        }
    }

    public static String convertAudioMediaTypeToString(int mediaType){
        switch (mediaType){
            case 1:
                return "PCMA";
            case 2:
                return "PCMU";
            case 3:
                return "G7231";
            case 4:
                return "G729";
            case 5:
                return "G7221";
            case 6:
                return "G7221C";
            case 7:
                return "OPUS";
            default:
                return "PCMA";
        }
    }

    public static void constructAudioMediaDescription(AudioCap audioCap, AudioMediaDescription audioMediaDescription){
        EncodingFormatEnum encodingFormatEnum = AudioCap.convertAudioMediaTypeToEncodingFormat(audioCap.getMediaType());
        audioMediaDescription.setEncodingFormat(encodingFormatEnum);
        audioMediaDescription.setPayload(encodingFormatEnum.getFormat());
        audioMediaDescription.setChannelNum(audioCap.getChannelNum());
        audioMediaDescription.setBitrate(audioCap.getBitRate());
        audioMediaDescription.setSampleRate(audioCap.getSampleRate());
        audioMediaDescription.setDirection(MediaDirectionEnum.SendRecv);

        if (audioCap.getType() == 1)
            audioMediaDescription.setDual(false);
        else
            audioMediaDescription.setDual(true);
    }

    private int mediaType;
    private int bitRate;
    private int sampleRate;
    private int channelNum;
    private int type;  //1:main, 2:slides
}
