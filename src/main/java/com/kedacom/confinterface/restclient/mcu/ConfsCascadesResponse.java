package com.kedacom.confinterface.restclient.mcu;


import com.kedacom.confinterface.dto.ConfsCascadesInfo;

import java.util.List;

public class ConfsCascadesResponse extends McuBaseResponse {

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

    public List<ConfsCascadesInfo> getCascades() {
        return cascades;
    }

    public void setCascades(List<ConfsCascadesInfo> cascades) {
        this.cascades = cascades;
    }

    private String name;
    private String conf_id;
    private String cascade_id;
    private String mt_id;
    private List<ConfsCascadesInfo> cascades;
}
