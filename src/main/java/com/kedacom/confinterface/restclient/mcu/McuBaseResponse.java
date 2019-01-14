package com.kedacom.confinterface.restclient.mcu;

public class McuBaseResponse {

    public McuBaseResponse() {
        super();
        this.success = 1;
        this.error_code = 0;
        this.description = null;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean success() {
        if (success == 1)
            return true;

        return false;
    }

    protected int success;
    protected int error_code;
    protected String description;
}
