package com.kedacom.confinterface.h323;


import com.kedacom.confadapter.common.ConferenceSysRegisterInfo;
import com.kedacom.confadapter.common.NetAddress;
import com.kedacom.confadapter.media.MediaDescription;
import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dto.BaseRequestMsg;
import com.kedacom.confinterface.dto.MediaResource;
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

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "e164(" + e164 + ") start register gk, time : " + System.currentTimeMillis() + ", threadName:" + Thread.currentThread().getName());
            System.out.println("e164(" + e164 + ") start register gk, time : " + System.currentTimeMillis() + ", threadName:" + Thread.currentThread().getName());

            regGK = conferenceParticipant.RegisterToConfSys(registerInfo);
            if (regGK) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "register gk ok, time : " + System.currentTimeMillis());
                System.out.println("register gk ok, time : " + System.currentTimeMillis());
                return new AsyncResult<>(true);
            } else {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "register gk failed, time : " + System.currentTimeMillis());
                System.out.println("register gk failed, time : " + System.currentTimeMillis());
                return new AsyncResult<>(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AsyncResult<>(false);
        }
    }

    public boolean onOpenLogicalChannel(Vector<MediaDescription> mediaDescriptions) {
        /*1.判断是否存在反向通道资源，存在，则直接查询
          2. 不存在，则请求创建
        * */
        boolean bCreate = true;
        boolean bOnlyDualStream = true;
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mediaDescriptions.get(0).getDual() : " + mediaDescriptions.get(0).getDual());
        System.out.println("mediaDescriptions.get(0).getDual() : " + mediaDescriptions.get(0).getDual());
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mediaDescriptions.get(0).getMediaType() :" + mediaDescriptions.get(0).getMediaType());
        System.out.println("mediaDescriptions.get(0).getMediaType() :" + mediaDescriptions.get(0).getMediaType());
        if (!mediaDescriptions.get(0).getDual()) {
            bOnlyDualStream = false;
            if (null == videoDualStreamMediaDesc
                    && mediaDescriptions.get(0).getMediaType().equals(MediaTypeEnum.VIDEO.getName())) {
                videoDualStreamMediaDesc = mediaDescriptions.get(0);
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "OnLocalMediaRequested, request terminal: " + videoDualStreamMediaDesc.getDirection() + ",: " + videoDualStreamMediaDesc.getEncodingFormat()
                        + ",: " + videoDualStreamMediaDesc.getMediaType() + ", :" + videoDualStreamMediaDesc.getDual() + ",:" + videoDualStreamMediaDesc.getStreamIndex());
                System.out.println("OnLocalMediaRequested, request terminal: " + videoDualStreamMediaDesc.getDirection() + ",: " + videoDualStreamMediaDesc.getEncodingFormat()
                        + ",: " + videoDualStreamMediaDesc.getMediaType() + ", :" + videoDualStreamMediaDesc.getDual() + ",:" + videoDualStreamMediaDesc.getStreamIndex());
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
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, onOpenLogicalChannel, exist reverseChannel info, start query exchange info, resourceInfoSize:" + resourceInfo.size());
                System.out.println("H323, onOpenLogicalChannel, exist reverseChannel info, start query exchange info, resourceInfoSize:" + resourceInfo.size());
                bCreate = false;

                List<ExchangeInfo> exchangeInfos = getExchange(resourceInfo);
                if (null == exchangeInfos) {
                    //如果查询不到资源节点，则清理相应的资源信息
                    for (DetailMediaResouce detailMediaResouce : reverseChannel) {
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

        if (bCreate) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, onOpenLogicalChannel, start add exchange info!! threadName:" + Thread.currentThread().getName());
            System.out.println("H323, onOpenLogicalChannel, start add exchange info!! threadName:" + Thread.currentThread().getName());

            for (MediaDescription mediaDescription : mediaDescriptions) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mediaDescription:" + mediaDescription.toString());
                System.out.println("mediaDescription:" + mediaDescription.toString());
                if (dualSource.size() == 0 || !mediaDescription.getDual()) {
                    CreateResourceParam createResourceParam = new CreateResourceParam();
                    createResourceParam.setSdp(constructSdp(mediaDescription));
                    CreateResourceResponse resourceResponse = addExchange(createResourceParam);
                    if (null == resourceResponse)
                        return false;

                    resourceInfo.add(resourceResponse.getResourceID());
                    addMediaResource(mediaDescription.getStreamIndex(), mediaDescription.getDual(), resourceResponse);
                } else if (mediaDescription.getDual()) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"Enter dual node update");
                    System.out.println("Enter dual node update");
                    List<String> dualResourceInfo = new ArrayList<>();
                    List<UpdateResourceParam> updateResourceParams = new ArrayList<>();
                    for (Map.Entry<String, MediaResource> entry : dualSource.entrySet()) {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"Added streaming media resource node update: dualSource.size() :" + dualSource.size());
                        System.out.println("Added streaming media resource node update: dualSource.size() :" + dualSource.size());
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"E164 = " + entry.getKey() + " dualResourceInfo.getId()= " + entry.getValue().toString());
                        System.out.println("E164 = " + entry.getKey() + " dualResourceInfo.getId()= " + entry.getValue().toString());
                        dualResourceInfo.add(entry.getValue().getId());
                        UpdateResourceParam updateResourceParam = new UpdateResourceParam(entry.getValue().getId());
                        updateResourceParam.setSdp(constructSdp(mediaDescriptions.get(0)));
                        updateResourceParams.add(updateResourceParam);
                    }
                    List<ExchangeInfo> exchangeInfos = getExchange(dualResourceInfo);
                    if (null == exchangeInfos) {
                        System.out.println("The streaming media resource node does not exist. Update failed");
                        updateResourceParams.clear();
                        dualResourceInfo.clear();
                        break;
                    } else {
                        boolean bOk = requestUpdateResource(updateResourceParams);
                        if (!bOk) {
                            removeMediaResource(false, dualResourceInfo);
                            return false;
                        }
                        updateMediaResource(true, exchangeInfos);
                        dualAddMediaResource();
                    }
                }
            }
        }

        boolean bOk = ackOpenLogicalChannel(mediaDescriptions);
        if (!bOk) {
            //反向通道打开失败,释放流媒体资源
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Reverse channel opening failed" + resourceInfo.toString());
            System.out.println("Reverse channel opening failed" + resourceInfo.toString());
            removeMediaResource(false, resourceInfo);
            resourceInfo.clear();
            return bOk;
        }

        resourceInfo.clear();

        if (bOnlyDualStream)
            return true;

        //在收到主流的反向的通道打开时，发起正向主流通道的打开
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, start open forward logical channel...........");
        System.out.println("H323, start open forward logical channel...........");
        return openLogicalChannel(mediaDescriptions);
    }

    public boolean openLogicalChannel(Vector<MediaDescription> mediaDescriptions) {
        Vector<MediaDescription> newMediaDescription = new Vector<>();

        boolean bCreate = true;
        DetailMediaResouce needUpdateDetailMediaResouce = null;
        List<String> resourceInfo = new ArrayList<>();
        if (null != forwardChannel) {
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
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, openLogicalChannel, exist forward channel info, query exchange info!!!");
                    System.out.println("H323, openLogicalChannel, exist forward channel info, query exchange info!!!");
                    needUpdateDetailMediaResouce = detailMediaResouce;
                    bCreate = false;
                    resourceInfo.add(detailMediaResouce.getId());
                    List<ExchangeInfo> exchangeInfos = getExchange(resourceInfo);
                    if (null == exchangeInfos) {
                        //清理资源
                        for (DetailMediaResouce cleanResource : forwardChannel) {
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
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, openLogicalChannel, start add exchange info!!! threadName:" + Thread.currentThread().getName());
            System.out.println("H323, openLogicalChannel, start add exchange info!!! threadName:" + Thread.currentThread().getName());

            CreateResourceParam createResourceParam = new CreateResourceParam();
            createResourceParam.setSdp(constructCreateSdp(mediaDescriptions.get(0)));
            resourceResponse = addExchange(createResourceParam);
            if (null == resourceResponse)
                return false;

            resourceInfo.add(resourceResponse.getResourceID());
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "resourceResponse" + resourceResponse.getSdp());
            System.out.println("resourceResponse : " + resourceResponse.getSdp());
            newMediaDescription.add(constructRequestMediaDescription(mediaDescriptions.get(0), resourceResponse.getSdp()));
        }
        boolean bOk = false;
        synchronized (this) {
            bOk = conferenceParticipant.RequestRemoteMedia(newMediaDescription);
            if (!bOk) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, openLogicalChannel, RequestRemoteMedia failed! participartId : " + e164);
                System.out.println("H323, openLogicalChannel, RequestRemoteMedia failed! participartId : " + e164);
                removeMediaResource(true, resourceInfo);
            } else {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, openLogicalChannel, RequestRemoteMedia Ok, participartId : " + e164);
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

    public void openDualStreamChannel(BaseRequestMsg startDualStreamRequest) {
        if (null != forwardChannel) {
            for (DetailMediaResouce detailMediaResouce : forwardChannel) {
                if (detailMediaResouce.getDual() == 1) {
                    LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50022 : exist dual stream!");
                    startDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.EXIST_DUALSTREAM.getCode(), HttpStatus.OK, ConfInterfaceResult.EXIST_DUALSTREAM.getMessage());
                    return;
                }
            }
        }
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "videoDualStreamMediaDesc : " + videoDualStreamMediaDesc.getDirection() + ",: " + videoDualStreamMediaDesc.getEncodingFormat()
                + ",: " + videoDualStreamMediaDesc.getMediaType() + ", :" + videoDualStreamMediaDesc.getDual() + ",:" + videoDualStreamMediaDesc.getStreamIndex());
        System.out.println("videoDualStreamMediaDesc : " + videoDualStreamMediaDesc.getDirection() + ",: " + videoDualStreamMediaDesc.getEncodingFormat()
                + ",: " + videoDualStreamMediaDesc.getMediaType() + ", :" + videoDualStreamMediaDesc.getDual() + ",:" + videoDualStreamMediaDesc.getStreamIndex());
        videoDualStreamMediaDesc.setStreamIndex(-1);
        videoDualStreamMediaDesc.setDual(true);
        CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setSdp(constructCreateSdp(videoDualStreamMediaDesc));
        CreateResourceResponse resourceResponse = addExchange(createResourceParam);
        if (null == resourceResponse) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "openDualStreamChannel, addExchange failed!");
            System.out.println("openDualStreamChannel, addExchange failed!");
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50020 : add exchange node failed!");
            startDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.ADD_EXCHANGENODE_FAILED.getCode(), HttpStatus.OK, ConfInterfaceResult.ADD_EXCHANGENODE_FAILED.getMessage());
            return;
        }

        Vector<MediaDescription> newMediaDescription = new Vector<>();
        MediaDescription requestRemoteMedia = constructRequestMediaDescription(videoDualStreamMediaDesc, resourceResponse.getSdp());
        newMediaDescription.add(requestRemoteMedia);

        boolean bOk = conferenceParticipant.RequestRemoteMedia(newMediaDescription);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "bOk ; " + bOk);
        System.out.println("bOk ; " + bOk);
        if (!bOk) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, openDualStreamChannel, RequestRemoteMedia failed! participartId : " + e164);
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

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "openDualStreamChannel, RequestRemoteMedia Ok! streamIndex:" + dualStreamIndex);
        System.out.println("openDualStreamChannel, RequestRemoteMedia Ok! streamIndex:" + dualStreamIndex);

        newMediaDescription.clear();

    }

    public boolean resumeDualStream() {
        videoDualStreamMediaDesc.setStreamIndex(-1);
        videoDualStreamMediaDesc.setDual(true);

        List<String> resourceInfo = new ArrayList<>();
        resourceInfo.add(dualStreamResourceId);
        List<ExchangeInfo> exchangeInfos = getExchange(resourceInfo);
        if (null == exchangeInfos) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "resumeDualStream, getExchange failed! resourceId:" + dualStreamResourceId);
            System.out.println("resumeDualStream, getExchange failed! resourceId:" + dualStreamResourceId);
            return false;
        }

        updateMediaResource(false, exchangeInfos);

        Vector<MediaDescription> requestMediaDescription = new Vector<>();
        requestMediaDescription.add(constructRequestMediaDescription(videoDualStreamMediaDesc, exchangeInfos.get(0).getLocalSdp()));

        boolean bOk = conferenceParticipant.RequestRemoteMedia(requestMediaDescription);
        if (!bOk) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, openDualStreamChannel, RequestRemoteMedia failed! participartId : " + e164);
            System.out.println("H323, openDualStreamChannel, RequestRemoteMedia failed! participartId : " + e164);
            //释放交换资源
            removeMediaResource(true, resourceInfo);
            requestMediaDescription.clear();

            return false;
        }

        int dualStreamIndex = requestMediaDescription.get(0).getStreamIndex();
        videoDualStreamMediaDesc.setStreamIndex(dualStreamIndex);

        for (DetailMediaResouce detailMediaResouce : forwardChannel) {
            if (!detailMediaResouce.getId().equals(dualStreamResourceId))
                continue;

            detailMediaResouce.setStreamIndex(dualStreamIndex);
            break;
        }

        requestMediaDescription.clear();
        return true;
    }

    public boolean closeDualStreamChannel() {
        if (null == videoDualStreamMediaDesc || null == forwardChannel || null == dualStreamResourceId)
            return true;

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "closeDualStreamChannel, streamIndex : " + videoDualStreamMediaDesc.getStreamIndex());
        System.out.println("closeDualStreamChannel, streamIndex : " + videoDualStreamMediaDesc.getStreamIndex());
        boolean bOk = conferenceParticipant.RequestCleanupMediaById(videoDualStreamMediaDesc.getStreamIndex());
        if (!bOk) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "closeDualStreamChannel, RequestCleanupMediaById failed!");
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

        if (detailMediaResouce.getDual() != 1) {
            forwardGenericStreamNum.incrementAndGet();
        } else if (detailMediaResouce.getStreamIndex() == -1) {
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
    private int localRasPort;
    private int localCallPort;
    protected static MediaDescription videoDualStreamMediaDesc;
    protected String dualStreamResourceId;
    protected AtomicBoolean resumeDualStream;
    protected AtomicInteger forwardGenericStreamNum;
}
