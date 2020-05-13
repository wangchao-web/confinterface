package com.kedacom.confinterface.dao;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dto.MonitorsMember;
import com.kedacom.confinterface.dto.TerminalMediaResource;
import com.kedacom.confinterface.redis.RedisClient;
import net.sf.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisTerminalMediaSourceDao implements TerminalMediaSourceDao {

    private final String confVmtListPrefix = "confVmtList_";
    private final String confGroupHashPrefix = "confGroupHash_";
    private final String mediaResourcePrefix = "mediaResource_";
    private final String broadcastSrcPrefix = "broadcastSrc_";
    private final String groupMtMembersPrefix = "groupMtMembers_";
    private final String groupVmtMembersPrefix = "groupVmtMembers_";
    private final String groupInspectionPrefix = "groupInspections_";
    private final String  monitorsResourcePrefix = "monitors_";
	
	private final String confMtListPrefix = "confMtList_";
	
    private final String publishUrlPrefix = "publishUrl_";
    private RedisClient redisClient;
    private String  srvToken;

    public RedisTerminalMediaSourceDao(RedisClient redisClient) {
        super();
        this.redisClient = redisClient;
    }

    @Override
    public String getSrvToken() {
        return srvToken;
    }

    @Override
    public void setSrvToken(String srvToken) {
        this.srvToken = srvToken;
    }

    @Override
    public List<String> getVmtList() {
        String key = keyGenernate(confVmtListPrefix, srvToken);
        return (List<String>) redisClient.listGet(key, 0, -1);
    }

    @Override
    public List<String> addVmt(String e164) {
        String key = keyGenernate(confVmtListPrefix, srvToken);
        redisClient.listPush(key, e164);
        return (List<String>) redisClient.listGet(key, 0, -1);
    }

    @Override
    public List<String> delVmt(String e164) {
        String key = keyGenernate(confVmtListPrefix, srvToken);
        redisClient.listRemove(key, 0, e164);
        return (List<String>) redisClient.listGet(key, 0, -1);
    }

    @Override
    public Map<String, String> getGroupsHash() {
        String key = keyGenernate(confGroupHashPrefix, srvToken);
        return (Map<String, String>) redisClient.hashGet(key);
    }

    @Override
    public Map<String, String> setGroup(String groupId, String confId) {
        String key = keyGenernate(confGroupHashPrefix, srvToken);
        redisClient.hashPut(key, groupId, confId);
        return (Map<String, String>)redisClient.hashGet(key);
    }

    @Override
    public Map<String, String>  delGroup(String groupId) {
        String key = keyGenernate(confGroupHashPrefix, srvToken);
        redisClient.hashRemove(key, groupId);
        return (Map<String, String>)redisClient.hashGet(key);
    }

    @Override
    public List<Terminal> addGroupMtMembers(String groupId, List<Terminal> mts) {
        String key = keyGenernate(groupMtMembersPrefix, groupId);
        for (Terminal mt : mts){
            redisClient.listPush(key, mt);
        }
        return getGroupMtMembers(groupId);
    }

    @Override
    public List<Terminal> delGroupMtMembers(String groupId, List<Terminal> mts) {
        String key = keyGenernate(groupMtMembersPrefix, groupId);
        if (null == mts){
            //删除group下的所有mt
            redisClient.listRemoveAll(key);
            return null;
        }

        for(Terminal terminal : mts){
            redisClient.listRemove(key, 0, terminal);
        }

        return (List<Terminal>)redisClient.listGet(key, 0, -1);
    }

    @Override
    public List<Terminal> getGroupMtMembers(String groupId) {
        String key = keyGenernate(groupMtMembersPrefix, groupId);
        if (!redisClient.keyExist(key)) {
            return null;
        }

        return (List<Terminal>)redisClient.listGet(key, 0, -1);
    }

    @Override
    public List<Terminal> setGroupMtMembers(String groupId, List<Terminal> mts) {
        String key = keyGenernate(groupMtMembersPrefix, groupId);
        for (Terminal mt : mts){
            redisClient.listPush(key, mt);
        }
        return (List<Terminal>)redisClient.listGet(key, 0, -1);
    }

    @Override
    public List<Terminal> addGroupVmtMembers(String groupId, List<Terminal> mts) {
        String key = keyGenernate(groupVmtMembersPrefix, groupId);
        for (Terminal mt : mts){
            redisClient.listPush(key, mt);
        }
        return getGroupVmtMembers(groupId);
    }

    @Override
    public List<Terminal> addGroupVmtMember(String groupId, String vmt) {
        String key = keyGenernate(groupVmtMembersPrefix, groupId);

        Terminal terminal = new Terminal(vmt);
        redisClient.listPush(key, terminal);

        return getGroupVmtMembers(groupId);
    }

    @Override
    public List<Terminal> delGroupVmtMembers(String groupId, List<Terminal> mts) {
        String key = keyGenernate(groupVmtMembersPrefix, groupId);
        if (null == mts){
            //删除group下的所有vmt
            redisClient.listRemoveAll(key);
            return null;
        }

        for (Terminal terminal : mts){
            redisClient.listRemove(key, 0, terminal);
        }

        return (List<Terminal>)redisClient.listGet(key, 0, -1);
    }

    @Override
    public List<Terminal> delGroupMtMember(String groupId, Terminal mtTerminal) {
        String key = keyGenernate(groupVmtMembersPrefix, groupId);
        redisClient.listRemove(key, 0, mtTerminal);
        return (List<Terminal>)redisClient.listGet(key, 0, -1);
    }

    @Override
    public List<Terminal> delGroupVmtMember(String groupId, String e164){
        String key = keyGenernate(groupVmtMembersPrefix, groupId);

        if (!redisClient.keyExist(key)) {
            return null;
        }

        Terminal terminal = new Terminal(e164);
        redisClient.listRemove(key, 0, terminal);

        return (List<Terminal>)redisClient.listGet(key, 0, -1);
    }

    @Override
    public List<Terminal> getGroupVmtMembers(String groupId) {
        String key = keyGenernate(groupVmtMembersPrefix, groupId);
        if (!redisClient.keyExist(key)) {
            return null;
        }

        return (List<Terminal>)redisClient.listGet(key, 0, -1);
    }

    @Override
    public List<Terminal> setGroupVmtMembers(String groupId, List<Terminal> mts) {
        String key = keyGenernate(groupVmtMembersPrefix, groupId);
        for (Terminal mt : mts){
            redisClient.listPush(key, mt);
        }

        return (List<Terminal>)redisClient.listGet(key, 0, -1);
    }

    @Override
    public InspectionSrcParam addInspectionParam(String e164, InspectionSrcParam inspectionParam) {
        String key = keyGenernate(groupInspectionPrefix, e164);
        redisClient.setValue(key, inspectionParam);
        return inspectionParam;
    }

    @Override
    public InspectionSrcParam getInspectionParam(String e164) {
        String key = keyGenernate(groupInspectionPrefix, e164);
        if (redisClient.keyExist(key)) {
            return (InspectionSrcParam)redisClient.getValue(key);
        }

        return null;
    }

    @Override
    public InspectionSrcParam delInspectionParam(String e164) {
        String key = keyGenernate(groupInspectionPrefix, e164);
        if (redisClient.keyExist(key)) {
            InspectionSrcParam inspectionParam = (InspectionSrcParam) redisClient.getValue(key);
            redisClient.delValue(key);
            return inspectionParam;
        }

        return null;
    }

    @Override
    public TerminalMediaResource getTerminalMediaResource(String mtE164) {
        String key = keyGenernate(mediaResourcePrefix, mtE164);
        if (redisClient.keyExist(key)) {
            String value = (String)redisClient.getValue(key);
            JSONObject jsonObject = JSONObject.fromObject(value);
            return  (TerminalMediaResource)JSONObject.toBean(jsonObject, TerminalMediaResource.class);
        }

        return null;
    }

    @Override
    public List<TerminalMediaResource> getTerminalMediaResources(String groupId) {
        String key = keyGenernate(groupMtMembersPrefix, groupId);
        if (!redisClient.keyExist(key)) {
            return null;
        }

        List<Terminal> mts = (List<Terminal>) redisClient.listGet(key, 0, -1);
        if (null == mts || mts.isEmpty()) {
            return null;
        }

        List<TerminalMediaResource> terminalMediaResources = new ArrayList<>();
        for (Terminal terminal : mts) {
            terminalMediaResources.add(getTerminalMediaResource(terminal.getMtE164()));
        }

        String vmtKey = keyGenernate(groupVmtMembersPrefix, groupId);
        if (!redisClient.keyExist(key)) {
            return terminalMediaResources;
        }

        List<Terminal> vmts = (List<Terminal>) redisClient.listGet(vmtKey, 0, -1);
        if (null == vmts || vmts.isEmpty()) {
            return terminalMediaResources;
        }

        for (Terminal vmt : vmts) {
            terminalMediaResources.add(getTerminalMediaResource(vmt.getMtE164()));
        }

        return terminalMediaResources;
    }

    @Override
    public TerminalMediaResource setTerminalMediaResource(TerminalMediaResource terminalMediaResource) {
        String key = keyGenernate(mediaResourcePrefix, terminalMediaResource.getMtE164());

        JSONObject jsonObject = JSONObject.fromObject(terminalMediaResource);
        String jsonString = jsonObject.toString();

        boolean bOk = redisClient.setValue(key, jsonString);
        if (bOk) {
            return terminalMediaResource;
        }

        return null;
    }

    @Override
    public TerminalMediaResource delTerminalMediaResource(String mtE164) {
        String key = keyGenernate(mediaResourcePrefix, mtE164);
        if (!redisClient.keyExist(key)) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"RedisTerminalMediaSourceDao, delTerminalMediaResource:"+mtE164+", not found terminal media resources!");
            System.out.println("RedisTerminalMediaSourceDao, delTerminalMediaResource:"+mtE164+", not found terminal media resources!");
            return null;
        }

        TerminalMediaResource terminalMediaResource = getTerminalMediaResource(mtE164);
        redisClient.delValue(key);
        return terminalMediaResource;
    }

    @Override
    public BroadcastSrcMediaInfo getBroadcastSrcInfo(String groupId){
        String key = keyGenernate(broadcastSrcPrefix, groupId);
        if (!redisClient.keyExist(key)){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"getBroadcastSrcInfo, not exist key:"+key);
            System.out.println("getBroadcastSrcInfo, not exist key:"+key);
            return null;
        }

        return (BroadcastSrcMediaInfo)redisClient.getValue(key);
    }

    @Override
    public BroadcastSrcMediaInfo setBroadcastSrcInfo(String groupId, BroadcastSrcMediaInfo broadcastSrcMediaInfo){
        String key = keyGenernate(broadcastSrcPrefix, groupId);
        boolean bOk = redisClient.setValue(key, broadcastSrcMediaInfo);
        if (bOk) {
            return broadcastSrcMediaInfo;
        }
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"setBroadcastSrcInfo , Failed!!! key:"+key);
        System.out.println("setBroadcastSrcInfo , Failed!!! key:"+key);
        return null;
    }

    @Override
    public BroadcastSrcMediaInfo delBroadcastSrcInfo(String groupId){
        String key = keyGenernate(broadcastSrcPrefix, groupId);
        if (!redisClient.keyExist(key)) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"delBroadcastSrcInfo, groupId:"+groupId+" not exist broadcast src info!");
            System.out.println("delBroadcastSrcInfo, groupId:"+groupId+" not exist broadcast src info!");
            return null;
        }

        BroadcastSrcMediaInfo broadcastSrcMediaInfo = getBroadcastSrcInfo(groupId);
        redisClient.delValue(key);
        return broadcastSrcMediaInfo;
    }

    @Override
    public ConcurrentHashMap<String, MonitorsMember> setMonitorsMembers(String confId, Map<String, MonitorsMember> monitorsMembers) {
        String key = keyGenernate(monitorsResourcePrefix, confId);
        redisClient.setValue(key, monitorsMembers);
        return (ConcurrentHashMap<String, MonitorsMember>)redisClient.getValue(key);
    }

    @Override
    public ConcurrentHashMap<String, MonitorsMember> deleteMonitorsMembers(String confId) {
        String key = keyGenernate(monitorsResourcePrefix, confId);
        if (!redisClient.keyExist(key)) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"deleteMonitorsMembers, "+confId+", not found this confId!");
            System.out.println("deleteMonitorsMembers, "+confId+", not found this confId!");
            return null;
        }
        ConcurrentHashMap<String, MonitorsMember> stringMonitorsMemberMap = getMonitorsMembers(confId);
        redisClient.delValue(key);
        return stringMonitorsMemberMap;
    }

    @Override
    public ConcurrentHashMap<String, MonitorsMember> getMonitorsMembers(String confId) {
        String key = keyGenernate(monitorsResourcePrefix, confId);
        if (!redisClient.keyExist(key)) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"gettMonitorsMembers, "+confId+", not found this confId!");
            System.out.println("gettMonitorsMembers, "+confId+", not found this confId!");
            return null;
        }
        return (ConcurrentHashMap<String, MonitorsMember>)redisClient.getValue(key);
    }


 //用于会议服务断开之后服务再启动时给上层业务推送失败的状态
    @Override
    public Map<String, String> setMtPublish(String E164, String publishUrl) {
        String key = keyGenernate(confMtListPrefix, srvToken);
        redisClient.hashPut(key, E164, publishUrl);
        return (Map<String, String>)redisClient.hashGet(key);
    }

    //用于会议服务断开之后服务再启动时给上层业务推送失败的状态
    @Override
    public Map<String, String> deleteMtPublish(String E164) {
        if(null == E164){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"deleteMtPublish, E164:"+ E164 +" not exist E164 !");
            System.out.println("deleteMtPublish, E164:"+ E164 +" not exist E164 !");
            return null;
        }
        String key = keyGenernate(confMtListPrefix, srvToken);
        redisClient.hashRemove(key, E164);
        return (Map<String, String>)redisClient.hashGet(key);
    }

    @Override
    public Map<String, String> getMtPublish() {
        String key = keyGenernate(confMtListPrefix, srvToken);
        return (Map<String, String>) redisClient.hashGet(key);
    }



    //用于保存订阅的路径
    @Override
    public Map<String, String> setPublishUrl(String groupId, String publishUrl) {
        String key = keyGenernate(publishUrlPrefix, srvToken);
        redisClient.hashPut(key, groupId, publishUrl);
        return (Map<String, String>)redisClient.hashGet(key);
    }

    @Override
    public Map<String, String> deletePublishUrl(String groupId) {
        if(null == groupId){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"deletePublishUrl, groupId:"+ groupId +" not exist groupId !");
            System.out.println("deletePublishUrl, groupId:"+ groupId +" not exist groupId !");
            return null;
        }
        String key = keyGenernate(publishUrlPrefix, srvToken);
        redisClient.hashRemove(key, groupId);
        return (Map<String, String>)redisClient.hashGet(key);
    }

    @Override
    public Map<String, String> getPublishUrl() {
        String key = keyGenernate(publishUrlPrefix, srvToken);
        return (Map<String, String>) redisClient.hashGet(key);
    }

    private String keyGenernate(String keyPrefix, String params) {
        StringBuilder keyResult = new StringBuilder(64);
        keyResult.append(keyPrefix);
        keyResult.append("_");
        keyResult.append(params);

        return keyResult.toString();
    }
}
