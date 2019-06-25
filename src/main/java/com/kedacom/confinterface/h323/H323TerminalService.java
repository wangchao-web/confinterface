package com.kedacom.confinterface.h323;


import com.kedacom.confadapter.common.ConferenceSysRegisterInfo;
import com.kedacom.confadapter.common.NetAddress;
import com.kedacom.confadapter.media.MediaDescription;
import com.kedacom.confinterface.dto.BaseRequestMsg;
import com.kedacom.confinterface.exchange.*;
import com.kedacom.confinterface.inner.DetailMediaResouce;
import com.kedacom.confinterface.inner.MediaTypeEnum;
import com.kedacom.confinterface.service.TerminalService;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class H323TerminalService extends TerminalService {

    public H323TerminalService(String e164, String name, boolean bVmt, H323ProtocalConfig h323ProtocalConfig) {
        super(e164, name, bVmt);
        this.regGK = false;
        this.gkIp = h323ProtocalConfig.getGkIp();
        this.gkRasPort = h323ProtocalConfig.getGkRasPort();
        this.localIp = h323ProtocalConfig.getBaseSysConfig().getLocalIp();
        this.localCallPort = h323ProtocalConfig.getLocalCallPort();
        this.localRasPort = h323ProtocalConfig.getLocalRasPort();
        this.videoDualStreamMediaDesc = null;
        this.dualStreamResourceId = null;
        this.resumeDualStream = new AtomicBoolean(false);
        this.forwardGenericStreamNum = new AtomicInteger(0);
    }

    public Future<Boolean> startRegGK() {
        if (regGK)
            return new AsyncResult<>(true);

        if (null == conferenceParticipant)
            return new AsyncResult<>(true);

        //注册Gk
        try {
            ConferenceSysRegisterInfo registerInfo = new ConferenceSysRegisterInfo();
            registerInfo.setId(e164);
            registerInfo.setName(name);
            registerInfo.setEndpointId(mtId);

            NetAddress gkAddress = new NetAddress();
            gkAddress.setIP(gkIp);
            gkAddress.setPort(gkRasPort);
            registerInfo.setConfRegServerAddr(gkAddress);

            NetAddress localAddress = new NetAddress();
            localAddress.setIP(localIp);
            localAddress.setPort(localCallPort);
            registerInfo.setCallLocalAddr(localAddress);

            NetAddress localRasAddress = new NetAddress();
            localRasAddress.setIP(localIp);
            localRasAddress.setPort(localRasPort);
            registerInfo.setRegClientAddr(localRasAddress);

            System.out.println("e164("+e164+") start register gk, time : "+System.currentTimeMillis()+", threadName:"+Thread.currentThread().getName());

            regGK = conferenceParticipant.RegisterToConfSys(registerInfo);
            if (regGK){
                System.out.println("register gk ok, time : "+System.currentTimeMillis());
                return new AsyncResult<>(true);
            } else {
                System.out.println("register gk failed, time : " + System.currentTimeMillis());
                return new AsyncResult<>(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AsyncResult<>(false);
        }
    }

    public boolean onOpenLogicalChannel(Vector<MediaDescription> mediaDescriptions){
        /*1.判断是否存在反向通道资源，存在，则直接查询
          2. 不存在，则请求创建
        * */
        boolean bCreate = true;
        boolean bOnlyDualStream = true;

        if (!mediaDescriptions.get(0).getDual()) {
            bOnlyDualStream = false;
            if (null == videoDualStreamMediaDesc
                    && mediaDescriptions.get(0).getMediaType().equals(MediaTypeEnum.VIDEO.getName())) {
                videoDualStreamMediaDesc = mediaDescriptions.get(0);
            }
        } else if (null == videoDualStreamMediaDesc
                    && mediaDescriptions.get(0).getMediaType().equals(MediaTypeEnum.VIDEO.getName())) {
            videoDualStreamMediaDesc = mediaDescriptions.get(0);
        }

        List<String> resourceInfo = new ArrayList<>();
        if (null != reverseChannel) {
            List<UpdateResourceParam> updateResourceParams = new ArrayList<>();
            for (DetailMediaResouce detailMediaResouce : reverseChannel) {
                if (!mediaDescriptions.get(0).getMediaType().equals(detailMediaResouce.getType()))
                    continue;

                if (mediaDescriptions.get(0).getDual() && detailMediaResouce.getDual() != 1
                        || !mediaDescriptions.get(0).getDual() && detailMediaResouce.getDual() == 1)
                    continue;

                int streamIndex = mediaDescriptions.get(0).getStreamIndex();
                if (detailMediaResouce.compareAndSetStreamIndex(-1, streamIndex)) {
                    //只有在异常重启后,回复上次的信息时,才会出现该情况
                    resourceInfo.add(detailMediaResouce.getId());
                    UpdateResourceParam updateResourceParam = new UpdateResourceParam(detailMediaResouce.getId());
                    updateResourceParam.setSdp(constructSdp(mediaDescriptions.get(0)));
                    updateResourceParams.add(updateResourceParam);
                    break;
                }
            }

            if (resourceInfo.size() > 0) {
                System.out.println("H323, onOpenLogicalChannel, exist reverseChannel info, start query exchange info, resourceInfoSize:" + resourceInfo.size());
                bCreate = false;

                List<ExchangeInfo> exchangeInfos = getExchange(resourceInfo);
                if (null == exchangeInfos) {
                    //如果查询不到资源节点，则清理相应的资源信息
                    for (DetailMediaResouce detailMediaResouce: reverseChannel){
                        if (!detailMediaResouce.getId().equals(resourceInfo.get(0)))
                            continue;

                        reverseChannel.remove(detailMediaResouce);
                        updateResourceParams.clear();
                        resourceInfo.clear();
                        break;
                    }

                    bCreate = true;
                } else {
                    boolean bOk = requestUpdateResource(updateResourceParams);
                    if (!bOk) {
                        removeMediaResource(false, resourceInfo);
                        return false;
                    }

                    updateMediaResource(true, exchangeInfos);
                }
            }
        }

        if (bCreate){
            System.out.println("H323, onOpenLogicalChannel, start add exchange info!! threadName:"+Thread.currentThread().getName());

            for (MediaDescription mediaDescription : mediaDescriptions) {
                System.out.println("mediaDescription:"+mediaDescription.toString());

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
        if (!bOk){
            //反向通道打开失败,释放流媒体资源
            removeMediaResource(false, resourceInfo);
            resourceInfo.clear();
            return bOk;
        }

        resourceInfo.clear();

        if (bOnlyDualStream)
            return true;

        //在收到主流的反向的通道打开时，发起正向主流通道的打开
        System.out.println("H323, start open forward logical channel...........");
        return openLogicalChannel(mediaDescriptions);
    }

    public boolean openLogicalChannel(Vector<MediaDescription> mediaDescriptions){
        Vector<MediaDescription> newMediaDescription  = new Vector<>();

        boolean bCreate = true;
        DetailMediaResouce needUpdateDetailMediaResouce = null;
        List<String> resourceInfo = new ArrayList<>();
        if (null != forwardChannel){
            //查询资源信息，并打开通道
            //1.异常重启后的恢复
            //2.此时打开的不是第一个通道
            for (DetailMediaResouce detailMediaResouce : forwardChannel) {
                if (!mediaDescriptions.get(0).getMediaType().equals(detailMediaResouce.getType())) {
                    continue;
                }

                //如果存在音视频都支持多条流时，避免重复处理同一条流
                if (detailMediaResouce.compareAndSetStreamIndex(-1, -2)) {
                    //说明是异常重启后的恢复
                    System.out.println("H323, openLogicalChannel, exist forward channel info, query exchange info!!!");
                    needUpdateDetailMediaResouce = detailMediaResouce;
                    bCreate = false;
                    resourceInfo.add(detailMediaResouce.getId());
                    List<ExchangeInfo> exchangeInfos = getExchange(resourceInfo);
                    if (null == exchangeInfos) {
                        //清理资源
                        for(DetailMediaResouce cleanResource : forwardChannel){
                            if (!cleanResource.getId().equals(resourceInfo.get(0)))
                                continue;

                            bCreate = true;
                            forwardChannel.remove(cleanResource);
                            resourceInfo.clear();
                            needUpdateDetailMediaResouce = null;
                            break;
                        }
                        break;
                    }

                    //更新正向媒体资源信息
                    updateMediaResource(false, exchangeInfos);
                    newMediaDescription.add(constructRequestMediaDescription(mediaDescriptions.get(0), exchangeInfos.get(0).getLocalSdp()));
                    break;
                }
            }
        }

        CreateResourceResponse resourceResponse = null;
        if (bCreate) {
            System.out.println("H323, openLogicalChannel, start add exchange info!!! threadName:" + Thread.currentThread().getName());

            CreateResourceParam createResourceParam = new CreateResourceParam();
            createResourceParam.setSdp(constructCreateSdp(mediaDescriptions.get(0)));
            resourceResponse = addExchange(createResourceParam);
            if (null == resourceResponse)
                return false;

            resourceInfo.add(resourceResponse.getResourceID());
            newMediaDescription.add(constructRequestMediaDescription(mediaDescriptions.get(0), resourceResponse.getSdp()));
        }
        boolean bOk = false;
        synchronized (this) {
             bOk = conferenceParticipant.RequestRemoteMedia(newMediaDescription);
            if (!bOk) {
                System.out.println("H323, openLogicalChannel, RequestRemoteMedia failed! participartId : " + e164);
                removeMediaResource(true, resourceInfo);
            } else {
                System.out.println("H323, openLogicalChannel, RequestRemoteMedia Ok, participartId : " + e164);
                if (bCreate) {
                    addMediaResource(newMediaDescription.get(0).getStreamIndex(), newMediaDescription.get(0).getDual(), resourceResponse);
                } else {
                    //更新流ID
                    needUpdateDetailMediaResouce.setStreamIndex(newMediaDescription.get(0).getStreamIndex());
                }
            }
        }
        newMediaDescription.clear();
        resourceInfo.clear();
        return bOk;
    }

    public void openDualStreamChannel(BaseRequestMsg startDualStreamRequest){
        if (null != forwardChannel) {
            for (DetailMediaResouce detailMediaResouce : forwardChannel) {
                if (detailMediaResouce.getDual() == 1) {
                    startDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.EXIST_DUALSTREAM.getCode(), HttpStatus.OK, ConfInterfaceResult.EXIST_DUALSTREAM.getMessage());
                    return;
                }
            }
        }

        videoDualStreamMediaDesc.setStreamIndex(-1);
        videoDualStreamMediaDesc.setDual(true);
        CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setSdp(constructCreateSdp(videoDualStreamMediaDesc));
        CreateResourceResponse resourceResponse = addExchange(createResourceParam);
        if (null == resourceResponse) {
            System.out.println("openDualStreamChannel, addExchange failed!");
            startDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.ADD_EXCHANGENODE_FAILED.getCode(), HttpStatus.OK, ConfInterfaceResult.ADD_EXCHANGENODE_FAILED.getMessage());
            return;
        }

        Vector<MediaDescription> newMediaDescription = new Vector<>();
        MediaDescription requestRemoteMedia = constructRequestMediaDescription(videoDualStreamMediaDesc, resourceResponse.getSdp());
        newMediaDescription.add(requestRemoteMedia);

        boolean bOk = conferenceParticipant.RequestRemoteMedia(newMediaDescription);
		if (!bOk) {
            System.out.println("H323, openDualStreamChannel, RequestRemoteMedia failed! participartId : " + e164);
            //释放交换资源
            List<String> resourceIds = new ArrayList<>();
            resourceIds.add(resourceResponse.getResourceID());
            removeExchange(resourceIds);
			newMediaDescription.clear();
            return;
        }

        int dualStreamIndex = newMediaDescription.get(0).getStreamIndex();
        videoDualStreamMediaDesc.setStreamIndex(dualStreamIndex);
        dualStreamResourceId = resourceResponse.getResourceID();
        addWaitMsg(startDualStreamRequest.getClass().getName(), startDualStreamRequest);
        addMediaResource(dualStreamIndex, videoDualStreamMediaDesc.getDual(), resourceResponse);

        System.out.println("openDualStreamChannel, RequestRemoteMedia Ok! streamIndex:"+dualStreamIndex);

		newMediaDescription.clear();
    }

    public boolean resumeDualStream(){
        videoDualStreamMediaDesc.setStreamIndex(-1);
        videoDualStreamMediaDesc.setDual(true);

        List<String> resourceInfo = new ArrayList<>();
        resourceInfo.add(dualStreamResourceId);
        List<ExchangeInfo> exchangeInfos = getExchange(resourceInfo);
        if (null == exchangeInfos) {
            System.out.println("resumeDualStream, getExchange failed! resourceId:"+dualStreamResourceId);
            return false;
        }

        updateMediaResource(false, exchangeInfos);

        Vector<MediaDescription> requestMediaDescription  = new Vector<>();
        requestMediaDescription.add(constructRequestMediaDescription(videoDualStreamMediaDesc, exchangeInfos.get(0).getLocalSdp()));

        boolean bOk = conferenceParticipant.RequestRemoteMedia(requestMediaDescription);
        if (!bOk) {
            System.out.println("H323, openDualStreamChannel, RequestRemoteMedia failed! participartId : " + e164);
            //释放交换资源
            removeMediaResource(true, resourceInfo);
			requestMediaDescription.clear();

            return false;
        }

        int dualStreamIndex = requestMediaDescription.get(0).getStreamIndex();
        videoDualStreamMediaDesc.setStreamIndex(dualStreamIndex);

        for (DetailMediaResouce detailMediaResouce : forwardChannel){
            if (!detailMediaResouce.getId().equals(dualStreamResourceId))
                continue;

            detailMediaResouce.setStreamIndex(dualStreamIndex);
            break;
        }

		requestMediaDescription.clear();
        return true;
    }

    public boolean closeDualStreamChannel(){
        if (null == videoDualStreamMediaDesc || null == forwardChannel || null == dualStreamResourceId)
            return true;

        System.out.println("closeDualStreamChannel, streamIndex : "+videoDualStreamMediaDesc.getStreamIndex());
        boolean bOk = conferenceParticipant.RequestCleanupMediaById(videoDualStreamMediaDesc.getStreamIndex());
        if (!bOk){
            System.out.println("closeDualStreamChannel, RequestCleanupMediaById failed!");
            return false;
        }

        List<String> resourceIds = new ArrayList<>();
        resourceIds.add(dualStreamResourceId);
        if (removeMediaResource(true, resourceIds))
            dualStreamResourceId = null;

        return true;
    }

    public boolean isRegGK() {
        return regGK;
    }

    public String getGkIp() {
        return gkIp;
    }

    public void setGkIp(String gkIp) {
        this.gkIp = gkIp;
    }

    public int getGkRasPort() {
        return gkRasPort;
    }

    public void setGkRasPort(int gkRasPort) {
        this.gkRasPort = gkRasPort;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public int getLocalRasPort() {
        return localRasPort;
    }

    public void setLocalRasPort(int localRasPort) {
        this.localRasPort = localRasPort;
    }

    public int getLocalCallPort() {
        return localCallPort;
    }

    public void setLocalCallPort(int localCallPort) {
        this.localCallPort = localCallPort;
    }

    public void addForwardChannel(DetailMediaResouce detailMediaResouce) {
        super.addForwardChannel(detailMediaResouce);

        if (detailMediaResouce.getDual() != 1){
            forwardGenericStreamNum.incrementAndGet();
        } else if (detailMediaResouce.getStreamIndex() == -1){
            dualStreamResourceId = detailMediaResouce.getId();
            resumeDualStream.set(true);
        }
    }

    public AtomicBoolean getResumeDualStream() {
        return resumeDualStream;
    }

    public AtomicInteger getForwardGenericStreamNum() {
        return forwardGenericStreamNum;
    }

    private boolean regGK;
    private String gkIp;
    private int gkRasPort;
    private String localIp;
    private int localRasPort;
    private int localCallPort;
    protected MediaDescription videoDualStreamMediaDesc;
    protected String dualStreamResourceId;
    protected AtomicBoolean resumeDualStream;
    protected AtomicInteger forwardGenericStreamNum;
}
