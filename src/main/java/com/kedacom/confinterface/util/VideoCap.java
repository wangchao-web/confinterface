package com.kedacom.confinterface.util;

import com.kedacom.confadapter.media.*;

public class VideoCap {

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public int getResolution() {
        return resolution;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRateType(int bitRateType) {
        this.bitRateType = bitRateType;
    }

    public int getBitRateType() {
        return bitRateType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static EncodingFormatEnum convertVideoMediaTypeToEncodingFormat(int mediaType){
        switch (mediaType) {
            case 1:
                return EncodingFormatEnum.MP4;
            case 2:
                return EncodingFormatEnum.H261;
            case 3:
                return EncodingFormatEnum.H263;
            case 4:
            case 5:
                return EncodingFormatEnum.H264;
            case 6:
                return EncodingFormatEnum.H265;
            case 7:
                return EncodingFormatEnum.H263PLUS;
            default:
                return EncodingFormatEnum.H264;
        }
    }

    public static String convertVideoMediaTypeToString(int mediaType){
        switch (mediaType) {
            case 1:
                return "MP4";
            case 2:
                return "H261";
            case 3:
                return "H263";
            case 4:
            case 5:
                return "H264";
            case 6:
                return "H265";
            case 7:
                return "H263PLUS";
            default:
                return "H264";
        }
    }

    public static String convertIntResolutionToString(int resolution){
        switch (resolution) {
            case 1:
                return VideoMediaDescription.RESOLUTION_QCIF;
            case 2:
                return VideoMediaDescription.RESOLUTION_CIF;
            case 3:
                return VideoMediaDescription.RESOLUTION_4CIF;
            case 4:
                return VideoMediaDescription.RESOLUTION_D1;
            case 5:
                return VideoMediaDescription.RESOLUTION_720P;
            case 6:
                return VideoMediaDescription.RESOLUTION_1080P;
            default:
                return VideoMediaDescription.RESOLUTION_720P;
        }
    }

    public static void constructMediaDescription(VideoCap videoCap, VideoMediaDescription videoMediaDescription){
        if (videoCap.getBitRateType() == 1)
            videoMediaDescription.setBitrateType("CBR");
        else
            videoMediaDescription.setBitrateType("VBR");

        videoMediaDescription.setBitrate(videoCap.getBitRate());
        videoMediaDescription.setFramerate(videoCap.getFrameRate());
        videoMediaDescription.setResolution(VideoCap.convertIntResolutionToString(videoCap.getResolution()));

        EncodingFormatEnum encodingFormat = VideoCap.convertVideoMediaTypeToEncodingFormat(videoCap.getMediaType());
        videoMediaDescription.setEncodingFormat(encodingFormat);
        videoMediaDescription.setPayload(encodingFormat.getFormat());

        if (videoCap.getMediaType() == 4){
            //high profile
            videoMediaDescription.getH264Desc().setProfile(ProfileEnum.HIGH);
        } else {
            videoMediaDescription.getH264Desc().setProfile(ProfileEnum.BASELINE);
        }

        videoMediaDescription.setDirection(MediaDirectionEnum.SendRecv);

        if (videoCap.getType() == 1)
            videoMediaDescription.setDual(false);
        else
            videoMediaDescription.setDual(true);
    }

    private int mediaType;
    private int resolution;
    private int frameRate;
    private int bitRate;
    private int bitRateType;
    private int type;   //1:main, 2:slides
}
