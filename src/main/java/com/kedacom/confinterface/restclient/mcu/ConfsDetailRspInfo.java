package com.kedacom.confinterface.restclient.mcu;

public class ConfsDetailRspInfo {


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

    public int getNeed_password() {
        return need_password;
    }

    public void setNeed_password(int need_password) {
        this.need_password = need_password;
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

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    private String name;
    private String meeting_room_name;
    private String conf_id;
    private int conf_level;
    private int voice_activity_detection;
    private int vacinterval;
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
    private int auto_end;
    private int  preoccupy_resource;
    private int video_quality;
    private String encrypted_key;
    private int dual_mode;
    private int public_conf;
    private int max_join_mt;
    private int force_broadcast;
    private int fec_mode;
    private int need_password;
    private int doubleflow;
    private String platform_id;
    private Creator creator ;
}
