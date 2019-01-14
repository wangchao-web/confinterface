package com.kedacom.confinterface.exchange;

public class UpdateResourceParam extends CreateResourceParam{

    public UpdateResourceParam(String resourceID){
        super();
        this.resourceID = resourceID;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("resourceID:").append(resourceID).toString();
    }

    private String resourceID;
}
