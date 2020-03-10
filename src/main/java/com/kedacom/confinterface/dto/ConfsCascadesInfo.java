package com.kedacom.confinterface.dto;

import java.util.List;

public class ConfsCascadesInfo {
    public ConfsCascadesInfo(String name, String confId, String cascadeId) {
        this.name = name;
        this.confId = confId;
        this.cascadeId = cascadeId;
    }

    public ConfsCascadesInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfId() {
        return confId;
    }

    public void setConfId(String confId) {
        this.confId = confId;
    }

    public String getCascadeId() {
        return cascadeId;
    }

    public void setCascadeId(String cascadeId) {
        this.cascadeId = cascadeId;
    }

    public List<ConfsCascadesInfo> getCascades() {
        return cascades;
    }

    public void setCascades(List<ConfsCascadesInfo> cascades) {
        this.cascades = cascades;
    }

    public String getConf_id() {
        return conf_id;
    }

    public void setConf_id(String conf_id) {
        this.conf_id = conf_id;
    }

    public String getCascade_id() {
        return cascade_id;
    }

    public void setCascade_id(String cascade_id) {
        this.cascade_id = cascade_id;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("name:").append(name)
                .append(", confId:").append(confId)
                .append(", cascadeId:").append(cascadeId)
                .append(", conf_id:").append(conf_id)
                .append(", cascade_id:").append(cascade_id)
                .toString();
    }

    private String name;
    private String confId;
    private String cascadeId;
    private String conf_id;
    private String cascade_id;
    private List<ConfsCascadesInfo> cascades;
}
