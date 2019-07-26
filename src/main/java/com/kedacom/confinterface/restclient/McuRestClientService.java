package com.kedacom.confinterface.restclient;

import com.kedacom.confinterface.dto.*;
import com.kedacom.confinterface.inner.*;
import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.restclient.mcu.*;
import com.kedacom.confinterface.service.ConfInterfacePublishService;
import com.kedacom.confinterface.service.ConfInterfaceService;
import com.kedacom.confinterface.service.TerminalService;
import com.kedacom.confinterface.service.UnifiedDevicePushService;
import com.kedacom.confinterface.syssetting.BaseSysConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty(name = "confinterface.sys.useMcu", havingValue = "true", matchIfMissing = true)
@Service
@EnableScheduling
@EnableConfigurationProperties(McuRestConfig.class)
public class McuRestClientService {

    public boolean login() {
        if (loginSuccess) {
            if (mcuSubscribeClientService.isHandShakeOk()) {
                System.out.println("login success and handshake OK!");
                return true;
            }

            return false;
        }

        //dev, test, prod
        //加载当前profile
        String[] activeProfs = env.getActiveProfiles();
        if (activeProfs.length > 0) {
            activeProf = activeProfs[0];
            System.out.println("constructUrl: active prof is " + activeProf);
        }

        //通过软件key和密钥获取account_token
        StringBuilder param = new StringBuilder();
        mcuSubscribeClientService.initializing(mcuRestConfig.getMcuIp());

        try {
            param.append("oauth_consumer_key=");
            param.append(URLEncoder.encode(mcuRestConfig.getSoftwareKey(), "UTF-8"));
            param.append("&oauth_consumer_secret=");
            param.append(URLEncoder.encode(mcuRestConfig.getSecretKey(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("param: " + param.toString());

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/system/token");

        ResponseEntity<AccountTokenResponse> result = restClientService.postForEntity(url.toString(), param.toString(), urlencodeMediaType, AccountTokenResponse.class);
        if (null == result) {
            System.out.println("get token failed!!!!!!!!!!!!");
            return false;
        }

        if (result.getBody().getSuccess() == 0) {
            int errorCode = result.getBody().getError_code();
            System.out.println("get token failed! errCode:" + errorCode + ", errMsg:" + McuStatus.resolve(errorCode).getDescription());
            return false;
        }

        accountToken = result.getBody().getAccount_token();
        System.out.println("token: " + accountToken);

        param.delete(0, param.length());

        try {
            param.append("account_token=");
            param.append(URLEncoder.encode(accountToken, "UTF-8"));
            param.append("&username=");
            param.append(URLEncoder.encode(mcuRestConfig.getUsername(), "UTF-8"));
            param.append("&password=");
            param.append(URLEncoder.encode(mcuRestConfig.getPassword(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("login param: " + param.toString());
        constructUrl(url, "/api/v1/system/login");

        //result中带有是否成功与用户名信息
        ResponseEntity<LoginResponse> loginResult = restClientService.postForEntity(url.toString(), param.toString(), urlencodeMediaType, LoginResponse.class);
        if (loginResult.getBody().getSuccess() == 0) {
            int errorCode = loginResult.getBody().getError_code();
            System.out.println("login fail, errCode:" + errorCode + ", errMsg:" + McuStatus.resolve(errorCode).getDescription());

            loginSuccess = false;
            return false;
        }

        System.out.println("login Ok, start get cookies!");

        HttpHeaders httpHeaders = loginResult.getHeaders();
        List<String> getCookies = httpHeaders.get("Set-Cookie");
        System.out.println("Set-Cookie: " + getCookies);
        if (getCookies.size() >= 1) {
            cookies = new ArrayList<>();
            for (String cookie : getCookies) {
                System.out.println("login, cookie:" + cookie);
                String[] cookieParse = cookie.split(";", 2);
                cookies.add(cookieParse[0]);
            }

            System.out.println("get cooke OK, cookie : " + cookies);
        }

        loginSuccess = true;
        mcuSubscribeClientService.handshake(cookies);

        if (null == confSubcribeChannelMap) {
            confSubcribeChannelMap = new ConcurrentHashMap<>();
        }

        System.out.println("login successfully!! handshake:" + mcuSubscribeClientService.isHandShakeOk());
        return mcuSubscribeClientService.isHandShakeOk();
    }

    public String createConference() {
        if (!loginSuccess) {
            System.out.println("createConference, has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        CreateConferenceParam createConferenceParam = new CreateConferenceParam();
        createConferenceParam.setCall_mode(mcuRestConfig.getCallMode());
        createConferenceParam.setCall_times(mcuRestConfig.getCallTimes());
        createConferenceParam.setCall_interval(mcuRestConfig.getCallInterval());
        createConferenceParam.setEncrypted_type(mcuRestConfig.getEncryptedType());
        createConferenceParam.setEncrypted_key(mcuRestConfig.getEncryptedKey());
        createConferenceParam.setVideo_formats(mcuRestConfig.getVideoFormat());
        createConferenceParam.setAudio_formats(mcuRestConfig.getAudioFormat());

        System.out.println("createConfParam:" + createConferenceParam.toString());

        constructUrl(url, "/api/v1/mc/confs");
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(createConferenceParam);
        CreateConferenceResponse createConferenceResponse = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, CreateConferenceResponse.class);
        if (null == createConferenceResponse) {
            System.out.println("createConferenceResponse is null");
            return null;
        }

        if (createConferenceResponse.success()) {
            //创建会议成功，返回confId
            List<String> channels = new ArrayList<>();
            confSubcribeChannelMap.put(createConferenceResponse.getConf_id(), channels);
            System.out.println("createConferenceResponse, success:" + createConferenceResponse.getSuccess() + ", ConfId:" + createConferenceResponse.getConf_id());
            return createConferenceResponse.getConf_id();
        } else {
            int errorCode = createConferenceResponse.getError_code();
            System.out.println("create conference failed! errCode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
            return null;
        }
    }

    public McuStatus endConference(String confId, boolean deleteConf) {
        if (!loginSuccess)
            return McuStatus.TimeOut;

        McuBaseResponse response = null;
        if (deleteConf) {
            StringBuilder url = new StringBuilder();
            constructUrl(url, "/api/v1/mc/confs/{conf_id}");
            Map<String, String> args = new HashMap<>();
            args.put("conf_id", confId);

            McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
            response = restClientService.exchange(url.toString(), HttpMethod.DELETE, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
            if (null == response)
                return McuStatus.Unknown;
        } else {
            List<String> channels = confSubcribeChannelMap.get(confId);
            if (null != channels) {
                for (String channel : channels) {
                    mcuSubscribeClientService.unsubscribe(channel);
                }
            }

            channels.clear();
            confSubcribeChannelMap.remove(confId);
        }

        if (null == response)
            return McuStatus.OK;

        return McuStatus.resolve(response.getError_code());
    }

    public List<JoinConferenceRspMtInfo> joinConference(String confId, List<Terminal> mts) {
        if (!loginSuccess) {
            System.out.println("joinConference, has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/mts");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        List<JoinConferenceMtInfo> joinConferenceMtInfos = new ArrayList<>();
        for (Terminal mt : mts) {
            JoinConferenceMtInfo joinConferenceMtInfo = new JoinConferenceMtInfo();
            joinConferenceMtInfo.setAccount(mt.getMtE164());
            joinConferenceMtInfo.setBitrate(mcuRestConfig.getBitrate());
            joinConferenceMtInfo.setProtocol(mcuRestConfig.getProtocalCode());

            joinConferenceMtInfos.add(joinConferenceMtInfo);
        }

        JoinConferenceMts joinConferenceMts = new JoinConferenceMts();
        joinConferenceMts.setMts(joinConferenceMtInfos);

        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(joinConferenceMts);
        JoinConferenceResponse response = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, JoinConferenceResponse.class);
        if (null == response) {
            System.out.println("JoinConferenceResponse null!");
            return null;
        }

        if (response.success()) {
            System.out.println("JoinConferenceResponse success, confId : " + confId);
            subscribeConfMts(confId);
            return response.getMts();
        } else {
            int errorCode = response.getError_code();
            System.out.println("join conference failed! errcode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
        }

        return null;
    }

    public McuStatus leftConference(String confId, List<TerminalId> mts, boolean delConf) {
        if (mts.isEmpty())
            return McuStatus.OK;

        if (!loginSuccess) {
            System.out.println("leftConference, has login out!!!");
            return McuStatus.TimeOut;
        }

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/mts");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        LeftConferenceMts leftConferenceMts = new LeftConferenceMts();
        leftConferenceMts.setMts(mts);
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(leftConferenceMts);
        McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.DELETE, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
        if (null == response)
            return McuStatus.Unknown;

        if (!response.success()) {
            System.out.println("left conference failed! errCode : " + response.getError_code());
        }

        if (delConf) {
            endConference(confId, true);
        }

        if (response.success())
            return McuStatus.OK;

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus inspections(String confId, String mode, String srcMtId, String dstMtId) {
        if (!loginSuccess) {
            System.out.println("inspections, has login out!!!");
            return McuStatus.TimeOut;
        }

        //开始选看 /api/v1/vc/confs/{conf_id}/inspections
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/inspections");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        McuInspectionParam mcuInspectionParam = new McuInspectionParam();
        InspectionSrcInfo inspectionSrcInfo = new InspectionSrcInfo();
        inspectionSrcInfo.setMt_id(srcMtId);
        inspectionSrcInfo.setType(1);
        mcuInspectionParam.setSrc(inspectionSrcInfo);

        InspectionDstInfo inspectionDstInfo = new InspectionDstInfo(dstMtId);
        mcuInspectionParam.setDst(inspectionDstInfo);

        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        if (mode.equals(InspectionModeEnum.ALL.getName()) || mode.equals(InspectionModeEnum.VIDEO.getName())) {
            mcuInspectionParam.setMode(InspectionModeEnum.VIDEO.getCode());
            mcuPostMsg.setParams(mcuInspectionParam);
            McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
            if (null == response) {
                System.out.println("inspections, video, null == response");
                return McuStatus.Unknown;
            }

            if (!response.success()) {
                int errorCode = response.getError_code();
                McuStatus mcuStatus = McuStatus.resolve(response.getError_code());
                System.out.println("inspections, dstMtId(" + dstMtId + ") inspect srcMtId(" + srcMtId + ") video failed! errcode : " + errorCode + ", errMsg : " + mcuStatus.getDescription());
                return mcuStatus;
            }
        }

        if (mode.equals(InspectionModeEnum.ALL.getName()) || mode.equals(InspectionModeEnum.AUDIO.getName())) {
            mcuInspectionParam.setMode(InspectionModeEnum.AUDIO.getCode());
            mcuPostMsg.setParams(mcuInspectionParam);
            McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
            if (null == response) {
                System.out.println("inspections, audio, null == response");
                return McuStatus.Unknown;
            }
            if (!response.success()) {
                int errorCode = response.getError_code();
                McuStatus mcuStatus = McuStatus.resolve(response.getError_code());
                System.out.println("inspections, dstMtId(" + dstMtId + ") inspect srcMtId(" + srcMtId + ") audio failed! errCode :" + errorCode + ", errMsg:" + mcuStatus.getDescription());
                return mcuStatus;
            }
        }

        return McuStatus.OK;
    }

    public McuStatus cancelInspection(String confId, String mode, String mtId) {
        if (!loginSuccess) {
            System.out.println("cancelInspection, has login out!!!");
            return McuStatus.TimeOut;
        }
        //url : /api/v1/vc/confs/{conf_id}/inspections/{mt_id}/{mode}
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/inspections/{mt_id}/{mode}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("mt_id", mtId);

        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        if (mode.equals(InspectionModeEnum.ALL.getName()) || mode.equals(InspectionModeEnum.VIDEO.getName())) {
            args.put("mode", "1");
            McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.DELETE, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
            if (null == response) {
                System.out.println("cancelInspection, video, null == response");
                return McuStatus.Unknown;
            }
            if (!response.success()) {
                System.out.println("cancelInspection, video, cancel inspection(" + "mtId:" + mtId + ",confId:" + confId + ", mode: video) failed! errCode : " + response.getError_code());
                return McuStatus.resolve(response.getError_code());
            }
        }

        if (mode.equals(InspectionModeEnum.ALL.getName()) || mode.equals(InspectionModeEnum.AUDIO.getName())) {
            args.put("mode", "2");
            McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.DELETE, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
            if (null == response) {
                System.out.println("cancelInspection, audio, null == response");
                return McuStatus.Unknown;
            }
            if (!response.success()) {
                System.out.println("cancelInspection, audio, cancel inspection(" + "mtId:" + mtId + ",confId:" + confId + ", mode: audio) failed! errCode : " + response.getError_code());
                return McuStatus.resolve(response.getError_code());
            }
        }

        return McuStatus.OK;
    }

    public String getDualStream(String confId) {
        if (!loginSuccess) {
            System.out.println("getDualStream, has login out!!!");
            return null;
        }

        //url : /api/v1/vc/confs/{conf_id}/dualstream
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/dualstream?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("account_token", accountToken);

        GetDualStreamResponse getDualStreamResponse = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, GetDualStreamResponse.class);
        if (null == getDualStreamResponse) {
            System.out.println("getDualStream, null == getDualStreamResponse");
            return null;
        }

        if (!getDualStreamResponse.success()) {
            int errorCode = getDualStreamResponse.getError_code();
            System.out.println("getDualStream, failed! errcode:" + errorCode + ",errmsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        return getDualStreamResponse.getMt_id();
    }

    public String getSpeaker(String confId) {
        if (!loginSuccess) {
            System.out.println("getSpeaker, has login out!!!");
            return null;
        }
        //url : /api/v1/vc/confs/{conf_id}/speaker
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/speaker?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("account_token", accountToken);

        GetSpeakerResponse getSpeakerResponse = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, GetSpeakerResponse.class);
        if (null == getSpeakerResponse) {
            System.out.println("getSpeaker, null == getSpeakerResponse");
            return null;
        }

        if (!getSpeakerResponse.success()) {
            int errorCode = getSpeakerResponse.getError_code();
            System.out.println("getSpeaker, failed! errcode:" + errorCode + ",errmsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        return getSpeakerResponse.getMt_id();
    }

    public McuStatus setSpeaker(String confId, String mtId) {
        if (!loginSuccess) {
            System.out.println("setSpeaker, has login out!!!");
            return McuStatus.TimeOut;
        }

        //url：/api/v1/vc/confs/{conf_id}/speaker
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/speaker");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        TerminalId terminalId = new TerminalId(mtId);
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(terminalId);
        McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.PUT, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
        if (null == response) {
            System.out.println("setSpeaker, exchange put, null == response");
            return McuStatus.Unknown;
        }

        if (!response.success()) {
            System.out.println("setSpeaker, exchange put failed! errcode:" + response.getError_code());
            return McuStatus.resolve(response.getError_code());
        }

        return McuStatus.OK;
    }

    public McuStatus cancelSpeaker(String confId) {
        if (!loginSuccess) {
            System.out.println("cancelSpeaker, has login out!!!");
            return McuStatus.TimeOut;
        }
        //url：/api/v1/vc/confs/{conf_id}/speaker
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/speaker");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        TerminalId terminalId = new TerminalId("");
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(terminalId);
        McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.PUT, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
        if (null == response)
            return McuStatus.Unknown;

        if (response.success())
            return McuStatus.OK;

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus ctrlCamera(String confId, String mtId, CameraCtrlParam cameraCtrlParam) {
        if (!loginSuccess) {
            System.out.println("ctrlCamera, has login out!!!");
            return McuStatus.TimeOut;
        }

        //url ：/api/v1/vc/confs/{conf_id}/mts/{mt_id}/camera
        //会议终端摄像头控制为同步操作，不需要订阅
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/mts/{mt_id}/camera");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("mt_id", mtId);

        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(cameraCtrlParam);
        McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
        if (null == response)
            return McuStatus.Unknown;

        if (response.success())
            return McuStatus.OK;

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus sendIFrame(String confId, TransportAddress transportAddress) {
        if (!loginSuccess) {
            System.out.println("sendIFrame, has login out!!!");
            return McuStatus.TimeOut;
        }

        //url ：/api/v1/vc/confs/{conf_id}/neediframe/monitors
        //请求关键帧为同步操作，不需要订阅
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/neediframe/monitors");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        NeedIFrameParam needIFrameParam = new NeedIFrameParam();
        needIFrameParam.setDst(transportAddress);
        mcuPostMsg.setParams(needIFrameParam);

        McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
        if (null == response)
            return McuStatus.Unknown;

        if (response.success())
            return McuStatus.OK;

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus ctrlVolume(String confId, String mtId, McuCtrlVolumeParam ctrlVolumeParam) {
        if (!loginSuccess) {
            System.out.println("ctrlVolume, has login out!!!");
            return McuStatus.TimeOut;
        }

        //url ：/api/v1/vc/confs/{conf_id}/mts/{mt_id}/volume
        //控制终端音量为同步操作，不需要订阅
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/mts/{mt_id}/volume");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("mt_id", mtId);

        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(ctrlVolumeParam);
        McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.PUT, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
        if (null == response)
            return McuStatus.Unknown;

        if (response.success())
            return McuStatus.OK;

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus silenceOrMute(String confId, String mtId, boolean silence, SilenceOrMuteParam silenceOrMuteParam) {
        if (!loginSuccess) {
            System.out.println("silenceOrMute, has login out!!!");
            return McuStatus.TimeOut;
        }

        //url ：静音：/api/v1/vc/confs/{conf_id}/mts/{mt_id}/silence
        //url : 哑音：/api/v1/vc/confs/{conf_id}/mts/{mt_id}/mute
        //控制静音或者哑音为同步操作，不需要订阅
        StringBuilder url = new StringBuilder();
        if (silence) {
            constructUrl(url, "/api/v1/vc/confs/{conf_id}/mts/{mt_id}/silence");
        } else {
            constructUrl(url, "/api/v1/vc/confs/{conf_id}/mts/{mt_id}/mute");
        }

        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("mt_id", mtId);

        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(silenceOrMuteParam);
        McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.PUT, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
        if (null == response)
            return McuStatus.Unknown;

        if (response.success())
            return McuStatus.OK;

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus ctrlDualStream(String confId, String mtId, boolean dual) {
        if (!loginSuccess) {
            System.out.println("ctrlDualStream, has login out!!!");
            return McuStatus.TimeOut;
        }

        //url ：/api/v1/vc/confs/{conf_id}/dualstream
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/dualstream");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        TerminalId terminalId = new TerminalId(mtId);
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(terminalId);

        McuBaseResponse response;

        if (dual) {
            response = restClientService.exchange(url.toString(), HttpMethod.PUT, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
        } else {
            response = restClientService.exchange(url.toString(), HttpMethod.DELETE, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
        }

        if (null == response)
            return McuStatus.Unknown;

        if (response.success())
            return McuStatus.OK;

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus onlineMts(String confId, OnlineMtsInfo onlineMtsInfo) {
        if (!loginSuccess) {
            System.out.println("onlineMts, has login out!!!");
            return McuStatus.TimeOut;
        }

        subscribeConfMts(confId);

        //url：/api/v1/vc/confs/{conf_id}/online_mts
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/online_mts");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(onlineMtsInfo);
        McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
        if (null == response)
            return McuStatus.Unknown;

        if (response.success())
            return McuStatus.OK;

        return McuStatus.resolve(response.getError_code());
    }

    public Map<String, CascadeTerminalInfo> getCascadesTerminal(String confId, String cascadeId, boolean e164Key) {
        if (!loginSuccess) {
            System.out.println("getCascadesTerminal, has login out!!!");
            return null;
        }

        //URL为 /api/v1/vc/confs/{conf_id}/cascades/{cascade_id}/mts
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/cascades/{cascade_id}/mts?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("cascade_id", cascadeId);
        args.put("account_token", accountToken);

        GetCascadesMtResponse getCascadesMtResponse = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, GetCascadesMtResponse.class);
        if (null == getCascadesMtResponse) {
            System.out.println("getCascadesTerminal, null == getCascadesMtResponse");
            return null;
        }

        if (!getCascadesMtResponse.success()) {
            int errorCode = getCascadesMtResponse.getError_code();
            System.out.println("getCascadesTerminal failed! errCode :" + errorCode + ", errMsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        List<CascadeTerminalInfo> terminalInfos = getCascadesMtResponse.getMts();
        if (null == terminalInfos || terminalInfos.isEmpty()) {
            System.out.println("GetCascadesMtResponse has no result!!");
            return null;
        }

        System.out.println("getCascadesTerminal, total terminal num :" + terminalInfos.size());
        Map<String, CascadeTerminalInfo> terminalInfoMap = new HashMap<>();
        if (e164Key) {
            for (CascadeTerminalInfo terminalInfo : terminalInfos) {
                terminalInfoMap.put(terminalInfo.getE164(), terminalInfo);
            }
        } else {
            for (CascadeTerminalInfo terminalInfo : terminalInfos) {
                terminalInfoMap.put(terminalInfo.getMt_id(), terminalInfo);
            }
        }

        return terminalInfoMap;
    }

    public GetConfMtInfoResponse getConfMtInfo(String confId, String mtId) {
        if (!loginSuccess) {
            System.out.println("getCascadesTerminal, has login out!!!");
            return null;
        }

        //URL为 /api/v1/vc/confs/{conf_id}/mts/{mt_id}
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/mts/{mt_id}?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("mt_id", mtId);
        args.put("account_token", accountToken);

        GetConfMtInfoResponse getConfMtInfoResponse = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, GetConfMtInfoResponse.class);
        if (null == getConfMtInfoResponse) {
            System.out.println("getConfMtInfo, null == getCascadesMtResponse");
            return null;
        }

        if (!getConfMtInfoResponse.success()) {
            int errorCode = getConfMtInfoResponse.getError_code();
            System.out.println("getConfMtInfo failed! errCode :" + errorCode + ", errmsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        return getConfMtInfoResponse;
    }

    @Scheduled(initialDelay = heartbeatInterval, fixedRate = heartbeatInterval)
    public void doHearbeat() {
        if ("mcu".equals(baseSysConfig.getMcuMode())) {
            System.out.println("5.2mcu心跳机制");
            System.out.println("McuRestClientService, start do heartbeat, current time : " + System.currentTimeMillis() + ", threadName:" + Thread.currentThread().getName());
            //每25分钟执行一次心跳检测 /api/v1/system/heartbeat
            StringBuilder url = new StringBuilder();
            constructUrl(url, "/api/v1/system/heartbeat");

            McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
            ResponseEntity<McuBaseResponse> response = restClientService.postForEntity(url.toString(), mcuPostMsg.getMsg(), urlencodeMediaType, McuBaseResponse.class);
            if (null == response || response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
                //尝试重新登陆
                loginSuccess = false;
                while (true) {
                    boolean bOK = login();
                    if (bOK) {
                        break;
                    }

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("mcuSubscribeClientService.isHandShakeOk() : " + mcuSubscribeClientService.isHandShakeOk());
                if (!mcuSubscribeClientService.isHandShakeOk()) {
                    System.out.println("尝试重新登录");
                    loginSuccess = false;
                    while (true) {
                        boolean bOK = login();
                        if (bOK) {
                           /* number = 0;
                            Map<String, String> groups = terminalMediaSourceService.getGroups();
                            //System.out.println("通过Map.entrySet遍历key和value");
                            for (Map.Entry<String, String> group : groups.entrySet()) {
                                //System.out.println("key= " + group.getKey() + " and value= " + group.getValue());
                                McuStatus mcuStatus = endConference(group.getValue(), true);
                                if (mcuStatus.getValue() == 0) {
                                    GroupConfInfo groupConfInfo = confInterfaceService.getGroupConfInfo(group.getKey());
                                    endConference(group.getValue(), false);
                                    confInterfaceService.delGroupConfInfo(groupConfInfo);
                                    groupConfInfo.cancelGroup();
                                    terminalMediaSourceService.delGroup(group.getKey());
                                    terminalMediaSourceService.delGroupMtMembers(group.getKey(), null);
                                    terminalMediaSourceService.delGroupVmtMembers(group.getKey(), null);
                                    terminalMediaSourceService.delBroadcastSrcInfo(group.getKey());*/
                            Map<String, P2PCallGroup> p2pCallGroupMap = ConfInterfaceService.p2pCallGroupMap;
                            if (null != p2pCallGroupMap) {
                                for (Map.Entry<String, P2PCallGroup> entry : p2pCallGroupMap.entrySet()) {
                                    System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
                                    for (Map.Entry<String, TerminalService> entrys : entry.getValue().getCallMap().entrySet()) {
                                        System.out.println("entrys.getKey() " + entrys.getKey());
                                        entry.getValue().removeCallMember(entrys.getKey());
                                        if ("mediaSchedule".equals(baseSysConfig.getPushServiceType())) {
                                            TerminalStatusNotify terminalStatusNotify = new TerminalStatusNotify();
                                            TerminalStatus terminalStatus = new TerminalStatus(entrys.getKey(), "MT", 0);
                                            terminalStatusNotify.addMtStatus(terminalStatus);
                                            confInterfacePublishService.publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, entry.getKey(), terminalStatusNotify);
                                        } else {
                                            UnifiedDevicePushTerminalStatus unifiedDevicePushTerminalStatus = new UnifiedDevicePushTerminalStatus(entrys.getKey(), entry.getKey(), TerminalOnlineStatusEnum.OFFLINE.getCode());
                                            unifiedDevicePushService.publishMtStatus(unifiedDevicePushTerminalStatus);
                                        }

                                    }
                                }
                            }
                            System.out.println("结会成功 confId : ");
                        } else {
                            System.out.println("结会失败 confId : " );
                        }
                        break;
                    }

                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void constructUrl(StringBuilder url, String restApi) {
        System.out.println("url length : " + url.length());

        if (url.length() > 0) {
            url.delete(0, url.length());
        }

        if ("prod".equals(activeProf))
            url.append("https://");
        else
            url.append("http://");

        url.append(mcuRestConfig.getMcuIp());
        if (mcuRestConfig.getMcuRestPort() > 0) {
            url.append(":");
            url.append(mcuRestConfig.getMcuRestPort());
        }
        url.append(restApi);

        System.out.println("url : " + url.toString());
    }

    public void subscribeConfInfo(String confId) {
        StringBuilder subscribeChannel = new StringBuilder();
        subscribeChannel.append("/confs/");
        subscribeChannel.append(confId);

        List<String> subscribeChannelList = confSubcribeChannelMap.get(confId);
        if (null == subscribeChannelList) {
            subscribeChannelList = Collections.synchronizedList(new ArrayList<>());
        }

        for (String channel : subscribeChannelList) {
            if (channel.equals(subscribeChannel.toString()))
                return;
        }

        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        subscribeChannelList.add(subscribeChannel.toString());
        confSubcribeChannelMap.put(confId, subscribeChannelList);
    }

    public void subscribeConfMts(String confId) {
        StringBuilder subscribeChannel = new StringBuilder();
        subscribeChannel.append("/confs/");
        subscribeChannel.append(confId);
        subscribeChannel.append("/mts");

        List<String> subscribeChannelList = confSubcribeChannelMap.get(confId);
        if (null == subscribeChannelList) {
            subscribeChannelList = Collections.synchronizedList(new ArrayList<>());
        }

        for (String channel : subscribeChannelList) {
            if (channel.equals(subscribeChannel.toString()))
                return;
        }

        //订阅会议终端信息 添加终端失败通知(/confs/{conf_id}/mts) 及 终端列表(/confs/{conf_id}/cascades/{cascade_id}/mts/{mt_id})
        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        subscribeChannelList.add(subscribeChannel.toString());

        subscribeChannel.delete(0, subscribeChannel.length());
        subscribeChannel.append("/confs/");
        subscribeChannel.append(confId);
        subscribeChannel.append("/cascades/0/mts/*");

        subscribeChannelList.add(subscribeChannel.toString());
        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        confSubcribeChannelMap.put(confId, subscribeChannelList);
    }

    public void subscribeInspection(String confId) {
        //终端选看及失败订阅通道/confs/{conf_id}/inspections/**
        StringBuilder subscribeChannel = new StringBuilder();
        subscribeChannel.append("/confs/");
        subscribeChannel.append(confId);
        subscribeChannel.append("/inspections/**");

        List<String> subscribeChannelList = confSubcribeChannelMap.get(confId);
        if (null == subscribeChannelList) {
            subscribeChannelList = Collections.synchronizedList(new ArrayList<>());
        }

        for (String channel : subscribeChannelList) {
            if (channel.equals(subscribeChannel.toString()))
                return;
        }

        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        subscribeChannelList.add(subscribeChannel.toString());
        confSubcribeChannelMap.put(confId, subscribeChannelList);
    }

    public void subscribeSpeaker(String confId) {
        //发言人通道/confs/{conf_id}/speaker
        StringBuilder subscribeChannel = new StringBuilder();
        subscribeChannel.delete(0, subscribeChannel.length());
        subscribeChannel.append("/confs/");
        subscribeChannel.append(confId);
        subscribeChannel.append("/speaker");

        List<String> subscribeChannelList = confSubcribeChannelMap.get(confId);
        if (null == subscribeChannelList) {
            subscribeChannelList = Collections.synchronizedList(new ArrayList<>());
        }

        for (String channel : subscribeChannelList) {
            if (channel.equals(subscribeChannel.toString()))
                return;
        }

        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        subscribeChannelList.add(subscribeChannel.toString());
        confSubcribeChannelMap.put(confId, subscribeChannelList);
    }

    public void subscribeDual(String confId) {
        //会议双流源通道/confs/{conf_id}/dualstream
        StringBuilder subscribeChannel = new StringBuilder();
        subscribeChannel.delete(0, subscribeChannel.length());
        subscribeChannel.append("/confs/");
        subscribeChannel.append(confId);
        subscribeChannel.append("/dualstream");

        List<String> subscribeChannelList = confSubcribeChannelMap.get(confId);
        if (null == subscribeChannelList) {
            subscribeChannelList = Collections.synchronizedList(new ArrayList<>());
        }

        for (String channel : subscribeChannelList) {
            if (channel.equals(subscribeChannel.toString()))
                return;
        }

        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        subscribeChannelList.add(subscribeChannel.toString());
        confSubcribeChannelMap.put(confId, subscribeChannelList);
    }

    @Autowired
    private RestClientService restClientService;

    @Autowired
    private McuSubscribeClientService mcuSubscribeClientService;

    @Autowired
    private McuRestConfig mcuRestConfig;

    @Autowired
    private Environment env;

    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MediaType urlencodeMediaType = MediaType.APPLICATION_FORM_URLENCODED;
    private String accountToken;
    private List<String> cookies;
    //private final long heartbeatInterval = 25 * 60 * 1000L;
    private final long heartbeatInterval = 1 * 60 * 1000L;
    //private volatile boolean loginSuccess;
    public volatile boolean loginSuccess;
    private Map<String, List<String>> confSubcribeChannelMap;
    private static String activeProf = "dev";

    @Autowired
    private BaseSysConfig baseSysConfig;

    @Autowired
    private ConfInterfacePublishService confInterfacePublishService;

    @Autowired
    private UnifiedDevicePushService unifiedDevicePushService;
}
