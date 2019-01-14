package com.kedacom.confinterface.inner;

/*参照T-REC-H.241-201202-I!!PDF-E.pdf文件中的Table 8-4-Level parameter values的定义*/
public enum  H264LevelParamEnum {
    LEVEL1(15,10),
    LEVEL1B(19, 9),
    LEVEL11(22, 11),
    LEVEL12(29, 12),
    LEVEL13(36, 13),
    LEVEL2(43, 20),
    LEVEL21(50, 21),
    LEVEL22(57, 22),
    LEVEL3(64, 30),
    LEVEL31(71, 31),
    LEVEL32(78, 32),
    LEVEL4(85, 40),
    LEVEL41(92, 41),
    LEVEL42(99, 42),
    LEVEL5(106, 50),
    LEVEL51(113, 51),
    LEVEL52(120, 52);

    public int getLevelParamValue(){
        return levelParamValue;
    }

    public int getH264LevelNum(){
        return h264LevelNum;
    }

    H264LevelParamEnum(int levelParamValue, int h264LevelNum){
        this.levelParamValue = levelParamValue;
        this.h264LevelNum = h264LevelNum;
    }

    private int levelParamValue;
    private int h264LevelNum;
}
