package com.kedacom.confinterface.restclient.mcu;

import java.util.ArrayList;
import java.util.List;

/*
"name": "name",
  "duration": 240,
  "bitrate": 8128,
  "closed_conf": 1,
  "safe_conf": 0,
  "password": "22222",
  "encrypted_type": 2,
  "conf_type": 0,
  "call_mode": 0,
  "call_times": 0,
  "call_interval": 20,
  "mute": 1,
  "silence": 1,
  "video_quality": 1,
  "encrypted_key": "234234234",
  "dual_mode": 0,
  "voice_activity_detection": 1,
  "vacinterval": 5,
  "cascade_mode": 1,
  "cascade_upload": 1,
  "cascade_return": 0,
  "cascade_return_para": 0,
  "max_join_mt": 192,
  "auto_end": 0,
  "preoccpuy_resource": 1,
  "one_reforming": 0,
 */
public class CreateConferenceParam {
    public CreateConferenceParam() {
        super();
        this.name = null;
        this.duration = 0;  //会议时长，0为永久会议
        this.bitrate = 8128;
        this.closed_conf = 1;
        this.safe_conf = 0;
        this.password = null;
        this.encrypted_type = 2;
        this.conf_type = 0;
        this.call_mode = 2; // 自动呼叫模式， 0：手动呼叫模式
        this.call_interval = 20; //单位秒
        this.call_times = 30;
        this.mute = 0;
        this.silence = 0;
        this.video_quality = 1;  //0:质量优先，1：速度优先
        this.dual_mode = 1;  //双流权限，0：发言会场，1：任意会场，2：指定会场
        this.voice_activity_detection = 0; //是否开启语音激励, 0：否，1:是
        this.vacinterval = 5;  //语音激励敏感度(s),5，15，30，60
        this.cascade_mode = 1; //级联模式, 0:简单级联, 1:合并级联
        this.cascade_upload = 1; //是否级联上传, 0:否，1：是
        this.cascade_return = 0; //是否级联回传，0：否， 1：是
        this.cascade_return_para = 0; //级联回传带宽参数
        this.max_join_mt = 192; //最大与会终端数 8:小型8方会议, 192:大型192方会议
        this.auto_end = 0; //会议中无终端时，是否自动结会
        this.preoccpuy_resource = 0; //预占资源
        this.one_reforming = 0; //归一重整，开启该功能后可增强对外厂商终端的兼容，但会使丢包重传失效, 0:不使用，1：使用
        this.video_formats = null;
        this.audio_formats = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCall_interval() {
        return call_interval;
    }

    public int getCall_mode() {
        return call_mode;
    }

    public int getCall_times() {
        return call_times;
    }

    public int getCascade_mode() {
        return cascade_mode;
    }

    public int getCascade_return() {
        return cascade_return;
    }

    public int getCascade_upload() {
        return cascade_upload;
    }

    public int getClosed_conf() {
        return closed_conf;
    }

    public int getConf_type() {
        return conf_type;
    }

    public int getDual_mode() {
        return dual_mode;
    }

    public int getEncrypted_type() {
        return encrypted_type;
    }

    public int getCascade_return_para() {
        return cascade_return_para;
    }

    public int getMute() {
        return mute;
    }

    public int getSafe_conf() {
        return safe_conf;
    }

    public int getSilence() {
        return silence;
    }

    public int getVideo_quality() {
        return video_quality;
    }

    public int getVacinterval() {
        return vacinterval;
    }

    public int getPublic_conf() {
        return public_conf;
    }

    public int getVoice_activity_detection() {
        return voice_activity_detection;
    }

    public String getEncrypted_key() {
        return encrypted_key;
    }

    public void setCall_interval(int call_interval) {
        this.call_interval = call_interval;
    }

    public void setCall_mode(int call_mode) {
        this.call_mode = call_mode;
    }

    public void setCall_times(int call_times) {
        this.call_times = call_times;
    }

    public void setCascade_mode(int cascade_mode) {
        this.cascade_mode = cascade_mode;
    }

    public void setCascade_upload(int cascade_upload) {
        this.cascade_upload = cascade_upload;
    }

    public void setClosed_conf(int closed_conf) {
        this.closed_conf = closed_conf;
    }

    public void setConf_type(int conf_type) {
        this.conf_type = conf_type;
    }

    public void setDual_mode(int dual_mode) {
        this.dual_mode = dual_mode;
    }

    public void setCascade_return(int cascade_return) {
        this.cascade_return = cascade_return;
    }

    public void setEncrypted_key(String encrypted_key) {
        this.encrypted_key = encrypted_key;
    }

    public void setCascade_return_para(int cascade_return_para) {
        this.cascade_return_para = cascade_return_para;
    }

    public void setEncrypted_type(int encrypted_type) {
        this.encrypted_type = encrypted_type;
    }

    public void setMute(int mute) {
        this.mute = mute;
    }

    public void setSafe_conf(int safe_conf) {
        this.safe_conf = safe_conf;
    }

    public void setSilence(int silence) {
        this.silence = silence;
    }

    public void setVideo_quality(int video_quality) {
        this.video_quality = video_quality;
    }

    public void setVacinterval(int vacinterval) {
        this.vacinterval = vacinterval;
    }

    public int getMax_join_mt() {
        return max_join_mt;
    }

    public void setPublic_conf(int public_conf) {
        this.public_conf = public_conf;
    }

    public void setVoice_activity_detection(int voice_activity_detection) {
        this.voice_activity_detection = voice_activity_detection;
    }

    public void setMax_join_mt(int max_join_mt) {
        this.max_join_mt = max_join_mt;
    }

    public int getAuto_end() {
        return auto_end;
    }

    public int getOne_reforming() {
        return one_reforming;
    }

    public int getPreoccpuy_resource() {
        return preoccpuy_resource;
    }

    public List<VideoFormat> getVideo_formats() {
        return video_formats;
    }

    public void setAuto_end(int auto_end) {
        this.auto_end = auto_end;
    }

    public void setOne_reforming(int one_reforming) {
        this.one_reforming = one_reforming;
    }

    public void setPreoccpuy_resource(int preoccpuy_resource) {
        this.preoccpuy_resource = preoccpuy_resource;
    }

    public void setVideo_formats(List<VideoFormat> video_formats) {
        this.video_formats = video_formats;
    }

    public void setVideo_formats(String videoFormats) {
        String[] videoformats = videoFormats.split(",");

        if (null == video_formats) {
            synchronized (this) {
                video_formats = new ArrayList<>();
            }
        }

        for (String videoformat : videoformats) {
            String[] result = videoformat.split("/");
            VideoFormat videoFormat = new VideoFormat();
            videoFormat.setFormat(Integer.valueOf(result[0]));
            videoFormat.setResolution(Integer.valueOf(result[1]));
            videoFormat.setFrame(Integer.valueOf(result[2]));
            videoFormat.setBitrate(Integer.valueOf(result[3]));

            video_formats.add(videoFormat);
        }
    }

    public void setAudio_formats(String audioFormats){
        if (null == audioFormats)
            return;

        String[] audioformats = audioFormats.split(",");

        if (null == audio_formats) {
            synchronized (this) {
                audio_formats = new ArrayList<>();
            }
        }

        for (String strAudioformat : audioformats) {
            int format = Integer.valueOf(strAudioformat);
            if (AudioFormatEnum.resolve(format) == AudioFormatEnum.UNKNOWN){
                System.out.println("invalid audio format! format:" + strAudioformat);
                continue;
            }

            audio_formats.add(format);
        }
    }

    public List<Integer> getAudio_formats() {
        return audio_formats;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("name:").append(name)
                .append(",duration:").append(duration)
                .append(",bitrate:").append(bitrate)
                .append(",closed_conf:").append(closed_conf)
                .append(",safe_conf:").append(safe_conf)
                .append(",password:").append(password)
                .append(",encrypted_type:").append(encrypted_type)
                .append(",conf_type:").append(conf_type)
                .append(",call_mode:").append(call_mode)
                .append(",call_times:").append(call_times)
                .append(",call_interval:").append(call_interval)
                .append(",mute:").append(mute)
                .append(",silence:").append(silence)
                .append(",video_quality:").append(video_quality)
                .append(",encrypted_key:").append(encrypted_key)
                .append(",dual_mode:").append(dual_mode)
                .append(",voice_activity_detection:").append(voice_activity_detection)
                .append(",vacinterval:").append(vacinterval)
                .append(",cascade_mode:").append(cascade_mode)
                .append(",cascade_upload:").append(cascade_upload)
                .append(",cascade_return:").append(cascade_return)
                .append(",cascade_return_para:").append(cascade_return_para)
                .append(",public_conf:").append(public_conf)
                .append(",max_join_mt:").append(max_join_mt)
                .append(",auto_end:").append(auto_end)
                .append(",preoccpuy_resource:").append(preoccpuy_resource)
                .append(",one_reforming:").append(one_reforming)
                .append(", video_formats:").append(video_formats)
                .append(", audio_formats:").append(audio_formats)
                .toString();
    }

    private String name;
    private int duration;
    private int bitrate;
    private int closed_conf;
    private int safe_conf;
    private String password;
    private int encrypted_type;
    private int conf_type;
    private int call_mode;
    private int call_times;
    private int call_interval;
    private int mute;
    private int silence;
    private int video_quality;
    private String encrypted_key;
    private int dual_mode;
    private int voice_activity_detection;
    private int vacinterval;
    private int cascade_mode;
    private int cascade_upload;
    private int cascade_return;
    private int cascade_return_para;
    private int public_conf;
    private int max_join_mt;
    private int auto_end;
    private int preoccpuy_resource;
    private int one_reforming;
    private List<VideoFormat> video_formats;
    private List<Integer> audio_formats;
}
