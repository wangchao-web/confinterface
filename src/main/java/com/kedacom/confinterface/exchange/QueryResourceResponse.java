package com.kedacom.confinterface.exchange;

import com.kedacom.confinterface.dto.BaseResponseMsg;

import java.util.List;

public class QueryResourceResponse extends BaseResponseMsg {

    public List<ExchangeInfo> getExchangNodeInfos() {
        return exchangNodeInfos;
    }

    public void setExchangNodeInfos(List<ExchangeInfo> exchangNodeInfos) {
        this.exchangNodeInfos = exchangNodeInfos;
    }

    private List<ExchangeInfo> exchangNodeInfos;
}
