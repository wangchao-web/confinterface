package com.kedacom.confinterface.exchange;

import java.util.List;

public class QueryAndDelResourceParam {
    public List<String> getResourceIDs() {
        return resourceIDs;
    }

    public void setResourceIDs(List<String> resourceIDs) {
        this.resourceIDs = resourceIDs;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("resourceIDs:").append(resourceIDs).toString();
    }

    private List<String> resourceIDs;
}
