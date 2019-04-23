package com.kedacom.confinterface;

import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.dto.BaseResponseMsg;
import com.kedacom.confinterface.dto.MediaResource;
import com.kedacom.confinterface.dto.TerminalMediaResource;
import com.kedacom.confinterface.event.SubscribeEvent;
import com.kedacom.confinterface.exchange.*;
import com.kedacom.confinterface.restclient.McuRestClientService;
import com.kedacom.confinterface.restclient.RestClientService;
import com.kedacom.confinterface.restclient.mcu.CascadeTerminalInfo;
import com.kedacom.confinterface.restclient.mcu.JoinConferenceRspMtInfo;
import com.kedacom.confinterface.restclient.mcu.McuStatus;
import com.kedacom.confinterface.service.GenerateE164Service;
import com.kedacom.confinterface.service.TerminalMediaSourceService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfinterfaceApplication.class)
public class ConfinterfaceApplicationTests implements ApplicationContextAware {
    private ApplicationContext context = null;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Test
    public void testGenE164(){
        GenerateE164Service.InitE164("00234561100009");
        String e164 = GenerateE164Service.generateE164();
        System.out.println("e164 : " +e164);
    }

    @Test
    public void testAddVmts(){
        List<Terminal> getMts = terminalMediaSourceService.getGroupVmtMembers("888888");
        if (null != getMts)
            System.out.println("getGroupVmtMembers, getMts : " + getMts.size());

        List<Terminal> vmtE164s = new ArrayList<>();
        Terminal terminal1 = new Terminal("1234560100004");
        vmtE164s.add(terminal1);

        Terminal terminal2 = new Terminal("1234560100005");
        vmtE164s.add(terminal2);

        Terminal terminal3 = new Terminal("1234560100006");
        vmtE164s.add(terminal3);

        List<Terminal> mts = terminalMediaSourceService.setGroupVmtMembers("888888", vmtE164s);
        System.out.println("add " + mts.size() + " vmts");

        for (Terminal terminal : mts){
            System.out.println("terminal : " + terminal.getMtE164());
        }

        terminalMediaSourceService.delGroupVmtMembers("888888", vmtE164s);

        return;
    }

    @Test
    public void testAddTerminalMediaResource(){
        TerminalMediaResource terminalMediaResource = new TerminalMediaResource();
        terminalMediaResource.setMtE164(mtE164);

        List<MediaResource> forward = new ArrayList<>();
        MediaResource mediaResource = new MediaResource();
        mediaResource.setType("Video");
        mediaResource.setId("8888888888888888");
        mediaResource.setDual(false);
        forward.add(mediaResource);
        terminalMediaResource.setForwardResources(forward);

        List<MediaResource> reverse = new ArrayList<>();
        MediaResource mediaResource1 = new MediaResource();
        mediaResource1.setDual(false);
        mediaResource1.setType("Video");
        mediaResource1.setId("999999999999999");
        reverse.add(mediaResource1);
        terminalMediaResource.setReverseResources(reverse);

        TerminalMediaResource result = terminalMediaSourceService.setTerminalMediaResource(terminalMediaResource);
        if (null != result){
            delTerminalResources = true;
            TerminalMediaResource getResult = terminalMediaSourceService.getTerminalMediaResource(mtE164);
            if (null != getResult)
                System.out.println(getResult);
        } else {
            delTerminalResources = false;
        }
    }

    @Test
    public void testToString(){
        MediaResource mediaResource = new MediaResource();
        System.out.println(mediaResource);

        mediaResource.setType("Video");
        mediaResource.setId("8888888888888888");
        mediaResource.setDual(false);

        System.out.println(mediaResource);

        TerminalMediaResource terminalMediaResource = new TerminalMediaResource();
        terminalMediaResource.setMtE164(mtE164);

        System.out.println(terminalMediaResource);
    }

    @Test
    public void testPublishEvent() {
        SubscribeEvent subscribeEvent = new SubscribeEvent(this, "8888888", "update", 0, "/llf/test/channel", null);
        context.publishEvent(subscribeEvent);
    }

    @Test
    public void testCreateConference(){
        String confId = mcuRestClientService.createConference();
        if (null == confId || confId.isEmpty()){
            System.out.println("create conference failed!");
        } else {
            System.out.println("create conference ok, confId : " + confId);

            McuStatus mcuStatus = mcuRestClientService.endConference(confId, true);
            if (mcuStatus.getValue() > 0){
                System.out.println("end conference failed! errmsg : " + mcuStatus.getDescription());
            } else {
                System.out.println("end conference ok!");
            }
        }

        return;
    }

    @Test
    public void testJoinConference(){

        String confId = mcuRestClientService.createConference();
        if (null == confId){
            System.out.println("create conference failed!");
            return;
        }

        List<Terminal> mts = new ArrayList<>();
        Terminal terminal = new Terminal(mtE164);
        mts.add(terminal);

        List<JoinConferenceRspMtInfo> rspMtInfos = mcuRestClientService.joinConference(confId, mts);
        if (null == rspMtInfos){
            System.out.println("join conference failed! confId : "+ confId);
            mcuRestClientService.endConference(confId, true);
            return;
        }

        System.out.println("join confernce ok!");
        for (JoinConferenceRspMtInfo mtInfo : rspMtInfos){
            System.out.println("E164:"+mtInfo.getAccount()+", mtId : "+mtInfo.getMt_id());
        }

        int queryTimes = 3;
        boolean online = false;
        while (queryTimes > 0){
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e){
                System.out.println("start get terminal info.........");
            }

            Map<String, CascadeTerminalInfo>  terminalInfoMap = mcuRestClientService.getCascadesTerminal(confId, "0", false);
            if (null == terminalInfoMap){
                System.out.println("getCascadesTerminal failed!");
                break;
            }

            for (Map.Entry<String, CascadeTerminalInfo> terminalInfoEntry : terminalInfoMap.entrySet()){
                if (!terminalInfoEntry.getValue().getE164().equals(mtE164)){
                    continue;
                }

                CascadeTerminalInfo terminalInfo = terminalInfoEntry.getValue();
                if (terminalInfo.getOnline() == 1){
                    System.out.println("E164 :"+mtE164+" is online!");
                    online = true;
                    break;
                } else {
                    System.out.println("E164 :"+mtE164+" is offline!!");
                }
            }

            queryTimes--;
            if(online)
                break;
        }

        mcuRestClientService.endConference(confId, true);
    }

    @Test
    public void testAddExchange(){
        addExchange(true);
    }

    @Test
    public void testUpdateExchange(){
        List<UpdateResourceParam> updateResourceParams = new ArrayList<>();
        updateResourceParams.add(createUpdateResourceParam(true));

        String url = "http://"+srvIp+":"+srvPort+"/services/media/v1/exchange?GroupID={groupId}&Action=updatenode";
        Map<String, String> args = new HashMap<>();
        args.put("groupId", groupId);

        for (UpdateResourceParam updateResourceParam : updateResourceParams){
            ResponseEntity<BaseResponseMsg> updateResponse = restClientService.exchangeJson(url, HttpMethod.POST, updateResourceParam, args, BaseResponseMsg.class);
            if (null == updateResponse)
                return;

            if (!updateResponse.getStatusCode().is2xxSuccessful()){
                System.out.println("update node failed! status : "+updateResponse.getStatusCodeValue()+", url:"+url);
                return;
            }

            if (updateResponse.getBody().getCode() != 0){
                System.out.println("update exchange failed, code: "+ updateResponse.getBody().getCode()+", message: "+updateResponse.getBody().getMessage());
                return;
            }

            System.out.println("update exchange oK");
        }
    }

    @Test
    public void testGetExchange(){
        addExchange(true);
        addExchange(false);

        String url = "http://"+srvIp+":"+srvPort+"/services/media/v1/exchange?GroupID={groupId}&Action=querynode";
        Map<String, String> args = new HashMap<>();
        args.put("groupId", groupId);

        QueryAndDelResourceParam queryResourceParam = new QueryAndDelResourceParam();
        queryResourceParam.setResourceIDs(resourceIDs);
        ResponseEntity<JSONObject> responseEntity = restClientService.exchangeJson(url, HttpMethod.POST, queryResourceParam, args, JSONObject.class);
        if (null == responseEntity)
            return;

        if (!responseEntity.getStatusCode().is2xxSuccessful()){
            System.out.println("query resource info failed! status :"+responseEntity.getStatusCodeValue());
            return;
        }

        JSONObject jsonObject = responseEntity.getBody();
        int code = jsonObject.getInt("code");
        if (code != 0){
            System.out.println("query resource info failed! errcord :"+code);
            return;
        }

        if (jsonObject.containsKey("exchangeNodeInfos")) {
            JSONArray exchangeNodeInfos = jsonObject.getJSONArray("exchangeNodeInfos");
            List<ExchangeInfo> exchangeInfos = JSONArray.toList(exchangeNodeInfos, new ExchangeInfo(), new JsonConfig());
            System.out.println("get exchange ok, exchangeInfo size : " + exchangeInfos.size());
            int exchangeInfoIndex = 0;
            for (ExchangeInfo exchangeInfo : exchangeInfos) {
                System.out.println("********* localsdp"+exchangeInfoIndex+" :");
                System.out.println(exchangeInfo.getLocalSdp());
                exchangeInfoIndex++;
            }
        } else {
            System.out.println("get exchange ok, but has no exchangeNodeInfos!!!!!!!!!!!!!!!!");
        }
    }

    @After
    public void destory(){
        delTerminalMediaResource();
        removeExchange();
    }

    public void delTerminalMediaResource(){
        if (!delTerminalResources)
            return;

        terminalMediaSourceService.delTerminalMediaResource(mtE164);
    }

    public void removeExchange(){
        if (null == resourceIDs || resourceIDs.isEmpty()) {
            System.out.println("no resource id need removed!!!!!");
            return;
        }

        System.out.println("now start remove exchange........");
        QueryAndDelResourceParam removeParam = new QueryAndDelResourceParam();
        removeParam.setResourceIDs(resourceIDs);

        String url = "http://"+srvIp+":"+srvPort+"/services/media/v1/exchange?GroupID={groupId}&Action=removenode";
        Map<String, String> args = new HashMap<>();
        args.put("groupId", groupId);

        ResponseEntity<BaseResponseMsg> removeResponse = restClientService.exchangeJson(url, HttpMethod.POST, removeParam, args, BaseResponseMsg.class);
        if (null == removeResponse) {
            System.out.println("remove exchange failed!!!!!!!");
            return;
        }

        if (!removeResponse.getStatusCode().is2xxSuccessful()){
            System.out.println("remove exchange failed!");
            return;
        }

        if (removeResponse.getBody().getCode() != 0) {
            System.out.println("remove resource failed! errcode : " + removeResponse.getBody().getCode());
            return;
        }
    }

    @BeforeClass
    public static void init(){
        if (null == resourceIDs){
            resourceIDs = new ArrayList<>();
        }
    }

    private String constructUpdateSdp(boolean video){
        StringBuilder sdp = new StringBuilder();
        sdp.append("c=IN IP4 ");
        sdp.append("172.16.64.25");
        sdp.append("\r\n");

        Random rand =new Random(System.currentTimeMillis());
        int port = 30000 + rand.nextInt(10000);

        if (video){
            sdp.append("m=video ");
            sdp.append(port);
            sdp.append(" RTP/AVP 106\r\n");
            sdp.append("f=v/2/6/25/1/4096/a////");
        } else {
            sdp.append("m=audio ");
            sdp.append(port);
            sdp.append(" RTP/AVP 0\r\n");
            sdp.append("f=v//////a/1/8/1/");
        }

        sdp.append("\r\n");

        System.out.println("constructUpdateSdp, video:"+video+", sdp:"+sdp.toString());
        return sdp.toString();
    }

    private String addExchange(boolean video){
        String url = "http://"+srvIp+":"+srvPort+"/services/media/v1/exchange?GroupID={groupId}&Action=addnode";
        Map<String, String> args = new HashMap<>();
        args.put("groupId", groupId);

        CreateResourceParam createResourceParam = new CreateResourceParam();
        StringBuilder sdp = new StringBuilder();
        if(video) {
            sdp.append("m=video\r\n");
            sdp.append("a=rtpmap:111 H264/90000\r\n");
            sdp.append("a=framerate:25");
        } else {
            sdp.append("m=audio\r\n");
            sdp.append("a=rtpmap:97 L16/8000/1");
        }

        sdp.append("\r\n");
        sdp.append("a=recvonly\r\n");

        createResourceParam.setSdp(sdp.toString());

        System.out.println("create param : " + sdp.toString());

        ResponseEntity<CreateResourceResponse> responseEntity = restClientService.exchangeJson(url, HttpMethod.POST, createResourceParam, args, CreateResourceResponse.class);
        if (null == responseEntity) {
            System.out.println("add exchange failed!!!!!!!");
            return null;
        }

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            System.out.println("addExchange, create resource failed! status : " + responseEntity.getStatusCodeValue());
            return null;
        }

        CreateResourceResponse resourceResponse = responseEntity.getBody();
        if (resourceResponse.getCode() != 0) {
            System.out.println("addExchange, create resource failed! errcode : " + resourceResponse.getCode()+", message:"+resourceResponse.getMessage());
            return null;
        }

        resourceIDs.add(resourceResponse.getResourceID());
        System.out.println("add exchange OK, resourceId : "+resourceResponse.getResourceID());
        System.out.println("response sdp:"+resourceResponse.getSdp());

        return resourceResponse.getResourceID();
    }

    private UpdateResourceParam createUpdateResourceParam(boolean video){
        String resourceId = addExchange(video);

        UpdateResourceParam updateResourceParam = new UpdateResourceParam(resourceId);
        updateResourceParam.setSdp(constructUpdateSdp(video));

        return updateResourceParam;
    }

    @Autowired
    private TerminalMediaSourceService terminalMediaSourceService;

    @Autowired
    private McuRestClientService mcuRestClientService;

    @Autowired
    private RestClientService restClientService;

    static protected List<String> resourceIDs;
    protected String groupId = "1234567890000001";
    protected String mtE164 = "1234561100000";
    protected String srvIp = "172.16.64.156";
    protected int srvPort = 8080;
    protected boolean delTerminalResources = false;
}
