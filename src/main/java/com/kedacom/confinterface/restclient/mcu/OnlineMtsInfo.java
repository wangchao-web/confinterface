package com.kedacom.confinterface.restclient.mcu;

import java.util.ArrayList;
import java.util.List;

public class OnlineMtsInfo {
    public List<OnlineMt> getMts() {
        return mts;
    }

    public void setMts(List<OnlineMt> mts) {
        this.mts = mts;
    }

    public void addOnlineMt(OnlineMt onlineMt){
        if (null == mts){
            mts = new ArrayList<>();
        }

        mts.add(onlineMt);
    }

    private List<OnlineMt> mts;
}
