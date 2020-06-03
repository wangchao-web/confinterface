package com.kedacom.confinterface.service;

import com.kedacom.confinterface.dto.MediaResource;
import com.kedacom.confinterface.inner.SubscribeMsgTypeEnum;

import java.util.List;

public abstract class ConfInterfacePublishService {
    public abstract void addSubscribeMessage(int type, String groupId, String url);
    public abstract void publishStatus(String account, String groupId, int status, List<MediaResource> forwardResources, List<MediaResource> reverseResources);
    public abstract void publishStatus(String account, String groupId, int status);
    public abstract void publishStatus(String account, String groupId, int status , int faileCode);

    //用于会议服务断链再重启之后推送状态
    public abstract void publishStatus(SubscribeMsgTypeEnum type, String publishUrl, Object publishMsg);
    //用于删除订阅路径
    public abstract void cancelSubscribeMessage(int type, String groupId, String url);
}
