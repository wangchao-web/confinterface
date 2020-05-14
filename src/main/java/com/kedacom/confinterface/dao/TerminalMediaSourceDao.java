package com.kedacom.confinterface.dao;

import com.kedacom.confinterface.dto.MonitorsMember;
import com.kedacom.confinterface.dto.TerminalMediaResource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface TerminalMediaSourceDao {

    String getSrvToken();
    void setSrvToken(String srvToken);

    List<String> getVmtList();

    List<String> addVmt(String e164);

    List<String> delVmt(String e164);

    Map<String, String> getGroupsHash();

    Map<String, String> setGroup(String groupId, String confId);

    ConcurrentHashMap<String, MonitorsMember> setMonitorsMembers(String confId, Map<String, MonitorsMember> monitorsMembers);

    ConcurrentHashMap<String, MonitorsMember> deleteMonitorsMembers(String confId);

    ConcurrentHashMap<String, MonitorsMember> getMonitorsMembers(String confId);

    Map<String, String> delGroup(String groupId);

    List<Terminal> getGroupMtMembers(String groupId);
    List<Terminal> setGroupMtMembers(String groupId, List<Terminal> mts);
    List<Terminal> addGroupMtMembers(String groupId, List<Terminal> mts);
    List<Terminal> delGroupMtMembers(String groupId, List<Terminal> mts);
    List<Terminal> delGroupMtMember(String groupId, Terminal mtTerminal);

    List<Terminal> getGroupVmtMembers(String groupId);
    List<Terminal> setGroupVmtMembers(String groupId, List<Terminal> mts);
    List<Terminal> addGroupVmtMembers(String groupId, List<Terminal> mts);
    List<Terminal> addGroupVmtMember(String groupId, String vmt);
    List<Terminal> delGroupVmtMembers(String groupId, List<Terminal> mts);
    List<Terminal> delGroupVmtMember(String groupId, String vmt);

    InspectionSrcParam addInspectionParam(String e164, InspectionSrcParam inspectionParam);
    InspectionSrcParam getInspectionParam(String e164);
    InspectionSrcParam delInspectionParam(String e164);

    TerminalMediaResource getTerminalMediaResource(String mtE164);

    List<TerminalMediaResource> getTerminalMediaResources(String groupId);

    TerminalMediaResource setTerminalMediaResource(TerminalMediaResource terminalMediaResource);

    TerminalMediaResource delTerminalMediaResource(String mtE164);

    BroadcastSrcMediaInfo getBroadcastSrcInfo(String groupId);

    BroadcastSrcMediaInfo setBroadcastSrcInfo(String groupId, BroadcastSrcMediaInfo broadcastSrcMediaInfo);

    BroadcastSrcMediaInfo delBroadcastSrcInfo(String groupId);

    Map<String, String> setMtPublish(String E164, String publishUrl);

    Map<String, String> deleteMtPublish(String E164);

    Map<String, String> getMtPublish();

    Map<String, String> setPublishUrl(String groupId, String publishUrl);

    Map<String, String> deletePublishUrl(String groupId);

    Map<String, String> getPublishUrl();

    Map<String, String> getConfIdHash();

    Map<String, String> setConfId(String confId, String createdConf);

    Map<String, String> delConfId(String confId);
}
