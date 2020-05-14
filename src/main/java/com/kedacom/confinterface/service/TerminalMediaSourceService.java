package com.kedacom.confinterface.service;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dao.InspectionSrcParam;
import com.kedacom.confinterface.dao.TerminalMediaSourceDao;
import com.kedacom.confinterface.dao.BroadcastSrcMediaInfo;
import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.dto.MonitorsMember;
import com.kedacom.confinterface.dto.TerminalMediaResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TerminalMediaSourceService {

    @Autowired
    private TerminalMediaSourceDao terminalMediaSourceDao;

    public String getSrvToken(){
        return terminalMediaSourceDao.getSrvToken();
    }
    public void setSrvToken(String srvToken){
        terminalMediaSourceDao.setSrvToken(srvToken);
    }

    @Cacheable(value = "vmtList", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public List<String> getVmtList() {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"getVmtList in TerminalMediaSourceService");
        System.out.println("getVmtList in TerminalMediaSourceService");
        return terminalMediaSourceDao.getVmtList();
    }

    @CachePut(value = "vmtList", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public List<String> addVmt(String e164) {
        return terminalMediaSourceDao.addVmt(e164);
    }

    @CacheEvict(value = "vmtList", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public List<String> delVmt(String e164){
        return terminalMediaSourceDao.delVmt(e164);
    }

    @Cacheable(value = "groupHash", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String, String> getGroups() {
        return terminalMediaSourceDao.getGroupsHash();
    }

    @CachePut(value = "groupHash", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String,String> setGroup(String groupId, String confId) {
        return terminalMediaSourceDao.setGroup(groupId, confId);
    }

    @CacheEvict(value = "groupHash", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String,String> delGroup(String groupId) {
        return terminalMediaSourceDao.delGroup(groupId);
    }

    @Cacheable(value = "groupMtMembers", key = "#groupId")
    public List<Terminal> getGroupMtMembers(String groupId) {
        return terminalMediaSourceDao.getGroupMtMembers(groupId);
    }

    @CachePut(value = "groupMtMembers", key = "#groupId")
    public List<Terminal> setGroupMtMembers(String groupId, List<Terminal> mts) {
        return terminalMediaSourceDao.setGroupMtMembers(groupId, mts);
    }

    @CachePut(value = "groupMtMembers", key = "#groupId")
    public List<Terminal> addGroupMtMembers(String groupId, List<Terminal> mts) {
        return terminalMediaSourceDao.addGroupMtMembers(groupId, mts);
    }

    @CacheEvict(value = "groupMtMembers", key = "#groupId")
    public List<Terminal> delGroupMtMembers(String groupId, List<Terminal> mts) {
        return terminalMediaSourceDao.delGroupMtMembers(groupId, mts);
    }

    @CacheEvict(value = "groupMtMembers", key = "#groupId")
    public List<Terminal> delGroupMtMember(String groupId, Terminal mtTerminal) {
        return terminalMediaSourceDao.delGroupMtMember(groupId, mtTerminal);
    }

    @Cacheable(value = "groupVmtMembers", key = "#groupId")
    public List<Terminal> getGroupVmtMembers(String groupId) {
        return terminalMediaSourceDao.getGroupVmtMembers(groupId);
    }

    @CachePut(value = "groupVmtMembers", key = "#groupId")
    public List<Terminal> setGroupVmtMembers(String groupId, List<Terminal> mts) {
        return terminalMediaSourceDao.setGroupVmtMembers(groupId, mts);
    }

    @CachePut(value = "groupVmtMembers", key = "#groupId")
    public List<Terminal> addGroupVmtMembers(String groupId, List<Terminal> mts) {
        return terminalMediaSourceDao.addGroupVmtMembers(groupId, mts);
    }

    @CachePut(value = "groupVmtMembers", key = "#groupId")
    public List<Terminal> addGroupVmtMember(String groupId, String vmt) {
        return terminalMediaSourceDao.addGroupVmtMember(groupId, vmt);
    }

    @CacheEvict(value = "groupVmtMembers", key = "#groupId")
    public List<Terminal> delGroupVmtMembers(String groupId, List<Terminal> mts) {
        return terminalMediaSourceDao.delGroupVmtMembers(groupId, mts);
    }

    @CacheEvict(value = "groupVmtMembers", key = "#groupId")
    public List<Terminal> delGroupVmtMember(String groupId, String e164) {
        return terminalMediaSourceDao.delGroupVmtMember(groupId, e164);
    }

    @Cacheable(value = "groupInspections", key = "#e164")
    public InspectionSrcParam getGroupInspectionParam(String e164) {
        return terminalMediaSourceDao.getInspectionParam(e164);
    }

    @CachePut(value = "groupInspections", key = "#dstMtE164")
    public InspectionSrcParam addGroupInspectionParam(String dstMtE164, InspectionSrcParam inspectionParam) {
        return terminalMediaSourceDao.addInspectionParam(dstMtE164, inspectionParam);
    }

    @CacheEvict(value = "groupInspections", key = "#e164")
    public InspectionSrcParam delGroupInspectionParam(String e164) {
        return terminalMediaSourceDao.delInspectionParam(e164);
    }

    @Cacheable(value = "TerminalMediaResource", key = "#mtE164")
    public TerminalMediaResource getTerminalMediaResource(String mtE164) {
        return terminalMediaSourceDao.getTerminalMediaResource(mtE164);
    }

    @Cacheable(value = "TerminalMediaResource", key = "#groupId")
    public List<TerminalMediaResource> getTerminalMediaResources(String groupId) {
        return terminalMediaSourceDao.getTerminalMediaResources(groupId);
    }

    @CachePut(value = "TerminalMediaResource", key = "#terminalMediaResource.mtE164")
    public TerminalMediaResource setTerminalMediaResource(TerminalMediaResource terminalMediaResource) {
        return terminalMediaSourceDao.setTerminalMediaResource(terminalMediaResource);
    }

    @CacheEvict(value = "TerminalMediaResource", key = "#mtE164")
    public TerminalMediaResource delTerminalMediaResource(String mtE164) {
        return terminalMediaSourceDao.delTerminalMediaResource(mtE164);
    }

    @Cacheable(value = "BroadcastSrcInfo", key = "#groupId")
    public BroadcastSrcMediaInfo getBroadcastSrcInfo(String groupId) {
        return terminalMediaSourceDao.getBroadcastSrcInfo(groupId);
    }

    @CachePut(value = "BroadcastSrcInfo", key = "#groupId")
    public BroadcastSrcMediaInfo setBroadcastSrcInfo(String groupId, BroadcastSrcMediaInfo broadcastSrcMediaInfo) {
        return terminalMediaSourceDao.setBroadcastSrcInfo(groupId, broadcastSrcMediaInfo);
    }

    @CacheEvict(value = "BroadcastSrcInfo", key = "#groupId")
    public BroadcastSrcMediaInfo delBroadcastSrcInfo(String groupId) {
        return terminalMediaSourceDao.delBroadcastSrcInfo(groupId);
    }

    @CachePut(value = "monitorsMembers", key = "#confId")
    public ConcurrentHashMap<String, MonitorsMember> setMonitorsMembers(String confId,Map<String, MonitorsMember> monitorsMembers) {
        return terminalMediaSourceDao.setMonitorsMembers(confId,monitorsMembers);
    }

    @CacheEvict(value = "monitorsMembers", key = "#confId")
    public ConcurrentHashMap<String, MonitorsMember> deleteMonitorsMembers(String confId) {
        return terminalMediaSourceDao.deleteMonitorsMembers(confId);
    }

    @Cacheable(value = "monitorsMembers", key = "#confId")
    public ConcurrentHashMap<String, MonitorsMember> getMonitorsMembers(String confId) {
        return terminalMediaSourceDao.getMonitorsMembers(confId);
    }
	
    @CachePut(value = "MtHash", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String, String> setMtPublish(String E164, String publishUrl) {
        return terminalMediaSourceDao.setMtPublish(E164, publishUrl);
    }

    @CacheEvict(value = "MtHash", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String, String> deleteMtPublish(String E164) {
        return terminalMediaSourceDao.deleteMtPublish(E164);
    }

    @Cacheable(value = "MtHash", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String, String> getMtPublish() {
        return terminalMediaSourceDao.getMtPublish();
    }

    @CachePut(value = "publishUrl", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String, String> setPublishUrl(String groupId, String publishUrl) {
        return terminalMediaSourceDao.setPublishUrl(groupId, publishUrl);
    }

    @CacheEvict(value = "publishUrl", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String, String> deletePublishUrl(String groupId) {
        return terminalMediaSourceDao.deletePublishUrl(groupId);
    }

    @Cacheable(value = "publishUrl", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String, String> getPublishUrl() {
        return terminalMediaSourceDao.getPublishUrl();
    }


    @Cacheable(value = "confIdHash", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String, String> getConfCreateTypeHash() {
        return terminalMediaSourceDao.getConfIdHash();
    }

    @CachePut(value = "confIdHash", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String,String> setConfCreateType(String confId, String createdConf) {
        return terminalMediaSourceDao.setConfId(confId, createdConf);
    }

    @CacheEvict(value = "confIdHash", key = "caches[0].name + '_' + #root.target.getSrvToken()")
    public Map<String,String> delConfCreateType(String confId) {
        return terminalMediaSourceDao.delConfId(confId);
    }
}
