package com.kedacom.confinterface.h323plus;

import com.kedacom.confadapter.common.ConferenceSysRegisterInfo;
import com.kedacom.confadapter.common.NetAddress;
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
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

public class H323PlusTerminalService extends TerminalService {

    public H323PlusTerminalService(String e164, String name, boolean bVmt, H323PlusProtocalConfig h323PlusProtocalConfig) {
        super(e164, name, bVmt);
        this.h323PlusProtocalConfig = h323PlusProtocalConfig;
    }


    public Future<Boolean> startRegGK() {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323PlusTerminalService startRegGK...........");
        System.out.println("H323PlusTerminalService startRegGK..............");
        if (H323PlusregGK) {
            return new AsyncResult<>(true);
        }

        if (null == conferenceParticipant) {
            return new AsyncResult<>(true);
        }

        //注册Gk
        try {
            ConferenceSysRegisterInfo registerInfo = new ConferenceSysRegisterInfo();
            registerInfo.setId(e164);
            registerInfo.setName(name);
            registerInfo.setEndpointId(mtId);

            NetAddress gkAddress = new NetAddress();
            gkAddress.setIP(h323PlusProtocalConfig.getGkIp());
            gkAddress.setPort(h323PlusProtocalConfig.getGkCallPort());
            registerInfo.setConfRegServerAddr(gkAddress);

            NetAddress localAddress = new NetAddress();
            localAddress.setIP(localIp);
            localAddress.setPort(h323PlusProtocalConfig.getLocalCallPort());
            registerInfo.setCallLocalAddr(localAddress);

            NetAddress localRasAddress = new NetAddress();
            localRasAddress.setIP(localIp);
            localRasAddress.setPort(h323PlusProtocalConfig.getLocalRasPort());
            registerInfo.setRegClientAddr(localRasAddress);

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323PlusTerminalService e164(" + e164 + ") start register gk, time : " + System.currentTimeMillis() + ", threadName:" + Thread.currentThread().getName());
            System.out.println("H323PlusTerminalService e164(" + e164 + ") start register gk, time : " + System.currentTimeMillis() + ", threadName:" + Thread.currentThread().getName());

            H323PlusregGK = conferenceParticipant.RegisterToConfSys(registerInfo);
            if (H323PlusregGK) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323PlusTerminalService e164(" + e164 + ") register gk ok, time : " + System.currentTimeMillis());
                System.out.println("H323PlusTerminalService e164(" + e164 + ") register gk ok, time : " + System.currentTimeMillis());
                return new AsyncResult<>(true);
            } else {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323PlusTerminalService e164(" + e164 + ") register gk failed, time : " + System.currentTimeMillis());
                System.out.println("H323PlusTerminalService e164(" + e164 + ") register gk failed, time : " + System.currentTimeMillis());
                return new AsyncResult<>(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AsyncResult<>(false);
        }
    }

    @Override
    public boolean closeDualStreamChannel() {
        return false;
    }

    @Override
    public boolean onOpenLogicalChannel(Vector<MediaDescription> mediaDescriptions) {

        List<String> resourceInfo = isNeedCreateResource(false, mediaDescriptions);

        if (resourceInfo.isEmpty()) {
            //向流媒体发起请求创建资源节点
            boolean bExistFalse = false;
            for (MediaDescription mediaDescription : mediaDescriptions) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, mediaDescription:" + mediaDescription.toString());
                System.out.println("H323Plus, mediaDescription:" + mediaDescription.toString());

                CreateResourceParam createResourceParam = new CreateResourceParam();
                createResourceParam.setSdp(constructSdp(mediaDescription));
                CreateResourceResponse resourceResponse = addExchange(createResourceParam);
                if (null == resourceResponse) {
                    bExistFalse = true;
                    break;
                }

                resourceInfo.add(resourceResponse.getResourceID());
                addMediaResource(mediaDescription.getStreamIndex(), mediaDescription.getDual(), resourceResponse);
            }

            if (bExistFalse && !resourceInfo.isEmpty()) {
                removeMediaResource(true, resourceInfo);
                resourceInfo.clear();
                return false;
            }
        }

        boolean bOk = ackOpenLogicalChannel(mediaDescriptions);
        if (!bOk) {
            //通道打开失败,释放流媒体资源
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, ack channel opening failed" + resourceInfo.toString());
            System.out.println("H323Plus, ack channel opening failed" + resourceInfo.toString());
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
        AudioMediaDescription h323PlusAudioMediaDescription = new AudioMediaDescription();
        if (null == audioCodec || null == audioCodec.getCodecFormat() || audioCodec.getCodecFormat().isEmpty()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "h323Plus, callRemote, audioCodec is null, read config file!");
            System.out.println("h323Plus, callRemote, audioCodec is null, read config file!");

            AudioCap audioCap = h323PlusProtocalConfig.getBaseSysConfig().getAudioCapSetList().get(0);
            AudioCap.constructAudioMediaDescription(audioCap, h323PlusAudioMediaDescription);
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "h323Plus callMt, audioCodec: " + audioCodec);
            System.out.println("h323Plus callMt, audioCodec : " + audioCodec);

            EncodingFormatEnum encodingFormatEnum = EncodingFormatEnum.FromName(audioCodec.getCodecFormat());
            h323PlusAudioMediaDescription.setPayload(encodingFormatEnum.getFormat());
            h323PlusAudioMediaDescription.setEncodingFormat(encodingFormatEnum);
            h323PlusAudioMediaDescription.setSampleRate(audioCodec.getSampleRate());
            h323PlusAudioMediaDescription.setChannelNum(audioCodec.getChannelNum());
            h323PlusAudioMediaDescription.setBitrate(audioCodec.getBitrate());
        }

        h323PlusAudioMediaDescription.setDirection(MediaDirectionEnum.SendRecv);
        h323PlusAudioMediaDescription.setDual(false);
        h323PlusAudioMediaDescription.setStreamIndex(1);
        mediaDescriptions.add(h323PlusAudioMediaDescription);

        VideoMediaDescription h323PlusVideoMediaDescription = new VideoMediaDescription();
        if (null == videoCodec || null == videoCodec.getCodecFormat() || videoCodec.getCodecFormat().isEmpty()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, callRemote, videoCodec is null or codec format is empty, read config file!");
            System.out.println("H323Plus, callRemote, videoCodec is null or codec format is empty, read config file!");

            VideoCap videoCap = h323PlusProtocalConfig.getBaseSysConfig().getVideoCapSetList().get(0);
            VideoCap.constructMediaDescription(videoCap, h323PlusVideoMediaDescription);
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus callMt, videoCodec.getCodeFormat() : " + videoCodec.getCodecFormat() + " ,videoCodec.getResolution()" + videoCodec.getResolution() + " ,videoCodec.getBitrate() :" + videoCodec.getBitrate() + " ,videoCodec.getFramerate()" + videoCodec.getFramerate());
            System.out.println("H323Plus callMt, videoCodec.getCodeFormat() : " + videoCodec.getCodecFormat() + " ,videoCodec.getResolution()" + videoCodec.getResolution() + " ,videoCodec.getBitrate() :" + videoCodec.getBitrate() + " ,videoCodec.getFramerate()" + videoCodec.getFramerate());

            EncodingFormatEnum encodingFormatEnum = EncodingFormatEnum.FromName(videoCodec.getCodecFormat());
            h323PlusVideoMediaDescription.setEncodingFormat(encodingFormatEnum);
            h323PlusVideoMediaDescription.setResolution(videoCodec.getResolution());
            h323PlusVideoMediaDescription.setPayload(encodingFormatEnum.getFormat());
            h323PlusVideoMediaDescription.setBitrate(videoCodec.getBitrate());
            h323PlusVideoMediaDescription.setFramerate(videoCodec.getFramerate());
        }

        h323PlusVideoMediaDescription.setDirection(MediaDirectionEnum.SendRecv);
        h323PlusVideoMediaDescription.setDual(false);
        h323PlusVideoMediaDescription.setStreamIndex(0);
        mediaDescriptions.add(h323PlusVideoMediaDescription);

        //向流媒体请求媒体资源
        boolean bExistFalse = false;
        List<String> resourceInfo = new ArrayList<>();
        for (MediaDescription mediaDescription : mediaDescriptions) {
            CreateResourceParam h323PlusCreateResourceParam = new CreateResourceParam();
            h323PlusCreateResourceParam.setDeviceID(deviceID);
            h323PlusCreateResourceParam.setSdp(constructSdp(mediaDescription));
            CreateResourceResponse resourceResponse = addExchange(h323PlusCreateResourceParam);
            if (null == resourceResponse) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus create resource node failed resourceResponse is null ........");
                System.out.println("H323Plus create resource node failed resourceResponse is null ........");
                bExistFalse = true;
                break;
            }

            resourceInfo.add(resourceResponse.getResourceID());
            addMediaResource(mediaDescription.getStreamIndex(), mediaDescription.getDual(), resourceResponse);

            TransportAddress transportAddress = constructTransAddress(resourceResponse.getSdp());
            mediaDescription.getRtpAddress().setPort(transportAddress.getPort());
            mediaDescription.getRtpAddress().setIP(transportAddress.getIp());

            mediaDescription.getRtcpAddress().setIP(transportAddress.getIp());
            mediaDescription.getRtcpAddress().setPort(transportAddress.getPort() + 1);
        }
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus callRemote mediaDescriptions node ..........mediaDescriptions : " + mediaDescriptions);
        System.out.println("H323Plus callRemote mediaDescriptions node ..........mediaDescriptions : " + mediaDescriptions);

        if (bExistFalse) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus remove resource node ..........");
            System.out.println("H323Plus remove resource node ..........");
            removeMediaResource(true, resourceInfo);
            resourceInfo.clear();
            return TerminalOfflineReasonEnum.Unknown;
        }

        conferenceParticipant.SetLocalMedia(mediaDescriptions);
        bOK = conferenceParticipant.CallRemote(remoteParticipantInfo);

        TerminalOfflineReasonEnum terminalOfflineReasonEnum = TerminalOfflineReasonEnum.OK;

        if (bOK) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, callRemote OK! write redis!!");
            System.out.println("H323Plus, callRemote OK! write redis!!");
            TerminalMediaResource terminalMediaResource = new TerminalMediaResource();
            terminalMediaResource.setMtE164(e164);
            terminalMediaResource.setForwardResources(TerminalMediaResource.convertToMediaResource(forwardChannel, "all"));
            terminalMediaSourceService.setTerminalMediaResource(terminalMediaResource);
        } else {
            removeMediaResource(true, resourceInfo);

            terminalOfflineReasonEnum = TerminalOfflineReasonEnum.Unknown;

            LogTools.debug(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, callRemote(GroupID:" + groupId +
                    " ,account: " + remoteParticipantInfo.getParticipantId() + ") failed !");

            System.out.println("H323Plus, callRemote(GroupID:" + groupId + " ,account: " + remoteParticipantInfo.getParticipantId() + ") failed!");
        }

        resourceInfo.clear();
        return terminalOfflineReasonEnum;
    }

    private H323PlusProtocalConfig h323PlusProtocalConfig;

    private boolean H323PlusregGK = false;
}
