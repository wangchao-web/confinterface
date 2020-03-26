package com.kedacom.confinterface.restclient.mcu;

import java.util.ArrayList;
import java.util.List;

public class ConfInspectionsResponse extends McuBaseResponse {

    public ConfInspectionsResponse() {
        super();
        this.inspections = null;
    }

    public ArrayList<McuInspectionParam> getInspections() {
        return inspections;
    }

    public void setInspections(ArrayList<McuInspectionParam> inspections) {
        this.inspections = inspections;
    }
    @Override
    public String toString() {
        return new StringBuilder().append("inspections:").append(inspections).toString();
    }
    private ArrayList<McuInspectionParam> inspections;
}
