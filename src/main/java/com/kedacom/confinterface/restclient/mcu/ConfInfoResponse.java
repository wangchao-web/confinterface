package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class ConfInfoResponse extends McuBaseResponse{
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeeting_room_name() {
        return meeting_room_name;
    }

    public void setMeeting_room_name(String meeting_room_name) {
        this.meeting_room_name = meeting_room_name;
    }

    public String getConf_id() {
        return conf_id;
    }

    public void setConf_id(String conf_id) {
        this.conf_id = conf_id;
    }

    public int getConf_level() {
        return conf_level;
    }

    public void setConf_level(int conf_level) {
        this.conf_level = conf_level;
    }

    public int getConf_type() {
        return conf_type;
    }

    public void setConf_type(int conf_type) {
        this.conf_type = conf_type;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
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

    public int getClosed_conf() {
        return closed_conf;
    }

    public void setClosed_conf(int closed_conf) {
        this.closed_conf = closed_conf;
    }

    public int getSafe_conf() {
        return safe_conf;
    }

    public void setSafe_conf(int safe_conf) {
        this.safe_conf = safe_conf;
    }

    public int getEncrypted_type() {
        return encrypted_type;
    }

    public void setEncrypted_type(int encrypted_type) {
        this.encrypted_type = encrypted_type;
    }

    public int getMute() {
        return mute;
    }

    public void setMute(int mute) {
        this.mute = mute;
    }

    public int getMute_filter() {
        return mute_filter;
    }

    public void setMute_filter(int mute_filter) {
        this.mute_filter = mute_filter;
    }

    public int getSilence() {
        return silence;
    }

    public void setSilence(int silence) {
        this.silence = silence;
    }

    public int getVideo_quality() {
        return video_quality;
    }

    public void setVideo_quality(int video_quality) {
        this.video_quality = video_quality;
    }

    public String getEncrypted_key() {
        return encrypted_key;
    }

    public void setEncrypted_key(String encrypted_key) {
        this.encrypted_key = encrypted_key;
    }

    public int getDual_mode() {
        return dual_mode;
    }

    public void setDual_mode(int dual_mode) {
        this.dual_mode = dual_mode;
    }

    public int getPublic_conf() {
        return public_conf;
    }

    public void setPublic_conf(int public_conf) {
        this.public_conf = public_conf;
    }

    public int getAuto_end() {
        return auto_end;
    }

    public void setAuto_end(int auto_end) {
        this.auto_end = auto_end;
    }

    public int getPreoccupy_resource() {
        return preoccupy_resource;
    }

    public void setPreoccupy_resource(int preoccupy_resource) {
        this.preoccupy_resource = preoccupy_resource;
    }

    public int getMax_join_mt() {
        return max_join_mt;
    }

    public void setMax_join_mt(int max_join_mt) {
        this.max_join_mt = max_join_mt;
    }

    public int getForce_broadcast() {
        return force_broadcast;
    }

    public void setForce_broadcast(int force_broadcast) {
        this.force_broadcast = force_broadcast;
    }

    public int getFec_mode() {
        return fec_mode;
    }

    public void setFec_mode(int fec_mode) {
        this.fec_mode = fec_mode;
    }

    public int getVoice_activity_detection() {
        return voice_activity_detection;
    }

    public void setVoice_activity_detection(int voice_activity_detection) {
        this.voice_activity_detection = voice_activity_detection;
    }

    public int getVacinterval() {
        return vacinterval;
    }

    public void setVacinterval(int vacinterval) {
        this.vacinterval = vacinterval;
    }

    public int getCall_times() {
        return call_times;
    }

    public void setCall_times(int call_times) {
        this.call_times = call_times;
    }

    public int getCall_interval() {
        return call_interval;
    }

    public void setCall_interval(int call_interval) {
        this.call_interval = call_interval;
    }

    public int getCall_mode() {
        return call_mode;
    }

    public void setCall_mode(int call_mode) {
        this.call_mode = call_mode;
    }

    public int getCascade_mode() {
        return cascade_mode;
    }

    public void setCascade_mode(int cascade_mode) {
        this.cascade_mode = cascade_mode;
    }

    public int getCascade_upload() {
        return cascade_upload;
    }

    public void setCascade_upload(int cascade_upload) {
        this.cascade_upload = cascade_upload;
    }

    public int getCascade_return() {
        return cascade_return;
    }

    public void setCascade_return(int cascade_return) {
        this.cascade_return = cascade_return;
    }

    public int getCascade_return_para() {
        return cascade_return_para;
    }

    public void setCascade_return_para(int cascade_return_para) {
        this.cascade_return_para = cascade_return_para;
    }

    public int getVmp_enable() {
        return vmp_enable;
    }

    public void setVmp_enable(int vmp_enable) {
        this.vmp_enable = vmp_enable;
    }

    public int getMix_enable() {
        return mix_enable;
    }

    public void setMix_enable(int mix_enable) {
        this.mix_enable = mix_enable;
    }

    public int getPoll_enable() {
        return poll_enable;
    }

    public void setPoll_enable(int poll_enable) {
        this.poll_enable = poll_enable;
    }

    public int getNeed_password() {
        return need_password;
    }

    public void setNeed_password(int need_password) {
        this.need_password = need_password;
    }

    public int getOne_reforming() {
        return one_reforming;
    }

    public void setOne_reforming(int one_reforming) {
        this.one_reforming = one_reforming;
    }

    public int getDoubleflow() {
        return doubleflow;
    }

    public void setDoubleflow(int doubleflow) {
        this.doubleflow = doubleflow;
    }

    public String getPlatform_id() {
        return platform_id;
    }

    public void setPlatform_id(String platform_id) {
        this.platform_id = platform_id;
    }

    public List<VideoFormat> getVideo_formats() {
        return video_formats;
    }

    public void setVideo_formats(List<VideoFormat> video_formats) {
        this.video_formats = video_formats;
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("name:").append(name)
                .append(", meeting_room_name:").append(meeting_room_name)
                .append(", conf_id:").append(conf_id)
                .append(", conf_level:").append(conf_level)
                .append(", conf_type:").append(conf_type)
                .append(", start_time:").append(start_time)
                .append(", end_time:").append(end_time)
                .append(", duration:").append(duration)
                .append(", bitrate:").append(bitrate)
                .append(", closed_conf:").append(closed_conf)
                .append(", safe_conf:").append(safe_conf)
                .append(", encrypted_type:").append(encrypted_type)
                .append(", mute:").append(mute)
                .append(", mute_filter:").append(mute_filter)
                .append(", silence:").append(silence)
                .append(", video_quality:").append(video_quality)
                .append(", encrypted_key:").append(encrypted_key)
                .append(", dual_mode:").append(dual_mode)
                .append(", public_conf:").append(public_conf)
                .append(", auto_end:").append(auto_end)
                .append(", preoccupy_resource:").append(preoccupy_resource)
                .append(", max_join_mt:").append(max_join_mt)
                .append(", force_broadcast:").append(force_broadcast)
                .append(", fec_mode:").append(fec_mode)
                .append(", voice_activity_detection").append(voice_activity_detection)
                .append(", vacinterval:").append(vacinterval)
                .append(", call_times:").append(call_times)
                .append(", call_interval:").append(call_interval)
                .append(", call_mode:").append(call_mode)
                .append(", cascade_mode:").append(cascade_mode)
                .append(", cascade_upload:").append(cascade_upload)
                .append(", cascade_return:").append(cascade_return)
                .append(", cascade_return_para:").append(cascade_return_para)
                .append(", vmp_enable:").append(vmp_enable)
                .append(", mix_enable:").append(mix_enable)
                .append(", poll_enable:").append(poll_enable)
                .append(", need_password:").append(need_password)
                .append(", one_reforming:").append(one_reforming)
                .append(", doubleflow:").append(doubleflow)
                .append(", platform_id:").append(platform_id)
                .append(", video_formats:").append(video_formats.toString())
                .append(", creator:").append(creator.toString())
                .toString();
    }

    private String name;
    private String meeting_room_name;
    private String conf_id;
    private int conf_level;
    private int conf_type;
    private String start_time;
    private String end_time;
    private int duration;
    private int bitrate;
    private int closed_conf;
    private int safe_conf;
    private int encrypted_type;
    private int mute;
    private int mute_filter;
    private int silence;
    private int video_quality;
    private String encrypted_key;
    private int dual_mode;
    private int public_conf;
    private int auto_end;
    private int  preoccupy_resource;
    private int max_join_mt;
    private int force_broadcast;
    private int fec_mode;
    private int voice_activity_detection;
    private int vacinterval;
    private int call_times;
    private int call_interval;
    private int call_mode;
    private int cascade_mode;
    private int cascade_upload;
    private int cascade_return;
    private int cascade_return_para;
    private int vmp_enable;
    private int mix_enable;
    private int poll_enable;
    private int need_password;
    private int one_reforming;
    private int doubleflow;
    private String platform_id;
    private List<VideoFormat> video_formats;
    private Creator creator ;
}
