package com.kedacom.confinterface.sip;

import com.kedacom.confadapter.common.RemoteParticipantInfo;
import com.kedacom.confadapter.media.*;
import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dto.BaseRequestMsg;
import com.kedacom.confinterface.dto.P2PAudioCallMediaCap;
import com.kedacom.confinterface.dto.P2PVideoCallMediaCap;
import com.kedacom.confinterface.dto.TerminalMediaResource;
import com.kedacom.confinterface.exchange.CreateResourceParam;
import com.kedacom.confinterface.exchange.CreateResourceResponse;
import com.kedacom.confinterface.inner.TerminalOfflineReasonEnum;
import com.kedacom.confinterface.inner.TransportAddress;
import com.kedacom.confinterface.service.TerminalService;
import com.kedacom.confinterface.util.AudioCap;
import com.kedacom.confinterface.util.VideoCap;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class SipTerminalService extends TerminalService {

    public SipTerminalService(String e164, String name, boolean bVmt, SipProtocalConfig sipProtocalConfig){
        super(e164, name, bVmt);
        this.sipProtocalConfig = sipProtocalConfig;
    }

    @Override
    public boolean closeDualStreamChannel() {
        return false;
    }

    @Override
    public boolean onOpenLogicalChannel(Vector<MediaDescription> mediaDescriptions) {

        List<String> resourceInfo = isNeedCreateResource(false, mediaDescriptions);

        if (resourceInfo.isEmpty()){
            //向流媒体发起请求创建资源节点
            for (MediaDescription mediaDescription : mediaDescriptions) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, mediaDescription:" + mediaDescription.toString());
                System.out.println("SIP, mediaDescription:" + mediaDescription.toString());

                CreateResourceParam createResourceParam = new CreateResourceParam();
                createResourceParam.setSdp(constructSdp(mediaDescription));
                CreateResourceResponse resourceResponse = addExchange(createResourceParam);
                if (null == resourceResponse)
                    return false;

                resourceInfo.add(resourceResponse.getResourceID());
                addMediaResource(mediaDescription.getStreamIndex(), mediaDescription.getDual(), resourceResponse);
            }
        }

        boolean bOk = ackOpenLogicalChannel(mediaDescriptions);
        if (!bOk) {
            //通道打开失败,释放流媒体资源
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, ack channel opening failed" + resourceInfo.toString());
            System.out.println("SIP, ack channel opening failed" + resourceInfo.toString());
            removeMediaResource(false, resourceInfo);
            resourceInfo.clear();
            return false;
        }

        resourceInfo.clear();
        return true;
    }

    @Override
    public void openDualStreamChannel(BaseRequestMsg startDualStreamRequest) {

    }

    @Override
    public TerminalOfflineReasonEnum callRemote(RemoteParticipantInfo remoteParticipantInfo, P2PVideoCallMediaCap videoCodec, P2PAudioCallMediaCap audioCodec) {
        boolean bOK;

        Vector<MediaDescription> mediaDescriptions = new Vector<>();
        VideoMediaDescription videoMediaDescription = new VideoMediaDescription();
        if(null == videoCodec){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"SIP, callRemote, videoCodec is null, read config file!");
            System.out.println("SIP, callRemote, videoCodec is null, read config file!");

            VideoCap videoCap = sipProtocalConfig.getBaseSysConfig().getVideoCapSetList().get(0);
            VideoCap.constructMediaDescription(videoCap, videoMediaDescription);
        } else {

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"callMt, videoCodec.getCodeFormat() : " + videoCodec.getCodecFormat() + " ,videoCodec.getResolution()" + videoCodec.getResolution() + " ,videoCodec.getBitrate() :" + videoCodec.getBitrate()+ " ,videoCodec.getFramerate()" + videoCodec.getFramerate());
            System.out.println("callMt, videoCodec.getCodeFormat() : " + videoCodec.getCodecFormat() + " ,videoCodec.getResolution()" + videoCodec.getResolution() + " ,videoCodec.getBitrate() :" + videoCodec.getBitrate() + " ,videoCodec.getFramerate()" + videoCodec.getFramerate());

            EncodingFormatEnum encodingFormatEnum = EncodingFormatEnum.FromName(videoCodec.getCodecFormat());
            videoMediaDescription.setEncodingFormat(encodingFormatEnum);
            videoMediaDescription.setPayload(encodingFormatEnum.getFormat());
            videoMediaDescription.setResolution(videoCodec.getResolution());
            videoMediaDescription.setBitrate(videoCodec.getBitrate());
            videoMediaDescription.setFramerate(videoCodec.getFramerate());
        }

        videoMediaDescription.setDirection(MediaDirectionEnum.SendRecv);
        videoMediaDescription.setDual(false);
        videoMediaDescription.setStreamIndex(0);
        mediaDescriptions.add(videoMediaDescription);

        AudioMediaDescription audioMediaDescription = new AudioMediaDescription();
        if (null == audioCodec){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"SIP, callRemote, audioCodec is null, read config file!");
            System.out.println("SIP, callRemote, audioCodec is null, read config file!");

            AudioCap audioCap = sipProtocalConfig.getBaseSysConfig().getAudioCapSetList().get(0);
            AudioCap.constructAudioMediaDescription(audioCap, audioMediaDescription);
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"callMt, audioCodec: " + audioCodec);
            System.out.println("callMt, audioCodec : " + audioCodec);

            EncodingFormatEnum encodingFormatEnum = EncodingFormatEnum.FromName(audioCodec.getCodecFormat());
            audioMediaDescription.setEncodingFormat(encodingFormatEnum);
            audioMediaDescription.setPayload(encodingFormatEnum.getFormat());
            audioMediaDescription.setSampleRate(audioCodec.getSampleRate());
            audioMediaDescription.setChannelNum(audioCodec.getChannelNum());
            audioMediaDescription.setBitrate(audioCodec.getBitrate());
        }

        audioMediaDescription.setDirection(MediaDirectionEnum.SendRecv);
        audioMediaDescription.setDual(false);
        audioMediaDescription.setStreamIndex(1);
        mediaDescriptions.add(audioMediaDescription);

        //向流媒体请求媒体资源
        boolean bExistFalse = false;
        List<String> resourceInfo = new ArrayList<>();
        for (MediaDescription mediaDescription : mediaDescriptions) {
            CreateResourceParam createResourceParam = new CreateResourceParam();
            createResourceParam.setSdp(constructCreateSdp(mediaDescription));
            CreateResourceResponse resourceResponse = addExchange(createResourceParam);
            if (null == resourceResponse){
                bExistFalse = true;
                break;
            }

            resourceInfo.add(resourceResponse.getResourceID());
            addMediaResource(mediaDescription.getStreamIndex(), mediaDescription.getDual(), resourceResponse);

            TransportAddress transportAddress = constructTransAddress(resourceResponse.getSdp());
            mediaDescription.getRtpAddress().setIP(transportAddress.getIp());
            mediaDescription.getRtpAddress().setPort(transportAddress.getPort());

            mediaDescription.getRtcpAddress().setIP(transportAddress.getIp());
            mediaDescription.getRtcpAddress().setPort(transportAddress.getPort()+1);
        }

        if (bExistFalse){
            removeMediaResource(true, resourceInfo);
            resourceInfo.clear();
            return TerminalOfflineReasonEnum.Unknown;
        }

        conferenceParticipant.SetLocalMedia(mediaDescriptions);
        bOK = conferenceParticipant.CallRemote(remoteParticipantInfo);

        TerminalOfflineReasonEnum terminalOfflineReasonEnum = TerminalOfflineReasonEnum.OK;

        if (bOK){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"SIP, callRemote OK! write redis!!");
            System.out.println("SIP, callRemote OK! write redis!!");
            TerminalMediaResource terminalMediaResource = new TerminalMediaResource();
            terminalMediaResource.setMtE164(e164);
            terminalMediaResource.setForwardResources(TerminalMediaResource.convertToMediaResource(forwardChannel, "all"));
            terminalMediaSourceService.setTerminalMediaResource(terminalMediaResource);
        } else {
            terminalOfflineReasonEnum = TerminalOfflineReasonEnum.Unknown;

            LogTools.debug(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, callRemote(GroupID:" + groupId +
                    " ,account: " + remoteParticipantInfo.getParticipantId() + ") failed !");

            System.out.println("SIP, callRemote(GroupID:" + groupId + " ,account: " + remoteParticipantInfo.getParticipantId() + ") failed!");
        }

        resourceInfo.clear();
        return terminalOfflineReasonEnum;
    }

    private SipProtocalConfig sipProtocalConfig;
}
