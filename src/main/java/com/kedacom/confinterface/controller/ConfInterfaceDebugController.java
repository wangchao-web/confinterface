package com.kedacom.confinterface.controller;

import com.kedacom.confinterface.dao.BroadcastSrcMediaInfo;
import com.kedacom.confinterface.dao.InspectionSrcParam;
import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.dto.*;
import com.kedacom.confinterface.inner.GroupConfInfo;
import com.kedacom.confinterface.service.ConfInterfaceService;
import com.kedacom.confinterface.service.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/services/confinterface/v1/debug")
public class ConfInterfaceDebugController {

    public ConfInterfaceDebugController(ConfInterfaceService confInterfaceService){
        super();
        this.confInterfaceService = confInterfaceService;
    }

    @GetMapping("/groups")
    public Map<String, String> getGroups(){
        Map<String, String> groups = confInterfaceService.getGroups();
        if (null == groups) {
            return new HashMap<>();
        }

        return groups;
    }

    @GetMapping("/groups/{groupId}/terminals")
    public List<Terminal> getTerminalsByGroupId(@PathVariable("groupId") String groupId){
        List<Terminal> terminals = confInterfaceService.getGroupMtMembers(groupId);
        if (null == terminals) {
            return new ArrayList<>();
        }

        return terminals;
    }

    @GetMapping("/groups/{groupId}/broadcastinfo")
    public BroadcastSrcMediaInfo getBroadcastByGroupId(@PathVariable("groupId") String groupId){
        BroadcastSrcMediaInfo broadcastSrcMediaInfo = confInterfaceService.getBroadcastSrc(groupId);
        if (null == broadcastSrcMediaInfo) {
            return new BroadcastSrcMediaInfo();
        }

        return broadcastSrcMediaInfo;
    }

    @GetMapping("/groups/{groupId}/inspections")
    public List<InspectionParam> getInspectionParamByGroupId(@PathVariable("groupId") String groupId){
        GroupConfInfo groupConfInfo = confInterfaceService.getGroupConfInfo(groupId);
        if (null == groupConfInfo) {
            return  new ArrayList<>();
        }

        Map<String, TerminalService> mtMembers = groupConfInfo.getMtMembers();
        if (null == mtMembers) {
            return new ArrayList<>();
        }

        List<InspectionParam> inspectionParams = new ArrayList<>();
        for(Map.Entry<String, TerminalService> mtMemberEntry : mtMembers.entrySet()) {
            TerminalService terminalService = mtMemberEntry.getValue();
            InspectionSrcParam inspectionSrcParam = terminalService.getInspectionParam();
            if (null == inspectionSrcParam) {
                continue;
            }

            InspectionParam inspectionParam = new InspectionParam();
            inspectionParam.setDstMtE164(terminalService.getE164());
            inspectionParam.setMode(inspectionSrcParam.getMode());

            if (null == groupConfInfo.getMtMember(inspectionSrcParam.getMtE164())){
                //说明选看的源是虚拟终端
                inspectionParam.setSrcMtE164("");
            } else {
                inspectionParam.setSrcMtE164(inspectionSrcParam.getMtE164());
            }

            inspectionParams.add(inspectionParam);
        }

        Map<String, TerminalService> vmtMembers = groupConfInfo.getUsedVmtMembers();
        if (null == vmtMembers) {
            return inspectionParams;
        }

        for (Map.Entry<String, TerminalService> vmtMemberEntry : vmtMembers.entrySet()){
            TerminalService terminalService = vmtMemberEntry.getValue();
            InspectionSrcParam inspectionSrcParam = terminalService.getInspectionParam();
            if (null == inspectionSrcParam) {
                continue;
            }

            InspectionParam inspectionParam = new InspectionParam();
            inspectionParam.setDstMtE164("");
            inspectionParam.setSrcMtE164(inspectionSrcParam.getMtE164());
            inspectionParam.setMode(inspectionSrcParam.getMode());
            inspectionParams.add(inspectionParam);
        }

        return inspectionParams;
    }

    @GetMapping("/groups/{groupId}/mediaresources")
    public List<TerminalMediaResource> getMediaResourcesByGroupId(@PathVariable("groupId") String groupId){
        List<TerminalMediaResource> terminalMediaResources = confInterfaceService.getTerminalMediaResources(groupId);
        if (null == terminalMediaResources) {
            return new ArrayList<>();
        }

        return  terminalMediaResources;
    }

    @GetMapping("/teststring")
    public DeferredResult<String> testString() {

        long currentTimeMillis = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + "进入testString方法");

        DeferredResult<String> deferredResult = new DeferredResult<>();
        confInterfaceService.testString(deferredResult);
        long currentTimeMillis1 = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + "从testString方法返回");
        System.out.println("testString耗时:" + (currentTimeMillis1 - currentTimeMillis) + "ms");
        //confInterfaceService.confTimeout(deferredResult);
        long currentTimeMillis2 = System.currentTimeMillis();
        System.out.println("testString退出耗时:" + (currentTimeMillis2 - currentTimeMillis1) + "ms");

        return deferredResult;
    }

    @PostMapping("/addGroup")
    public String testAddGroup(@RequestParam("GroupId") String groupId, @RequestBody JoinConferenceParam testAddGroup){
        System.out.println("testAddGroup, groupId : " + groupId);
        List<Terminal> terminals = testAddGroup.getMts();
        for (Terminal terminal : terminals){
            System.out.println("terminal : " + terminal.getMtE164());
        }

        return "add group ok, groupId : " + groupId;
    }

    @GetMapping("/teststring1")
    public DeferredResult<ResponseEntity<TestStringResponse>> testString1() {

        long currentTimeMillis = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + "进入testString1方法");

        TestStringRequest testRequest = new TestStringRequest();
        confInterfaceService.testString1(testRequest);
        long currentTimeMillis1 = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + "从testString1方法返回, 耗时：" + (currentTimeMillis1 - currentTimeMillis) + "ms");

        //confInterfaceService.confTimeout(deferredResult);

        long currentTimeMillis2 = System.currentTimeMillis();
        System.out.println("testString1退出耗时:" + (currentTimeMillis2 - currentTimeMillis1) + "ms");

        return testRequest.getResponseMsg();
    }

    @Autowired
    private ConfInterfaceService confInterfaceService;
}
