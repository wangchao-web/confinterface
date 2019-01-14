package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.inner.DetailMediaResouce;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TerminalMediaResource implements Serializable {
    public TerminalMediaResource(){
        super();
        this.mtE164 = null;
        this.reverseResources = null;
        this.forwardResources = null;
    }

    public void setMtE164(String mtE164) {
        this.mtE164 = mtE164;
    }

    public String getMtE164() {
        return mtE164;
    }

    public List<MediaResource> getForwardResources() {
        return forwardResources;
    }

    public void setForwardResources(List<MediaResource> mediaResources){
        this.forwardResources = mediaResources;
    }

    public List<MediaResource> getReverseResources() {
        return reverseResources;
    }

    public void setReverseResources(List<MediaResource> mediaResources){
        this.reverseResources = mediaResources;
    }

    public static List<MediaResource> convertToMediaResource(List<DetailMediaResouce> detailMediaResouces, String type) {
        if (null == detailMediaResouces)
            return null;

        List<MediaResource> mediaResources = Collections.synchronizedList(new ArrayList<>());
        for (DetailMediaResouce detailMediaResouce : detailMediaResouces) {
            if (type.equals("video") && detailMediaResouce.getType().equals("audio")
                    || type.equals("audio") && detailMediaResouce.getType().equals("video"))
                continue;

            MediaResource mediaResource = new MediaResource();
            mediaResource.setId(detailMediaResouce.getId());
            mediaResource.setType(detailMediaResouce.getType());
            mediaResources.add(mediaResource);
        }

        return mediaResources;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("mtE164:").append(mtE164)
                .append(", forwardResources{").append(forwardResources).append("}")
                .append(", reverseResources{").append(reverseResources).append("}")
                .toString();
    }

    private String mtE164;
    private List<MediaResource> forwardResources;
    private List<MediaResource> reverseResources;
}
