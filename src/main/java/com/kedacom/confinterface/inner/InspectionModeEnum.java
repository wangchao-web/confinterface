package com.kedacom.confinterface.inner;

public enum InspectionModeEnum {

    ALL("all",0), VIDEO("video", 1), AUDIO("audio", 2);

    public String getName(){
        return this.name;
    }

    public int getCode(){
        return this.code;
    }

    InspectionModeEnum(String name, int code){
        this.name = name;
        this.code = code;
    }

    public static InspectionModeEnum resolve(String mode) {
        InspectionModeEnum[] inspectionModes = values();
        int modeNum = inspectionModes.length;

        for(int index = 0; index < modeNum; ++index) {
            InspectionModeEnum inspectionModeEnum = inspectionModes[index];
            if ( inspectionModeEnum.getName().equals(mode)) {
                return inspectionModeEnum;
            }
        }

        return null;
    }
    private String name;
    private int code;
}
