package com.kedacom.confinterface.service;

import com.kedacom.confinterface.dto.MediaResource;

import java.util.List;

public abstract class ConfInterfacePublishService {
    public abstract void addSubscribeMessage(int type, String groupId, String url);
    public abstract void publishStatus(String account, String groupId, int status, List<MediaResource> forwardResources, List<MediaResource> reverseResources);
    public abstract void publishStatus(String account, String groupId, int status);
    public abstract void publishStatus(String account, String groupId, int status , int faileCode);
}
