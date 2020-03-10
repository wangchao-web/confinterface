package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class CascadesInfo {
    public CascadesInfo(String name, String conf_id, String cascade_id, String mt_id, List<CascadesInfo> cascades) {
        this.name = name;
        this.conf_id = conf_id;
        this.cascade_id = cascade_id;
        this.mt_id = mt_id;
        cascades = cascades;
    }

    public CascadesInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getMt_id() {
        return mt_id;
    }

    public void setMt_id(String mt_id) {
        this.mt_id = mt_id;
    }

    public List<CascadesInfo> getCascades() {
        return cascades;
    }

    public void setCascades(List<CascadesInfo> cascades) {
        this.cascades = cascades;
    }

    private String name;
    private String conf_id;
    private String cascade_id;
    private String mt_id;
    private List<CascadesInfo> cascades;
}
