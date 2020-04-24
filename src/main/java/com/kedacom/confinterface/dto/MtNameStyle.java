package com.kedacom.confinterface.dto;

import org.hibernate.validator.constraints.Range;

public class MtNameStyle {
    public MtNameStyle(@Range(min = 0, max = 2) int fontSize, String fontColor, @Range(min = 0, max = 4) int position) {
        this.fontSize = fontSize;
        this.fontColor = fontColor;
        this.position = position;
    }

    public MtNameStyle() {
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("fontSize:").append(fontSize)
                .append(", fontColor:").append(fontColor)
                .append(", position:").append(position)
                .toString();
    }
    @Range(min = 0, max = 2)
    private int fontSize = 0; //台标字体大小, 默认为: 0, 0-小；1-中；2-大；

    private String fontColor = "#FFFFFF白色"; //台标字体三原色#RGB格式, 十六进制表示, 默认为: #FFFFFF白色

    @Range(min = 0, max = 4)
    private int position = 1; //台标显示位置, 默认为: 1, 0-左上角；1-左下角；2右上角；3-右下角；4-底部中间；
}
