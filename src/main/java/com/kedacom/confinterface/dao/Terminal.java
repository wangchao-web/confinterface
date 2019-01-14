package com.kedacom.confinterface.dao;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class Terminal implements Serializable {

    public Terminal(){
        super();
        this.mtE164 = null;
    }

    public Terminal(String mtE164) {
        super();
        this.mtE164 = mtE164;
    }

    public String getMtE164() {
        return mtE164;
    }

    public void setMtE164(String mtE164) {
        this.mtE164 = mtE164;
    }

    @Override
    public String toString() {
        return mtE164;
    }

    @NotBlank
    protected String mtE164;
}
