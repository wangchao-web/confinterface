package com.kedacom.confinterface.h323;

import com.kedacom.confadapter.*;
import com.kedacom.confinterface.dao.InspectionSrcParam;
import com.kedacom.confinterface.dto.MediaResource;
import com.kedacom.confinterface.dto.StartDualStreamRequest;
import com.kedacom.confinterface.dto.TerminalMediaResource;
import com.kedacom.confinterface.inner.DetailMediaResouce;
import com.kedacom.confinterface.inner.InspectedParam;
import com.kedacom.confinterface.restclient.mcu.InspectionStatusEnum;
import com.kedacom.confinterface.service.TerminalManageService;
import com.kedacom.confinterface.service.TerminalMediaSourceService;
import com.kedacom.confinterface.service.TerminalService;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class RegisterGkHandler implements Runnable{

    public void setH323TerminalService(H323TerminalService h323TerminalService) {
        this.h323TerminalService = h323TerminalService;
    }

    public static List<Future<Boolean>> getFutureList() {
        return futureList;
    }

    private H323TerminalService h323TerminalService;

    private static List<Future<Boolean>> futureList = Collections.synchronizedList(new ArrayList<>());

    @Override
    public synchronized void run(){
        Future<Boolean> regFuture = h323TerminalService.startRegGK();
        try {
            if (null != regFuture && regFuture.get()) {
                futureList.add(regFuture);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

public class H323TerminalManageService extends TerminalManageService implements IConferenceEventHandler {

    public H323TerminalManageService(H323ProtocalConfig h323ProtocalConfig) {
        super();
        this.protocalConfig = h323ProtocalConfig;
    }

    public H323ProtocalConfig getProtocalConfig() {
        return protocalConfig;
    }

    @Override
    public TerminalService createTerminal(String e164, boolean bVmt) {

        StringBuffer name = new StringBuffer();

        if (bVmt) {
            name.append(protocalConfig.getBaseSysConfig().getVmtNamePrefix());
            name.append(e164);
        } else {
            name.append("h323mt_");
            name.append(e164);
        }

        H323TerminalService h323TerminalService = new H323TerminalService(e164, name.toString(), bVmt, protocalConfig);
        createConfParticipant(h323TerminalService);
        if (bVmt)
            freeVmtServiceMap.put(e164, h323TerminalService);

        return h323TerminalService;
    }

    @Override
    public void StartUp() {
        //如果需要注册GK，则在此处完成所有虚拟终端的注册
        if (!protocalConfig.isUseGK()) {
            System.out.println("[H323VmtManageService] no need register gk");
            return;
        }

        int registerGkTerminalNum = freeVmtServiceMap.size() + usedVmtServiceMap.size();
        ExecutorService queueThreadPool = Executors.newFixedThreadPool(protocalConfig.getRegisterGkThreadNum());
        for (TerminalService h323VmtService : freeVmtServiceMap.values()) {
            RegisterGkHandler registerGkHandler = new RegisterGkHandler();
            registerGkHandler.setH323TerminalService((H323TerminalService) h323VmtService);
            queueThreadPool.execute(registerGkHandler);
        }

        //会议接入微服务异常重启后的Gk注册会涉及到usedVmtServiceMap
        for (TerminalService h323UsedVmtService : usedVmtServiceMap.values()){
            RegisterGkHandler registerGkHandler = new RegisterGkHandler();
            registerGkHandler.setH323TerminalService((H323TerminalService) h323UsedVmtService);
            queueThreadPool.execute(registerGkHandler);
        }

        queueThreadPool.shutdown();

        List<Future<Boolean>> futureList = RegisterGkHandler.getFutureList();
        while (true) {
            System.out.println("futureList size : " + futureList.size() + ", freeVmt : " + registerGkTerminalNum);
            if (futureList.size() == registerGkTerminalNum )
                break;

            try {
                TimeUnit.SECONDS.sleep(1);
            }catch (Exception e){

            }
        }

        System.out.println("[H323VmtManageService] finished start up, time : " + System.currentTimeMillis());
    }

    @Override
    @Async("confTaskExecutor")
    public void OnInvited(String participantid, ConferenceInfo conferenceInfo) {
        System.out.println("OnInvited, participantid : " + participantid + ", confId : " + conferenceInfo.getId()+", threadName:"+Thread.currentThread().getName());
        TerminalService terminalService = usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            System.out.println("OnInvited, not found participant!!");
            return;
        }

        terminalService.setConfId(conferenceInfo.getId());
        return;
    }

    @Override
    @Async("confTaskExecutor")
    public void OnParticipantJoined(String participantid, ConferencePresentParticipant conferencePresentParticipant) {
        System.out.println("OnParticipantJoined, terminal: " + conferencePresentParticipant.getId() + "join conference! confId : "
                + conferencePresentParticipant.getConf().getId()+", threadName:"+Thread.currentThread().getName());
    }

    @Override
    @Async("confTaskExecutor")
    public void OnParticipantLeft(String participantid, ConferencePresentParticipant conferencePresentParticipant) {
        System.out.println("OnParticipantLeft, terminal: "+ conferencePresentParticipant.getId() + "left conference! confId: "
                + conferencePresentParticipant.getConf().getId() + ", threadName:"+Thread.currentThread().getName());
        /*如果下线的是虚拟终端,则不需要额外的处理,因为虚拟终端不会选看或者被其他虚拟终端选看*/
        String offlineMtE164 = conferencePresentParticipant.getId();
        TerminalService offlineTerminal = findVmt(offlineMtE164);
        if (null != offlineTerminal) {
            System.out.println("OnParticipantLeft, offline terminal is vmt!!");
            return;
        }

        TerminalService terminalService = usedVmtServiceMap.get(participantid);
        if (terminalService.isInspection()){
            //判断选看的是否是下线的终端，如果是，则需要将选看状态清除
            InspectionSrcParam inspectionParam = terminalService.getInspectionParam();
            if (inspectionParam.getMtE164().equals(offlineMtE164)){
                System.out.println("OnParticipantLeft, inspection left terminal! dstMt:"+participantid);
                terminalService.setInspectionStatus(InspectionStatusEnum.UNKNOWN);
                terminalService.setInspectAudioStatus(InspectionStatusEnum.UNKNOWN.getCode());
                terminalService.setInspectVideoStatus(InspectionStatusEnum.UNKNOWN.getCode());

                //清除数据库中的选看资源
                terminalMediaSourceService.delGroupInspectionParam(participantid);
            }
        }

        if (terminalService.isInspected()){
            //是否被下线的终端选看
            InspectedParam inspectedParam = terminalService.getInspectedParam(offlineMtE164);
            if (null == inspectedParam)
                return;

            terminalService.delInspentedTerminal(offlineMtE164);
        }
    }

    @Override
    @Async("confTaskExecutor")
    public void OnKickedOff(String participantid) {
        System.out.println("OnKickedOff, terminal: "+ participantid + " is kicked off conference, threadName: "+Thread.currentThread().getName());
        H323TerminalService terminalService = (H323TerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService){
            System.out.println("OnKickedOff, not found terminal!");
            return;
        }

        usedVmtServiceMap.remove(participantid);
        freeVmtServiceMap.put(participantid, terminalService);

        //释放该虚拟终端的所有交换资源
        terminalService.clearExchange();

        //移除数据库中的资源
        terminalMediaSourceService.delTerminalMediaResource(participantid);
        terminalMediaSourceService.delGroupInspectionParam(participantid);
        terminalMediaSourceService.delGroupVmtMember(terminalService.getGroupId(), participantid);

        terminalService.leftConference();
    }

    @Override
    @Async("confTaskExecutor")
    public void OnLocalMediaRequested(String participantid, Vector<MediaDescription> mediaDescriptions) {
        System.out.println("OnLocalMediaRequested, request terminal: "+ participantid + " local media! threadName:" + Thread.currentThread().getName());
        H323TerminalService terminalService = (H323TerminalService)usedVmtServiceMap.get(participantid);
        if (null == terminalService){
            System.out.println("OnLocalMediaRequested, not found terminal : " + participantid);
            return;
        }

        boolean bOK = terminalService.onOpenLogicalChannel(mediaDescriptions);
        if (!bOK){
            System.out.println("OnLocalMediaRequested, onOpenLogicalChannel failed! participantid :"+participantid);
        }

        //将虚拟终端的资源更新到数据库中
        TerminalMediaResource terminalMediaResource = new TerminalMediaResource();
        terminalMediaResource.setMtE164(participantid);

        synchronized (terminalService) {
            TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(participantid);
            List<MediaResource> forwardResources = TerminalMediaResource.convertToMediaResource(terminalService.getForwardChannel(), "all");
            List<MediaResource> reverseResources = TerminalMediaResource.convertToMediaResource(terminalService.getReverseChannel(), "all");
            boolean bNeedUpdate = false;
            if (null == oldTerminalMediaResource) {
                bNeedUpdate = true;
                terminalMediaResource.setForwardResources(forwardResources);
                terminalMediaResource.setReverseResources(reverseResources);
                System.out.println("OnLocalMediaRequested, null == oldTerminalMediaResource, need update! E164:"+participantid+", forwardResources:"+forwardResources+", reverseResources:"+reverseResources);
            } else {
                List<MediaResource> oldForwardResources = oldTerminalMediaResource.getForwardResources();
                if (null == oldForwardResources || oldForwardResources.size() != forwardResources.size()){
                    System.out.println("OnLocalMediaRequested, forward resource need update! "+"E164:"+participantid+", oldResources:"+oldForwardResources+", newResource:"+forwardResources);
                    bNeedUpdate = true;
                    terminalMediaResource.setForwardResources(forwardResources);
                } else {
                    System.out.println("OnLocalMediaRequested, forward resource no need update! "+"E164:"+participantid+", Resources:"+oldForwardResources);
                    terminalMediaResource.setForwardResources(oldForwardResources);
                }

                List<MediaResource> oldReverseResources = oldTerminalMediaResource.getReverseResources();
                if (null == oldReverseResources || oldReverseResources.size() != reverseResources.size()){
                    System.out.println("OnLocalMediaRequested, reverse resource need update! "+"E164:"+participantid+", oldResources:"+oldReverseResources+", newResource:"+reverseResources);
                    bNeedUpdate = true;
                    terminalMediaResource.setReverseResources(reverseResources);
                } else {
                    System.out.println("OnLocalMediaRequested, reverse resource no need update! "+"E164:"+participantid+", Resources:"+oldReverseResources);
                    terminalMediaResource.setReverseResources(oldReverseResources);
                }
            }

            if (bNeedUpdate)
                terminalMediaSourceService.setTerminalMediaResource(terminalMediaResource);
        }
    }

    @Override
    @Async("confTaskExecutor")
    public void OnRemoteMediaReponsed(String participantid, Vector<MediaDescription> mediaDescriptions) {
        //该接口只有在使用H323协议时会用到
        System.out.println("OnRemoteMediaReponsed, request terminal: "+ participantid + " local media! threadName:"+Thread.currentThread().getName() );
        H323TerminalService terminalService = (H323TerminalService)usedVmtServiceMap.get(participantid);
        if (null == terminalService){
            System.out.println("OnRemoteMediaReponsed， not found terminal : " + participantid);
            return;
        }

        boolean bOk = terminalService.updateExchange(mediaDescriptions);
        if (bOk) {
            if (!mediaDescriptions.get(0).getDual()) {
                if (terminalService.getForwardGenericStreamNum().decrementAndGet() != 0)
                    return;

                 //如果主流全部开启，判断是否需要恢复辅流
                if (terminalService.getResumeDualStream().compareAndSet(true, false)){
                    boolean bResumeOk = terminalService.resumeDualStream();
                    if (!bResumeOk){
                        List<DetailMediaResouce> mediaResouces = terminalService.getForwardChannel();
                        TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(participantid);

                        if (oldTerminalMediaResource.getForwardResources().size() > mediaResouces.size()) {
                            oldTerminalMediaResource.setForwardResources(TerminalMediaResource.convertToMediaResource(mediaResouces, "all"));
                            terminalMediaSourceService.setTerminalMediaResource(oldTerminalMediaResource);
                        }
                    }
                }

                return;
            }

            //更新数据库中的正向双流交换资源信息
            List<DetailMediaResouce> mediaResouces = terminalService.getForwardChannel();
            TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(participantid);
            oldTerminalMediaResource.setForwardResources(TerminalMediaResource.convertToMediaResource(mediaResouces, "all"));
            terminalMediaSourceService.setTerminalMediaResource(oldTerminalMediaResource);

            StartDualStreamRequest startDualStreamRequest = (StartDualStreamRequest) terminalService.getWaitMsg(StartDualStreamRequest.class.getName());
            if (null == startDualStreamRequest)
                return;

            List<DetailMediaResouce> detailMediaResouces = terminalService.getForwardChannel();
            for (DetailMediaResouce detailMediaResouce : detailMediaResouces) {
                if (detailMediaResouce.getStreamIndex() != mediaDescriptions.get(0).getStreamIndex())
                    continue;

                System.out.println("OnRemoteMediaReponsed, dual streamIndex:"+mediaDescriptions.get(0).getStreamIndex());
                MediaResource mediaResource = new MediaResource();
                mediaResource.setType(detailMediaResouce.getType());
                mediaResource.setDual(true);
                mediaResource.setId(detailMediaResouce.getId());

                startDualStreamRequest.addResource(mediaResource);
                startDualStreamRequest.makeSuccessResponseMsg();
                terminalService.delWaitMsg(StartDualStreamRequest.class.getName());

                return;
            }
        }

        //失败处理
        if (mediaDescriptions.get(0).getDual()){
            StartDualStreamRequest startDualStreamRequest = (StartDualStreamRequest)terminalService.getWaitMsg(StartDualStreamRequest.class.getName());
            if(null == startDualStreamRequest)
                return;

            startDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.UPDATE_EXCHANGENODE_FAILED.getCode(), HttpStatus.OK, ConfInterfaceResult.UPDATE_EXCHANGENODE_FAILED.getMessage());
            terminalService.delWaitMsg(StartDualStreamRequest.class.getName());

            bOk = terminalService.closeDualStreamChannel();
            if (bOk){
                System.out.println("OnRemoteMediaReponsed, closeDualStreamChannel OK!");
            } else {
                System.out.println("OnRemoteMediaReponsed, closeDualStreamChannel failed!");
            }
        }
    }

    @Override
    @Async("confTaskExecutor")
    public void OnMediaCleaned(String participantid, Vector<MediaDescription> mediaDescriptions) {
        System.out.println("OnMediaCleaned, terminal: "+ participantid + ", media cleaned, threadName: "+Thread.currentThread().getName());
        H323TerminalService terminalService = (H323TerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService){
            System.out.println("OnMediaCleaned, not found terminal!");
            return;
        }

        List<String> resourceIds = new ArrayList<>();
        List<DetailMediaResouce> mediaResouces = terminalService.getReverseChannel();
        if (null == mediaResouces)
            return;

        for (MediaDescription mediaDescription : mediaDescriptions){
            for (DetailMediaResouce mediaResouce : mediaResouces){
                if (mediaResouce.getStreamIndex() != mediaDescription.getStreamIndex()){
                    continue;
                }

                System.out.println("OnMediaCleaned, type:"+mediaResouce.getType()+", resourceId:"+mediaResouce.getId()+", streamIndex:"+mediaDescription.getStreamIndex());
                resourceIds.add(mediaResouce.getId());
                break;
            }
        }

        if (resourceIds.isEmpty()) {
            return;
        }

        boolean bRemoveOk = terminalService.removeExchange(resourceIds);
        if (!bRemoveOk){
            System.out.println("onMediaCleaned, removeExchange failed!");
            resourceIds.clear();
            return;
        }

        synchronized (terminalService) {
            //删除对应的资源信息
            Iterator<String> resoureIdIterator = resourceIds.iterator();
            while (resoureIdIterator.hasNext()){
                String resourceId = resoureIdIterator.next();
                mediaResouces = terminalService.getReverseChannel();
                if (null == mediaResouces)
                    break;

                for(DetailMediaResouce mediaResouce : mediaResouces) {
                    if (mediaResouce.getId().equals(resourceId)) {
                        mediaResouces.remove(mediaResouce);
                        resoureIdIterator.remove();
                        break;
                    }
                }
            }

            mediaResouces = terminalService.getReverseChannel();
            TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(participantid);
            oldTerminalMediaResource.setReverseResources(TerminalMediaResource.convertToMediaResource(mediaResouces, "all"));
            terminalMediaSourceService.setTerminalMediaResource(oldTerminalMediaResource);
        }

        resourceIds.clear();
    }

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private H323ProtocalConfig protocalConfig;

    @Autowired
    private TerminalMediaSourceService terminalMediaSourceService;
}
