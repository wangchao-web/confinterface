package com.kedacom.confinterface.restclient.mcu;

import java.util.ArrayList;
import java.util.List;

public class McuMonitorsHeartbeatParam {
    public McuMonitorsHeartbeatParam(List<McuMonitorsDst> monitors) {
        this.monitors = monitors;
    }

    public McuMonitorsHeartbeatParam() {
    }

    public List<McuMonitorsDst> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<McuMonitorsDst> monitors) {
        this.monitors = monitors;
    }

    public void addMonitor(McuMonitorsDst monitor) {
        if (null == monitors){
            monitors = new ArrayList<>();
        }

        monitors.add(monitor);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("monitors:").append(monitors.toString())
                .toString();
    }

    private List<McuMonitorsDst> monitors;
}
