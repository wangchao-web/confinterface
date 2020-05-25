package com.kedacom.confinterface.restclient;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dto.*;
import com.kedacom.confinterface.inner.*;
import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.restclient.mcu.*;
import com.kedacom.confinterface.restclient.mcu.ConfsCascadesMtsRspInfo;
import com.kedacom.confinterface.service.ConfInterfaceService;
import com.kedacom.confinterface.service.TerminalManageService;
import com.kedacom.confinterface.service.TerminalMediaSourceService;
import com.kedacom.confinterface.service.TerminalService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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

@ConditionalOnExpression("${confinterface.sys.useMcu:true} && '${confinterface.sys.mcuMode}'.equals('mcu')")
//@ConditionalOnExpression("${confinterface.sys.useMcu:true}")
@Service
@EnableScheduling
@EnableConfigurationProperties(McuRestConfig.class)
public class McuRestClientService {

    public boolean login() {
        if (loginSuccess) {
            if (mcuSubscribeClientService.isHandShakeOk()) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "login success and handshake OK!");
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
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "constructUrl: active prof is " + activeProf);
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
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mcuRestConfig.getSoftwareKey() : " + mcuRestConfig.getSoftwareKey());
        System.out.println("mcuRestConfig.getSoftwareKey() : " + mcuRestConfig.getSoftwareKey());
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"mcuRestConfig.getSecretKey() : " + mcuRestConfig.getSecretKey());
        System.out.println("mcuRestConfig.getSecretKey() : " + mcuRestConfig.getSecretKey());

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "param: " + param.toString());
        System.out.println("param: " + param.toString());

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/system/token");

        //System.out.println(restClientService);
        ResponseEntity<AccountTokenResponse> result = restClientService.postForEntity(url.toString(), param.toString(), urlencodeMediaType, AccountTokenResponse.class);
        if (null == result) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "get token failed!!!!!!!!!!!!");
            System.out.println("get token failed!!!!!!!!!!!!");
            return false;
        }

        if (result.getBody().getSuccess() == 0) {
            int errorCode = result.getBody().getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "get token failed! errCode:" + errorCode + ", errMsg:" + McuStatus.resolve(errorCode).getDescription());
            System.out.println("get token failed! errCode:" + errorCode + ", errMsg:" + McuStatus.resolve(errorCode).getDescription());
            return false;
        }

        accountToken = result.getBody().getAccount_token();
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "token: " + accountToken);
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

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "login param: " + param.toString());
        System.out.println("login param: " + param.toString());
        constructUrl(url, "/api/v1/system/login");

        //result中带有是否成功与用户名信息
        ResponseEntity<LoginResponse> loginResult = restClientService.postForEntity(url.toString(), param.toString(), urlencodeMediaType, LoginResponse.class);
        if (loginResult.getBody().getSuccess() == 0) {
            int errorCode = loginResult.getBody().getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "login fail, errCode:" + errorCode + ", errMsg:" + McuStatus.resolve(errorCode).getDescription());
            System.out.println("login fail, errCode:" + errorCode + ", errMsg:" + McuStatus.resolve(errorCode).getDescription());

            loginSuccess = false;
            return false;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "login Ok, start get cookies!");
        System.out.println("login Ok, start get cookies!");

        HttpHeaders httpHeaders = loginResult.getHeaders();
        List<String> getCookies = httpHeaders.get("Set-Cookie");
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Set-Cookie: " + getCookies);
        System.out.println("Set-Cookie: " + getCookies);
        if (getCookies.size() >= 1) {
            cookies = new ArrayList<>();
            for (String cookie : getCookies) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "login, cookie:" + cookie);
                System.out.println("login, cookie:" + cookie);
                String[] cookieParse = cookie.split(";", 2);
                cookies.add(cookieParse[0]);
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "get cooke OK, cookie : " + cookies);
            System.out.println("get cooke OK, cookie : " + cookies);
        }

        loginSuccess = true;
        mcuSubscribeClientService.handshake(cookies);

        if (null == confSubcribeChannelMap) {
            confSubcribeChannelMap = new ConcurrentHashMap<>();
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "login successfully!! handshake:" + mcuSubscribeClientService.isHandShakeOk());
        System.out.println("login successfully!! handshake:" + mcuSubscribeClientService.isHandShakeOk());
        return mcuSubscribeClientService.isHandShakeOk();
    }

    public String createConference() {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "createConference, has login out!!!");
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
        createConferenceParam.setConf_type(mcuRestConfig.getConfType());
        createConferenceParam.setVideo_formats(mcuRestConfig.getVideoFormat());
        createConferenceParam.setAudio_formats(mcuRestConfig.getAudioFormat());

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "createConfParam:" + createConferenceParam.toString());
        System.out.println("createConfParam:" + createConferenceParam.toString());

        constructUrl(url, "/api/v1/mc/confs");
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(createConferenceParam);
        CreateConferenceResponse createConferenceResponse = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, CreateConferenceResponse.class);
        if (null == createConferenceResponse) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "createConferenceResponse is null");
            System.out.println("createConferenceResponse is null");
            return null;
        }

        if (createConferenceResponse.success()) {
            //创建会议成功，返回confId
            List<String> channels = new ArrayList<>();
            confSubcribeChannelMap.put(createConferenceResponse.getConf_id(), channels);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "createConferenceResponse, success:" + createConferenceResponse.getSuccess() + ", ConfId:" + createConferenceResponse.getConf_id());
            System.out.println("createConferenceResponse, success:" + createConferenceResponse.getSuccess() + ", ConfId:" + createConferenceResponse.getConf_id());
            return createConferenceResponse.getConf_id();
        } else {
            int errorCode = createConferenceResponse.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "create conference failed! errCode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
            System.out.println("create conference failed! errCode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
            return null;
        }
    }

    public McuStatus endConference(String confId, boolean deleteConf) {
        if (!loginSuccess) {
            return McuStatus.TimeOut;
        }

        McuBaseResponse response = null;
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "endConference :　");
        System.out.println("endConference :　");
        if (deleteConf) {
            StringBuilder url = new StringBuilder();
            constructUrl(url, "/api/v1/mc/confs/{conf_id}");
            Map<String, String> args = new HashMap<>();
            args.put("conf_id", confId);

            McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
            response = restClientService.exchange(url.toString(), HttpMethod.DELETE, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
            if (null == response) {
                return McuStatus.Unknown;
            }
        } else {
            synchronized (this) {
                List<String> channels = confSubcribeChannelMap.get(confId);
                if (channels == null || channels.isEmpty()) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "channels is null or isEmpty");
                    System.out.println("channels is null or isEmpty");
                    return McuStatus.OK;
                }
                if (null != channels) {
                    for (String channel : channels) {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "channel : " + channel);
                        System.out.println("channel : " + channel);
                        mcuSubscribeClientService.unsubscribe(channel);
                    }
                }
                channels.clear();
                confSubcribeChannelMap.remove(confId);
            }
        }

        if (null == response) {
            return McuStatus.OK;
        }

        return McuStatus.resolve(response.getError_code());
    }

    public List<JoinConferenceRspMtInfo> joinConference(String confId, List<Terminal> mts) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[joinConference] has login out!!!");
            System.out.println("[joinConference] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/mts");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        /*Map<String, String> confGroupMap = confInterfaceService.getConfGroupMap();
        String groupId = confGroupMap.get(confId);
        GroupConfInfo groupConfInfo = confInterfaceService.getGroupConfInfoMap().get(groupId);*/

        List<JoinConferenceMtInfo> joinConferenceMtInfos = new ArrayList<>();
        for (Terminal mt : mts) {
            String account = mt.getMtE164();
            JoinConferenceMtInfo joinConferenceMtInfo = new JoinConferenceMtInfo();
            joinConferenceMtInfo.setAccount(account);
            if (account.contains(".") || account.contains("::")) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[joinConference] terminal account is ip : " + account);
                System.out.println("[joinConference] terminal account is ip : " + account);
                joinConferenceMtInfo.setAccount_type(7);  //设置账号类型为IP
            }
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
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[joinConference] JoinConferenceResponse null!");
            System.out.println("[joinConference] JoinConferenceResponse null!");
            return null;
        }

        if (response.success()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[joinConference] JoinConferenceResponse success, confId : " + confId);
            System.out.println("[joinConference] JoinConferenceResponse success, confId : " + confId);
            /*if(!groupConfInfo.getCreatedConf().equals("mcu")){
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"[joinConference] JoinConferenceResponse success groupId : " + groupId +", groupConfInfo.getCreatedConf() : " +groupConfInfo.getCreatedConf());
                System.out.println("[joinConference] JoinConferenceResponse success groupId : " + groupId +", groupConfInfo.getCreatedConf() : " +groupConfInfo.getCreatedConf());
            }*/
            subscribeConfMts(confId);
            return response.getMts();
        } else {
            int errorCode = response.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[joinConference] join conference failed! errcode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
            System.out.println("[joinConference] join conference failed! errcode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
        }

        return null;
    }

    public McuStatus leftConference(String confId, List<TerminalId> mts, boolean delConf) {
        if (mts.isEmpty()) {
            return McuStatus.OK;
        }

        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "leftConference, has login out!!!");
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
        if (null == response) {
            return McuStatus.Unknown;
        }

        if (!response.success()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "left conference failed! errCode : " + response.getError_code());
            System.out.println("left conference failed! errCode : " + response.getError_code());
        }

        if (delConf) {
            endConference(confId, true);
        }

        if (response.success()) {
            return McuStatus.OK;
        }

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus inspections(String confId, String mode, String srcMtId, String dstMtId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "inspections, has login out!!!");
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
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "inspections, video, null == response");
                System.out.println("inspections, video, null == response");
                return McuStatus.Unknown;
            }

            if (!response.success()) {
                int errorCode = response.getError_code();
                McuStatus mcuStatus = McuStatus.resolve(response.getError_code());
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "inspections, dstMtId(" + dstMtId + ") inspect srcMtId(" + srcMtId + ") video failed! errcode : " + errorCode + ", errMsg : " + mcuStatus.getDescription());
                System.out.println("inspections, dstMtId(" + dstMtId + ") inspect srcMtId(" + srcMtId + ") video failed! errcode : " + errorCode + ", errMsg : " + mcuStatus.getDescription());
                return mcuStatus;
            }
        }

        if (mode.equals(InspectionModeEnum.ALL.getName()) || mode.equals(InspectionModeEnum.AUDIO.getName())) {
            mcuInspectionParam.setMode(InspectionModeEnum.AUDIO.getCode());
            mcuPostMsg.setParams(mcuInspectionParam);
            McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
            if (null == response) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "inspections, audio, null == response");
                System.out.println("inspections, audio, null == response");
                return McuStatus.Unknown;
            }
            if (!response.success()) {
                int errorCode = response.getError_code();
                McuStatus mcuStatus = McuStatus.resolve(response.getError_code());
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "inspections, dstMtId(" + dstMtId + ") inspect srcMtId(" + srcMtId + ") audio failed! errCode :" + errorCode + ", errMsg:" + mcuStatus.getDescription());
                System.out.println("inspections, dstMtId(" + dstMtId + ") inspect srcMtId(" + srcMtId + ") audio failed! errCode :" + errorCode + ", errMsg:" + mcuStatus.getDescription());
                return mcuStatus;
            }
        }

        return McuStatus.OK;
    }

    public McuStatus cancelInspection(String confId, String mode, String mtId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelInspection, has login out!!!");
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
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelInspection, video, null == response");
                System.out.println("cancelInspection, video, null == response");
                return McuStatus.Unknown;
            }
            if (!response.success()) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelInspection, video, cancel inspection(" + "mtId:" + mtId + ",confId:" + confId + ", mode: video) failed! errCode : " + response.getError_code());
                System.out.println("cancelInspection, video, cancel inspection(" + "mtId:" + mtId + ",confId:" + confId + ", mode: video) failed! errCode : " + response.getError_code());
                return McuStatus.resolve(response.getError_code());
            }
        }

        if (mode.equals(InspectionModeEnum.ALL.getName()) || mode.equals(InspectionModeEnum.AUDIO.getName())) {
            args.put("mode", "2");
            McuBaseResponse response = restClientService.exchange(url.toString(), HttpMethod.DELETE, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
            if (null == response) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelInspection, audio, null == response");
                System.out.println("cancelInspection, audio, null == response");
                return McuStatus.Unknown;
            }
            if (!response.success()) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelInspection, audio, cancel inspection(" + "mtId:" + mtId + ",confId:" + confId + ", mode: audio) failed! errCode : " + response.getError_code());
                System.out.println("cancelInspection, audio, cancel inspection(" + "mtId:" + mtId + ",confId:" + confId + ", mode: audio) failed! errCode : " + response.getError_code());
                return McuStatus.resolve(response.getError_code());
            }
        }

        return McuStatus.OK;
    }

    public String getDualStream(String confId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getDualStream, has login out!!!");
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
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getDualStream, null == getDualStreamResponse");
            System.out.println("getDualStream, null == getDualStreamResponse");
            return null;
        }

        if (!getDualStreamResponse.success()) {
            int errorCode = getDualStreamResponse.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getDualStream, failed! errcode:" + errorCode + ",errmsg:" + McuStatus.resolve(errorCode).getDescription());
            System.out.println("getDualStream, failed! errcode:" + errorCode + ",errmsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        return getDualStreamResponse.getMt_id();
    }

    public String getSpeaker(String confId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getSpeaker, has login out!!!");
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
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getSpeaker, null == getSpeakerResponse");
            System.out.println("getSpeaker, null == getSpeakerResponse");
            return null;
        }

        if (!getSpeakerResponse.success()) {
            int errorCode = getSpeakerResponse.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getSpeaker, failed! errcode:" + errorCode + ",errmsg:" + McuStatus.resolve(errorCode).getDescription());
            System.out.println("getSpeaker, failed! errcode:" + errorCode + ",errmsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        return getSpeakerResponse.getMt_id();
    }

    public McuStatus setSpeaker(String confId, String mtId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "setSpeaker, has login out!!!");
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
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "setSpeaker, exchange put, null == response");
            System.out.println("setSpeaker, exchange put, null == response");
            return McuStatus.Unknown;
        }

        if (!response.success()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "setSpeaker, exchange put failed! errcode:" + response.getError_code());
            System.out.println("setSpeaker, exchange put failed! errcode:" + response.getError_code());
            return McuStatus.resolve(response.getError_code());
        }

        return McuStatus.OK;
    }

    public McuStatus cancelSpeaker(String confId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelSpeaker, has login out!!!");
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
        if (null == response) {
            return McuStatus.Unknown;
        }

        if (response.success()) {
            return McuStatus.OK;
        }

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus ctrlCamera(String confId, String mtId, CameraCtrlParam cameraCtrlParam) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ctrlCamera, has login out!!!");
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
        if (null == response) {
            return McuStatus.Unknown;
        }

        if (response.success()) {
            return McuStatus.OK;
        }

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus sendIFrame(String confId, TransportAddress transportAddress) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "sendIFrame, has login out!!!");
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
        if (null == response) {
            return McuStatus.Unknown;
        }

        if (response.success()) {
            return McuStatus.OK;
        }

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus ctrlVolume(String confId, String mtId, McuCtrlVolumeParam ctrlVolumeParam) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ctrlVolume, has login out!!!");
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
        if (null == response) {
            return McuStatus.Unknown;
        }

        if (response.success()) {
            return McuStatus.OK;
        }

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus silenceOrMute(String confId, String mtId, boolean silence, SilenceOrMuteParam silenceOrMuteParam) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "silenceOrMute, has login out!!!");
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
        if (null == response) {
            return McuStatus.Unknown;
        }

        if (response.success()) {
            return McuStatus.OK;
        }

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus ctrlDualStream(String confId, String mtId, boolean dual) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ctrlDualStream, has login out!!!");
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

        if (null == response) {
            return McuStatus.Unknown;
        }

        if (response.success()) {
            return McuStatus.OK;
        }

        return McuStatus.resolve(response.getError_code());
    }

    public McuStatus onlineMts(String confId, OnlineMtsInfo onlineMtsInfo) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "onlineMts, has login out!!!");
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
        if (null == response) {
            return McuStatus.Unknown;
        }

        if (response.success()) {
            return McuStatus.OK;
        }

        return McuStatus.resolve(response.getError_code());
    }

    public Map<String, CascadeTerminalInfo> getCascadesTerminal(String confId, String cascadeId, boolean e164Key) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getCascadesTerminal, has login out!!!");
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
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getCascadesTerminal, null == getCascadesMtResponse");
            System.out.println("getCascadesTerminal, null == getCascadesMtResponse");
            return null;
        }

        if (!getCascadesMtResponse.success()) {
            int errorCode = getCascadesMtResponse.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getCascadesTerminal failed! errCode :" + errorCode + ", errMsg:" + McuStatus.resolve(errorCode).getDescription());
            System.out.println("getCascadesTerminal failed! errCode :" + errorCode + ", errMsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        List<CascadeTerminalInfo> terminalInfos = getCascadesMtResponse.getMts();
        if (null == terminalInfos || terminalInfos.isEmpty()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "GetCascadesMtResponse has no result!!");
            System.out.println("GetCascadesMtResponse has no result!!");
            return null;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getCascadesTerminal, total terminal num :" + terminalInfos.size());
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

    @Scheduled(initialDelay = heartbeatInterval, fixedRate = heartbeatInterval)
    public void doHearbeat() {
        //每25分钟执行一次心跳检测 /api/v1/system/heartbeat
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/system/heartbeat");

        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        ResponseEntity<McuBaseResponse> response = restClientService.postForEntity(url.toString(), mcuPostMsg.getMsg(), urlencodeMediaType, McuBaseResponse.class);
        if (null == response || response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            //尝试重新登陆
            loginSuccess = false;

            /*处理点对点呼叫的终端状态发布及呼叫挂断*/
            Map<String, P2PCallGroup> p2pCallGroupMap = ConfInterfaceService.p2pCallGroupMap;
            if (null != p2pCallGroupMap) {
                for (Map.Entry<String, P2PCallGroup> groupEntry : p2pCallGroupMap.entrySet()) {
                    P2PCallGroup p2PCallGroup = groupEntry.getValue();
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "groupId= " + groupEntry.getKey() + " and callGroup= " + groupEntry.getValue());
                    System.out.println("groupId = " + groupEntry.getKey() + " and callGroup = " + groupEntry.getValue());
                    for (Map.Entry<String, TerminalService> terminalEntry : p2PCallGroup.getCallMap().entrySet()) {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, " remoteCallMt: " + terminalEntry.getKey());
                        System.out.println("remoteCallMt: " + terminalEntry.getKey());

                        TerminalService terminalService = terminalEntry.getValue();
                        String groupId = terminalService.getGroupId();
                        terminalService.cancelCallMt();
                        TerminalManageService.publishStatus(terminalEntry.getKey(), groupId, TerminalOnlineStatusEnum.OFFLINE.getCode());

                        p2PCallGroup.removeCallMember(terminalEntry.getKey());
                    }
                }
            }

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
        }
    }

    @Scheduled(initialDelay = monitorsHeartbeatInterval, fixedRate = monitorsHeartbeatInterval)
    public void monitorsDoHearbeat() {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "monitorsDoHearbeat, has login out!!!");
            System.out.println("monitorsDoHearbeat, has login out!!!");
            return;
        }
        if(ConfInterfaceService.monitorsMemberHearbeat == null || ConfInterfaceService.monitorsMemberHearbeat.isEmpty()){
           /* LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "monitorsMemberHearbeat is null or empty  *****! current time : " + System.currentTimeMillis());
            System.out.println("monitorsMemberHearbeat is null or empty  *****! current time : " + System.currentTimeMillis());*/
            return;
        }
        for (Map.Entry<String, Map<String, MonitorsMember>> monitorsMemberHearbeat : ConfInterfaceService.monitorsMemberHearbeat.entrySet()) {
            //获取所有组的监看信息
            String confId = monitorsMemberHearbeat.getKey();
            Map<String, MonitorsMember> monitorsMembersMaps = monitorsMemberHearbeat.getValue();

            if(monitorsMembersMaps.isEmpty() || monitorsMembersMaps == null){
                /*LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "monitorsDoHearbeat monitorsMembersMaps is null or empty  *****! + "+ ", confId : " + confId);
                System.out.println("monitorsDoHearbeat monitorsMembersMaps is null or empty  *****! + "+ ", confId : " + confId);*/
                continue;
            }
            McuMonitorsHeartbeatParam mcuMonitorsHeartbeatParam = new McuMonitorsHeartbeatParam();
            List<McuMonitorsDst> monitors = new ArrayList<>();
            for (Map.Entry<String, MonitorsMember> monitorsMembersMap : monitorsMembersMaps.entrySet()){
                MonitorsMember monitorsMember = monitorsMembersMap.getValue();
                if(monitorsMember == null){
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "monitorsDoHearbeat monitorsMember is null or empty  *****! + "+ ", confId : " + confId);
                    System.out.println("monitorsDoHearbeat monitorsMember is null or empty  *****! + "+ ", confId : " + confId);
                    continue;
                }
                McuMonitorsDst mcuMonitorsDst = new McuMonitorsDst(monitorsMember.getDstIp(), monitorsMember.getPort());
                McuStatus mcuStatus = NeedMonistorsFrame(confId, monitorsMember.getDstIp(), monitorsMember.getPort());
                if(mcuStatus.getValue() == 200){
                   /* LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu monitorsDoHearbeat NeedMonistorsFrame is success " );
                    System.out.println("Mcu monitorsDoHearbeat NeedMonistorsFrame is success " );*/
                }else{
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu monitorsDoHearbeat NeedMonistorsFrame is failed  errcode : " + mcuStatus.getValue() + ", confId : " + confId +", dstIP : "+monitorsMember.getDstIp()+", port : "+monitorsMember.getPort());
                    System.out.println("Mcu monitorsDoHearbeat NeedMonistorsFrame is failed  errcode : " + mcuStatus.getValue() + ", confId : " + confId +", dstIP : "+monitorsMember.getDstIp()+", port : "+monitorsMember.getPort());
                }
                monitors.add(mcuMonitorsDst);
            }
            if (monitors.isEmpty()){
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "monitorsDoHearbeat monitors is null or empty  *****! + "+ ", confId : " + confId);
                System.out.println("monitorsDoHearbeat monitors is null or empty  *****! + "+ ", confId : " + confId);
                continue;
            }
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"confId : " + confId + ", monitorsDoHearbeat monitors : " +  monitors.toString());
            System.out.println("confId : " + confId + ", monitorsDoHearbeat monitors : " +  monitors.toString());
            mcuMonitorsHeartbeatParam.setMonitors(monitors);
            StringBuilder url = new StringBuilder();
            constructUrl(url, "/api/v1/vc/confs/{conf_id}/monitors_heartbeat");
            Map<String, String> args = new HashMap<>();
            args.put("conf_id", confId);
            McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
            mcuPostMsg.setParams(mcuMonitorsHeartbeatParam);
            McuBaseResponse MonitorsResponse = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);

            if (null == MonitorsResponse) {
                continue;
            }

            if (MonitorsResponse.success()) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"confId : " + confId + ", monitorsDoHearbeat is success " );
                System.out.println("confId : " + confId + ", monitorsDoHearbeat is success " );
                continue ;
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu MonitorsResponse getError_code() :" + MonitorsResponse.getError_code());
            System.out.println("Mcu MonitorsResponse getError_code() :" + MonitorsResponse.getError_code());
            continue ;
        }

    }

    private void constructUrl(StringBuilder url, String restApi) {
        //System.out.println("url length : " + url.length());

        if (url.length() > 0) {
            url.delete(0, url.length());
        }

        if ("prod".equals(activeProf)) {
            url.append("https://");
        } else {
            url.append("http://");
        }

        url.append(mcuRestConfig.getMcuIp());
        if (mcuRestConfig.getMcuRestPort() > 0) {
            url.append(":");
            url.append(mcuRestConfig.getMcuRestPort());
        }
        url.append(restApi);

        //System.out.println("url : " + url.toString());
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
            if (channel.equals(subscribeChannel.toString())) {
                return;
            }
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
            if (channel.equals(subscribeChannel.toString())) {
                return;
            }
        }

        //订阅会议终端信息 添加终端失败通知(/confs/{conf_id}/mts) 及 终端列表(/confs/{conf_id}/cascades/{cascade_id}/mts/{mt_id})
        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        subscribeChannelList.add(subscribeChannel.toString());

        subscribeChannel.delete(0, subscribeChannel.length());
        subscribeChannel.append("/confs/");
        subscribeChannel.append(confId);
        //subscribeChannel.append("/cascades/0/mts/*");
        subscribeChannel.append("/cascades/**");

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
            if (channel.equals(subscribeChannel.toString())) {
                return;
            }
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
            if (channel.equals(subscribeChannel.toString())) {
                return;
            }
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
            if (channel.equals(subscribeChannel.toString())) {
                return;
            }
        }

        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        subscribeChannelList.add(subscribeChannel.toString());
        confSubcribeChannelMap.put(confId, subscribeChannelList);
    }

    //订阅会议级联信息
    public void subscribeConfCascadesInfo(String confId) {
        //会议级联信息通道/confs/{conf_id}/cascades/**
        StringBuilder subscribeChannel = new StringBuilder();
        subscribeChannel.delete(0, subscribeChannel.length());
        subscribeChannel.append("/confs/");
        subscribeChannel.append(confId);
        subscribeChannel.append("/cascades/**");

        List<String> subscribeChannelList = confSubcribeChannelMap.get(confId);
        if (null == subscribeChannelList) {
            subscribeChannelList = Collections.synchronizedList(new ArrayList<>());
        }

        for (String channel : subscribeChannelList) {
            if (channel.equals(subscribeChannel.toString())) {
                return;
            }
        }

        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        subscribeChannelList.add(subscribeChannel.toString());
        confSubcribeChannelMap.put(confId, subscribeChannelList);
    }

    public void subscribeVmps(String confId) {
        ///confs/{conf_id}/vmps/{vmp_id}
        StringBuilder subscribeChannel = new StringBuilder();
        subscribeChannel.delete(0, subscribeChannel.length());
        subscribeChannel.append("/confs/");
        subscribeChannel.append(confId);
        subscribeChannel.append("/vmps/");
        subscribeChannel.append("1");

        List<String> subscribeChannelList = confSubcribeChannelMap.get(confId);
        if (null == subscribeChannelList) {
            subscribeChannelList = Collections.synchronizedList(new ArrayList<>());
        }

        for (String channel : subscribeChannelList) {
            if (channel.equals(subscribeChannel.toString())) {
                return;
            }
        }

        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        subscribeChannelList.add(subscribeChannel.toString());
        confSubcribeChannelMap.put(confId, subscribeChannelList);
    }

    public void subscribeMixs(String confId) {
        ///confs/{conf_id}/mixs/{mix_id}
        StringBuilder subscribeChannel = new StringBuilder();
        subscribeChannel.delete(0, subscribeChannel.length());
        subscribeChannel.append("/confs/");
        subscribeChannel.append(confId);
        subscribeChannel.append("/mixs/");
        subscribeChannel.append("1");

        List<String> subscribeChannelList = confSubcribeChannelMap.get(confId);
        if (null == subscribeChannelList) {
            subscribeChannelList = Collections.synchronizedList(new ArrayList<>());
        }

        for (String channel : subscribeChannelList) {
            if (channel.equals(subscribeChannel.toString())) {
                return;
            }
        }

        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        subscribeChannelList.add(subscribeChannel.toString());
        confSubcribeChannelMap.put(confId, subscribeChannelList);
    }

    //订阅所有会议信息
    public void subscribeAllConfInfo() {

        //会议级联信息通道/confs/**
        StringBuilder subscribeChannel = new StringBuilder();
        subscribeChannel.delete(0, subscribeChannel.length());
        subscribeChannel.append("/confs/*");

        List<String> subscribeChannelList = confSubcribeChannelMap.get("allConf");
        if (null == subscribeChannelList) {
            subscribeChannelList = Collections.synchronizedList(new ArrayList<>());
        }

        for (String channel : subscribeChannelList) {
            if (channel.equals(subscribeChannel.toString())) {
                return;
            }
        }

        mcuSubscribeClientService.subscribe(subscribeChannel.toString());
        subscribeChannelList.add(subscribeChannel.toString());
        confSubcribeChannelMap.put("allConf", subscribeChannelList);
    }

    public List<ConfsDetailRspInfo> queryConfs() {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[queryConfs] has login out!!!");
            System.out.println("[queryConfs] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("account_token", accountToken);

        ConfsInfoResponse response = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, ConfsInfoResponse.class);
        if (null == response) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[QueryConfs] JoinConferenceResponse null!");
            System.out.println("[QueryConfs] QueryConfs null!");
            return null;
        }

        if (response.success()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[QueryConfs] QueryConfs success");
            System.out.println("[QueryConfs] QueryConfs success");
            return response.getConfs();
        } else {
            int errorCode = response.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[QueryConfs] QueryConfs failed! errcode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
            System.out.println("[QueryConfs] QueryConfs failed! errcode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
        }

        return null;
    }

    public ConfInfoResponse queryConf(String confId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[queryConf] has login out!!!");
            System.out.println("[queryConf] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id",confId);
        args.put("account_token", accountToken);

        ConfInfoResponse confInfoResponse = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, ConfInfoResponse.class);
        if (null == confInfoResponse) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[queryConf] queryConf null!");
            System.out.println("[queryConf] queryConf null!");
            return null;
        }

        if (confInfoResponse.success()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[queryConf] queryConf success");
            System.out.println("[queryConf] queryConf success");
            return confInfoResponse;
        } else {
            int errorCode = confInfoResponse.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[confInfoResponse] confInfoResponse failed! errcode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
            System.out.println("[confInfoResponse] confInfoResponse failed! errcode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
        }

        return null;
    }

    public ConfsCascadesResponse queryConfsCascades(String confId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "queryConfsCascades, has login out!!!");
            System.out.println("queryConfsCascades, has login out!!!");
            return null;
        }
        //url : /api/v1/vc/confs/{conf_id}/cascades
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/cascades?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("account_token", accountToken);

        ConfsCascadesResponse confsCascadesResponse = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, ConfsCascadesResponse.class);
        if (null == confsCascadesResponse) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "confsCascadesResponse, null == confsCascadesResponse");
            System.out.println("confsCascadesResponse, null == confsCascadesResponse");
            return null;
        }

        if (!confsCascadesResponse.success()) {
            int errorCode = confsCascadesResponse.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "confsCascadesResponse, failed! errcode:" + errorCode + ",errmsg:" + McuStatus.resolve(errorCode).getDescription());
            System.out.println("confsCascadesResponse, failed! errcode:" + errorCode + ",errmsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        return confsCascadesResponse;
    }

    public List<ConfsCascadesMtsRspInfo> queryConfsCascadesMts(String confId, String cascadeId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[queryConfsCascadesMts] has login out!!!");
            System.out.println("[queryConfsCascadesMts] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/cascades/{cascade_id}/mts?account_token={account_token}");
        Map<String, Object> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("cascade_id", cascadeId);
        args.put("account_token", accountToken);

        ConfsCascadesMtsResponse response = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, ConfsCascadesMtsResponse.class);
        if (null == response) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[queryConfsCascadesMts] queryConfsCascadesMts null!");
            System.out.println("[queryConfsCascadesMts] queryConfsCascadesMts null!");
            return null;
        }

        if (response.success()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[queryConfsCascadesMts] queryConfsCascadesMts success");
            System.out.println("[queryConfsCascadesMts] queryConfsCascadesMts success");
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "response.getConfsCascadesMtsRspInfos : " + response.getMts());
            System.out.println("response.getConfsCascadesMtsRspInfos : " + response.getMts());
            return response.getMts();
        } else {
            int errorCode = response.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[queryConfsCascadesMts] queryConfsCascadesMts failed! errcode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
            System.out.println("[queryConfsCascadesMts] queryConfsCascadesMts failed! errcode : " + errorCode + ", errMsg : " + McuStatus.resolve(errorCode).getDescription());
        }

        return null;
    }

    public McuStatus sendMsm(String confId, SendSmsParam sendSmsParam, List<TerminalId> smsInfoMts) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[sendMsm] has login out!!!");
            System.out.println("[sendMsm] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/sms");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        SendSmsInfo sendSmsInfo = new SendSmsInfo(sendSmsParam.getMessage(), sendSmsParam.getType(), sendSmsParam.getRollNum(), sendSmsParam.getRollSpeed());
        sendSmsInfo.setMts(smsInfoMts);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu sendSmsInfo : " + sendSmsInfo.toString());
        System.out.println("Mcu sendSmsInfo : " + sendSmsInfo.toString());
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(sendSmsInfo);
        McuBaseResponse sendSmsResponse = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);

        if (null == sendSmsResponse) {
            return McuStatus.Unknown;
        }

        if (sendSmsResponse.success()) {
            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "sendSmsreSponse.getError_code() :" + sendSmsResponse.getError_code());
        System.out.println("sendSmsreSponse.getError_code() :" + sendSmsResponse.getError_code());
        return McuStatus.resolve(sendSmsResponse.getError_code());
    }

    public GetConfMtInfoResponse getConfMtInfo(String confId, String mtId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getCascadesTerminal, has login out!!!");
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
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getConfMtInfo, null == getCascadesMtResponse");
            System.out.println("getConfMtInfo, null == getCascadesMtResponse");
            return null;
        }

        if (!getConfMtInfoResponse.success()) {
            int errorCode = getConfMtInfoResponse.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getConfMtInfo failed! errCode :" + errorCode + ", errmsg:" + McuStatus.resolve(errorCode).getDescription());
            System.out.println("getConfMtInfo failed! errCode :" + errorCode + ", errmsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        return getConfMtInfoResponse;
    }

    //获取终端选看列表
    public ConfInspectionsResponse getConfinspections(String confId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getConfinspections, has login out!!!");
            System.out.println("getConfinspections, has login out!!!");
            return null;
        }

        ///api/v1/vc/confs/{conf_id}/inspections
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/inspections?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("account_token", accountToken);

        ConfInspectionsResponse confInspectionsResponse = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, ConfInspectionsResponse.class);
        if (null == confInspectionsResponse) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "confInspectionsResponse, null == confInspectionsResponse");
            System.out.println("confInspectionsResponse, null == confInspectionsResponse");
            return null;
        }

        if (!confInspectionsResponse.success()) {
            int errorCode = confInspectionsResponse.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "confInspectionsResponse failed! errCode :" + errorCode + ", errmsg:" + McuStatus.resolve(errorCode).getDescription());
            System.out.println("confInspectionsResponse failed! errCode :" + errorCode + ", errmsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        return confInspectionsResponse;
    }

    //开始画面合成
    public McuStatus startVmps(String confId, McuVmpsParam mcuVmpsParam) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[startVmps] has login out!!!");
            System.out.println("[startVmps] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/vmps");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu startVmps : " + mcuVmpsParam.toString());
        System.out.println("Mcu startVmps : " + mcuVmpsParam.toString());
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(mcuVmpsParam);
        McuBaseResponse stratVmpsResponse = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);

        if (null == stratVmpsResponse) {
            return McuStatus.Unknown;
        }

        if (stratVmpsResponse.success()) {
            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startVmps getError_code() :" + stratVmpsResponse.getError_code());
        System.out.println("startVmps getError_code() :" + stratVmpsResponse.getError_code());
        return McuStatus.resolve(stratVmpsResponse.getError_code());
    }

    //更新画面合成
    public McuStatus updateVmps(String confId, McuVmpsParam mcuVmpsParam) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[updatteVmps] has login out!!!");
            System.out.println("[updatteVmps] has login out!!!");
            return null;
        }
        ///api/v1/vc/confs/{conf_id}/vmps/{vmp_id}
        //会议主画面合成，默认vmp_id为1，异步操作，当未开启画面合成时返回错误
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/vmps/{vmp_id}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("vmp_id", "1");

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu updatteVmps : " + mcuVmpsParam.toString());
        System.out.println("Mcu updatteVmps : " + mcuVmpsParam.toString());
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(mcuVmpsParam);
        McuBaseResponse updateVmpsResponse = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);

        if (null == updateVmpsResponse) {
            return McuStatus.Unknown;
        }

        if (updateVmpsResponse.success()) {
            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "updatteVmps getError_code() :" + updateVmpsResponse.getError_code());
        System.out.println("updatteVmps getError_code() :" + updateVmpsResponse.getError_code());
        return McuStatus.resolve(updateVmpsResponse.getError_code());
    }

    //结束画面合成
    public McuStatus endVmps(String confId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[endVmps] has login out!!!");
            System.out.println("[endVmps] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        ///api/v1/vc/confs/{conf_id}/vmps/{vmp_id}
        //会议主画面合成，默认vmp_id为1，异步操作，当未开启画面合成时返回错误
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/vmps/{vmp_id}?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("vmp_id", "1");
        args.put("account_token", accountToken);

        McuBaseResponse endVmpsResponse = restClientService.exchange(url.toString(), HttpMethod.DELETE, null, urlencodeMediaType, args, McuBaseResponse.class);

        if (null == endVmpsResponse) {
            return McuStatus.Unknown;
        }

        if (endVmpsResponse.success()) {
            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "end Vmps getError_code() :" + endVmpsResponse.getError_code());
        System.out.println("end Vmps getError_code() :" + endVmpsResponse.getError_code());
        return McuStatus.resolve(endVmpsResponse.getError_code());
    }

    //获取画面合成信息
    public McuGetVmpsInfoResponse getVmpsInfo(String confId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[getVmpsInfo] has login out!!!");
            System.out.println("[getVmpsInfo] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        ///api/v1/vc/confs/{conf_id}/vmps/{vmp_id}
        //会议主画面合成，默认vmp_id为1，异步操作，当未开启画面合成时返回错误
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/vmps/{vmp_id}?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("vmp_id", "1");
        args.put("account_token", accountToken);

        McuGetVmpsInfoResponse mcuGetVmpsInfoResponse = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, McuGetVmpsInfoResponse.class);

        if (null == mcuGetVmpsInfoResponse) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mcuGetVmpsInfoResponse, null == mcuMixsInfoResponse");
            System.out.println("mcuGetVmpsInfoResponse, null == mcuGetVmpsInfoResponse");
            return null;
        }

        if (!mcuGetVmpsInfoResponse.success()) {
            int errorCode = mcuGetVmpsInfoResponse.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mcu Get Vmps Info Response failed! errCode :" + errorCode + ", errmsg:" + McuStatus.resolve(errorCode).getDescription());
            System.out.println("mcu Get Vmps Info Response failed! errCode :" + errorCode + ", errmsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        return mcuGetVmpsInfoResponse;
    }


    //开始混音
    public McuStatus startMix(String confId, McuStartMixparam mcuStartMixparam) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[startMix] has login out!!!");
            System.out.println("[startMix] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        ///api/v1/vc/confs/{conf_id}/mixs
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/mixs");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu start Mix : " + mcuStartMixparam.toString());
        System.out.println("Mcu start Mix : " + mcuStartMixparam.toString());
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(mcuStartMixparam);

        McuBaseResponse baseResponse = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "baseResponse : " + baseResponse);
        System.out.println("baseResponse : " + baseResponse);
        if (null == baseResponse) {
            return McuStatus.Unknown;
        }

        if (baseResponse.success()) {
            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "start Mix getError_code() :" + baseResponse.getError_code());
        System.out.println("start Mix getError_code() :" + baseResponse.getError_code());
        return McuStatus.resolve(baseResponse.getError_code());
    }

    //增加混音成员
    public McuStatus addMixMembers(String confId, McuMixMembers mcuMixMembers) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[addMixMembers] has login out!!!");
            System.out.println("[addMixMembers] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        ///api/v1/vc/confs/{conf_id}/mixs/{mix_id}/members
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/mixs/{mix_id}/members");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("mix_id", "1");

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu addMixMembers : " + mcuMixMembers.toString());
        System.out.println("Mcu addMixMembers : " + mcuMixMembers.toString());
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(mcuMixMembers);

        McuBaseResponse addMixsMembersResponse = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);

        if (null == addMixsMembersResponse) {
            return McuStatus.Unknown;
        }

        if (addMixsMembersResponse.success()) {
            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu addMixMembers getError_code() :" + addMixsMembersResponse.getError_code());
        System.out.println("Mcu addMixMembers getError_code() :" + addMixsMembersResponse.getError_code());
        return McuStatus.resolve(addMixsMembersResponse.getError_code());
    }

    //删除混音成员
    public McuStatus deleteMixMembers(String confId, McuMixMembers mcuMixMembers) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[deleteMixMembers] has login out!!!");
            System.out.println("[deleteMixMembers] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        ///api/v1/vc/confs/{conf_id}/mixs/{mix_id}/members
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/mixs/{mix_id}/members");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("mix_id", "1");

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu deleteMixMembers : " + mcuMixMembers.toString());
        System.out.println("Mcu deleteMixMembers : " + mcuMixMembers.toString());
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(mcuMixMembers);

        McuBaseResponse deleteMixsMembersResponse = restClientService.exchange(url.toString(), HttpMethod.DELETE, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);

        if (null == deleteMixsMembersResponse) {
            return McuStatus.Unknown;
        }

        if (deleteMixsMembersResponse.success()) {
            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu deleteMixMembers getError_code() :" + deleteMixsMembersResponse.getError_code());
        System.out.println("Mcu deleteMixMembers getError_code() :" + deleteMixsMembersResponse.getError_code());
        return McuStatus.resolve(deleteMixsMembersResponse.getError_code());
    }

    //结束混音
    public McuStatus endMixs(String confId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[endMixs] has login out!!!");
            System.out.println("[endMixs] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        ///api/v1/vc/confs/{conf_id}/mixs/{mix_id}
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/mixs/{mix_id}?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("mix_id", "1");
        args.put("account_token", accountToken);

        McuBaseResponse endMixsMembersResponse = restClientService.exchange(url.toString(), HttpMethod.DELETE, null, urlencodeMediaType, args, McuBaseResponse.class);

        if (null == endMixsMembersResponse) {
            return McuStatus.Unknown;
        }

        if (endMixsMembersResponse.success()) {
            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu end Mixs getError_code() :" + endMixsMembersResponse.getError_code());
        System.out.println("Mcu end Mixs getError_code() :" + endMixsMembersResponse.getError_code());
        return McuStatus.resolve(endMixsMembersResponse.getError_code());
    }

    //得到混音信息
    public McuMixsInfoResponse getMixsInfo(String confId) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[getMixsInfo] has login out!!!");
            System.out.println("[getMixsInfo] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        ///api/v1/vc/confs/{conf_id}/mixs/{mix_id}
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/mixs/{mix_id}?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("mix_id", "1");
        args.put("account_token", accountToken);

        McuMixsInfoResponse mcuMixsInfoResponse = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, McuMixsInfoResponse.class);

        if (null == mcuMixsInfoResponse) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mcuMixsInfoResponse, null == mcuMixsInfoResponse");
            System.out.println("mcuMixsInfoResponse, null == mcuMixsInfoResponse");
            return null;
        }

        if (!mcuMixsInfoResponse.success()) {
            int errorCode = mcuMixsInfoResponse.getError_code();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mcuMixsInfoResponse failed! errCode :" + errorCode + ", errmsg:" + McuStatus.resolve(errorCode).getDescription());
            System.out.println("mcuMixsInfoResponse failed! errCode :" + errorCode + ", errmsg:" + McuStatus.resolve(errorCode).getDescription());
            return null;
        }

        return mcuMixsInfoResponse;
    }

    //开始监看
    public McuStatus startMonitors(String confId, McuStartMonitorsParam mcuStartMonitorsParam) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[startMonitors] has login out!!!");
            System.out.println("[startMonitors] has login out!!!");
            return null;
        }
        McuMonitorsDst dst = mcuStartMonitorsParam.getDst();

        StringBuilder url = new StringBuilder();
        ///api/v1/vc/confs/{conf_id}/monitors
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/monitors");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu startMonitors : " + mcuStartMonitorsParam.toString());
        System.out.println("Mcu startMonitors : " + mcuStartMonitorsParam.toString());
        McuPostMsg mcuPostMsg = new McuPostMsg(accountToken);
        mcuPostMsg.setParams(mcuStartMonitorsParam);

        McuBaseResponse startMonitorsResponse = restClientService.exchange(url.toString(), HttpMethod.POST, mcuPostMsg.getMsg(), urlencodeMediaType, args, McuBaseResponse.class);

        if (null == startMonitorsResponse) {
            return McuStatus.Unknown;
        }

        if (startMonitorsResponse.success()) {
            McuStatus mcuStatus = NeedMonistorsFrame(confId, dst.getIp(), dst.getPort());
            if(mcuStatus.getValue() == 200){
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu startMonitors NeedMonistorsFrame is success " );
                System.out.println("Mcu startMonitors NeedMonistorsFrame is success " );
            }else{
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu startMonitors NeedMonistorsFrame is failed  errcode : " + mcuStatus.getValue() );
                System.out.println("Mcu startMonitors NeedMonistorsFrame is failed  errcode : " + mcuStatus.getValue());
            }

            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu startMonitors getError_code() :" + startMonitorsResponse.getError_code());
        System.out.println("Mcu startMonitors getError_code() :" + startMonitorsResponse.getError_code());
        return McuStatus.resolve(startMonitorsResponse.getError_code());
    }

    //取消监看
    public McuStatus deleteMonistors(String confId, String dstIp, int port) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[deleteMonistors] has login out!!!");
            System.out.println("[deleteMonistors] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        ///api/v1/vc/confs/{conf_id}/monitors/{dst_ip}/{dst_port}
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/monitors/{dst_ip}/{dst_port}?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("dst_ip", dstIp);
        args.put("dst_port", String.valueOf(port));
        args.put("account_token", accountToken);

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu deleteMonistors dst_ip : " + dstIp + ", dst_port : " + port);
        System.out.println("Mcu deleteMonistors dst_ip : " + dstIp + ", dst_port : " + port);


        McuBaseResponse deleteMonitorsResponse = restClientService.exchange(url.toString(), HttpMethod.DELETE, null, urlencodeMediaType, args, McuBaseResponse.class);

        if (null == deleteMonitorsResponse) {
            return McuStatus.Unknown;
        }

        if (deleteMonitorsResponse.success()) {

            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu deleteMonistors getError_code() :" + deleteMonitorsResponse.getError_code());
        System.out.println("Mcu deleteMonistors getError_code() :" + deleteMonitorsResponse.getError_code());
        return McuStatus.resolve(deleteMonitorsResponse.getError_code());
    }

    //获取监看关键帧
    public McuStatus NeedMonistorsFrame(String confId, String dstIp, int port) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[NeedMonistorsFrame] has login out!!!");
            System.out.println("[NeedMonistorsFrame] has login out!!!");
            return null;
        }

        StringBuilder needIframeUrl = new StringBuilder();
        ///api/v1/vc/confs/{conf_id}/neediframe/monitors
        constructUrl(needIframeUrl, "/api/v1/vc/confs/{conf_id}/neediframe/monitors");
        Map<String, String> needIframeArgs = new HashMap<>();
        needIframeArgs.put("conf_id", confId);

        McuMonitorsDst mcuMonitorsDst = new McuMonitorsDst(dstIp, port);
        McuMonitorsIframeParam mcuMonitorsIframeParam = new McuMonitorsIframeParam(mcuMonitorsDst);
        McuPostMsg needIframePostMsg = new McuPostMsg(accountToken);
        needIframePostMsg.setParams(mcuMonitorsIframeParam);
        McuBaseResponse needIframeResponse = restClientService.exchange(needIframeUrl.toString(), HttpMethod.POST, needIframePostMsg.getMsg(), urlencodeMediaType, needIframeArgs, McuBaseResponse.class);

        if (null == needIframeResponse) {
            return McuStatus.Unknown;
        }

        if (needIframeResponse.success()) {
            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu needIframeResponse getError_code() :" + needIframeResponse.getError_code());
        System.out.println("Mcu needIframeResponse getError_code() :" + needIframeResponse.getError_code());
        return McuStatus.resolve(needIframeResponse.getError_code());
    }

    //获取监看信息
    public McuStatus GetMonistors(String confId, String dstIp, int port) {
        if (!loginSuccess) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[GetMonistors] has login out!!!");
            System.out.println("[GetMonistors] has login out!!!");
            return null;
        }

        StringBuilder url = new StringBuilder();
        ///api/v1/vc/confs/{conf_id}/monitors/{dst_ip}/{dst_port}
        constructUrl(url, "/api/v1/vc/confs/{conf_id}/monitors/{dst_ip}/{dst_port}?account_token={account_token}");
        Map<String, String> args = new HashMap<>();
        args.put("conf_id", confId);
        args.put("dst_ip", dstIp);
        args.put("dst_port", String.valueOf(port));
        args.put("account_token", accountToken);

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu GetMonistors dst_ip : " + dstIp + ", dst_port : " + port + ", confId : " +confId);
        System.out.println("Mcu GetMonistors dst_ip : " + dstIp + ", dst_port : " + port + ", confId : " +confId);


        GetMonitorsInfoResponse getMonitorsInfoResponse = restClientService.exchange(url.toString(), HttpMethod.GET, null, urlencodeMediaType, args, GetMonitorsInfoResponse.class);

        if (null == getMonitorsInfoResponse) {
            return McuStatus.Unknown;
        }

        if (getMonitorsInfoResponse.success()) {
            return McuStatus.OK;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Mcu getMonitorsInfoResponse getError_code() :" + getMonitorsInfoResponse.getError_code());
        System.out.println("Mcu getMonitorsInfoResponse getError_code() :" + getMonitorsInfoResponse.getError_code());
        return McuStatus.resolve(getMonitorsInfoResponse.getError_code());
    }

    public Map<String, List<String>> getConfSubcribeChannelMap() {
        return confSubcribeChannelMap;
    }

    public void setConfSubcribeChannelMap(Map<String, List<String>> confSubcribeChannelMap) {
        this.confSubcribeChannelMap = confSubcribeChannelMap;
    }
    public void removeConfSubcribeChannelMap(String confId) {
        if(!confSubcribeChannelMap.containsKey(confId)){
            return;
        }
        confSubcribeChannelMap.remove(confId);
    }

    public void removeMcuSubscribe(String channel) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"removeMcuSubscribe channel : " +channel);
        System.out.println("removeMcuSubscribe channel : " +channel);
        mcuSubscribeClientService.unsubscribe(channel);
    }

    @Autowired
    private RestClientService restClientService;

    @Autowired
    private McuSubscribeClientService mcuSubscribeClientService;

    @Autowired
    private McuRestConfig mcuRestConfig;

    @Autowired
    private Environment env;

    @Autowired
    private TerminalMediaSourceService terminalMediaSourceService;


    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MediaType urlencodeMediaType = MediaType.APPLICATION_FORM_URLENCODED;
    private String accountToken;
    private List<String> cookies;
    //private final long heartbeatInterval = 25 * 60 * 1000L;
    private final long heartbeatInterval = 1 * 60 * 1000L;
    private final long monitorsHeartbeatInterval = 10000L;
    //private volatile boolean loginSuccess;
    public volatile boolean loginSuccess = false;
    private Map<String, List<String>> confSubcribeChannelMap;
    private static String activeProf = "dev";


}
