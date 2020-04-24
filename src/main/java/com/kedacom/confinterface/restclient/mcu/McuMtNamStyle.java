package com.kedacom.confinterface.restclient.mcu;

public class McuMtNamStyle {
    public McuMtNamStyle(int font_size, String font_color, int position) {
        this.font_size = font_size;
        this.font_color = font_color;
        this.position = position;
    }

    public McuMtNamStyle() {
    }

    public int getFont_size() {
        return font_size;
    }

    public void setFont_size(int font_size) {
        this.font_size = font_size;
    }

    public String getFont_color() {
        return font_color;
    }

    public void setFont_color(String font_color) {
        this.font_color = font_color;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("font_size:").append(font_size)
                .append(", font_color:").append(font_color)
                .append(", position:").append(position)
                .toString();
    }

    private int font_size = 0; //台标字体大小, 默认为: 0 0-小；1-中；2-大；
    private String font_color = "#FFFFFF"; //台标字体三原色#RGB格式, 十六进制表示, 默认为: #FFFFFF白色
    private int position = 1; //台标显示位置, 默认为: 10-左上角；1-左下角；2-右上角；3-右下角；4-底部中间；
}
