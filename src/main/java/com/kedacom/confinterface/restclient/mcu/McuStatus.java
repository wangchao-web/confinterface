package com.kedacom.confinterface.restclient.mcu;

public enum McuStatus {
    OK(0, "success"),
    Authentication(10001, "软件认证失败"),
    AccountTokenAuthentication(10002, "accountToken认证失败"),
    PasswordError(10101, "password 验证错误"),
    TimeOut(10102, "用户登录超时或者没有登录"),
    ConfNotExist(12103, "会议不存在"),
    TerminalNotExist(12104, "终端不存在"),
    McuWaitTimeout(20001, "MCU等待应答超时"),
    McuIsInitialing(20002, "MCU正在初始化"),
    McuIsBusy(20003, "MCU正忙"),
    LogicalChannelFail(20054, "得到的终端逻辑通道失败"),
    NoResourceForConf(20101, "无法为此会议分配资源，请稍候再创建新会议"),
    ReachMaxConfNum(20102, "创建会议失败,已达最大会议数"),
    E164Exist(20107, "该E164号码已存在"),
    ConfBitRateNotSupport(20120, "会议码率不支持"),
    MediaTypeNotSupport(20121, "指定的会议音频或视频格式不支持"),
    ReachMaxJoinTerminalNums(20301, "超过MCU最大接入终端能力"),
    TerminalJoinConfFail(20382, "接入能力满,终端呼入失败"),
    HDTerminalJoinFail(20383, "高清终端接入能力满, 呼叫终端失败"),
    TerminalCannotReach(20401, "指定终端不可及, 可能不在线"),
    TerminalRejectJoinConf(20402, "指定终端拒绝加入会议"),
    TerminalIsInConf(20403, "指定终端已在会议中"),
    TerminalIsNotChair(20404, "非主席终端不能执行此项操作"),
    CalledTerminalIsInConf(20405, "呼叫的终端已经与会"),
    CalledTerminalHasNoValidAddress(20406, "呼叫的终端没有可用的呼叫地址信息"),
    TerminalIsBusy(20410, "指定终端忙, 可能在另一会议中"),
    TerminalTypeIsInvalid(20421, "呼叫的终端类型与MCU类型不匹配"),
    TerminalOnlyRecv(20431, "指定终端是只接收终端"),
    TerminalIsAChair(20432, "被指定终端已经是会议主席"),
    CannotSetChair(20433, "会议处于无主席模式,不能指定主席"),
    HasNoChair(20434, "会议中无主席"),
    TerminalNotInConf(20435, "指定终端未与会"),
    TerminalIsASpeaker(20437, "指定终端已经是发言终端"),
    ExistSpeaker(20438, "会议中已有发言人"),
    NoSpeaker(20439, "会议中无发言人"),
    MediaTypeDifferent(20501, "源与目的的媒体类型不同, 不能选看"),
    CannotInspectHDTerminal(20506, "会控不能选看高清终端"),
    AudioInspectionBeRejected(20507, "音频选看被拒绝, 选看模式改变"),
    VideoInspectionBeRejected(20508, "视频选看被拒绝, 选看模式改变"),
    NoVideoAdapterResource(20509, "视频适配资源不足, 视频选看失败"),
    NoAudioAdapterResource(20510, "音频适配资源不足, 音频选看失败"),
    NoSystemResource(20511, "由于系统资源有限, 您不能进行选看操作"),
    AudioTerminalCannotBeMonitor(20512, "音频终端不能做监控源"),
    TerminalHasNoVideoSrc(20513, "终端没有视频源"),
    CannotControlConf(20521, "会议控制权被其它会控独享"),
    InvalidConfProtectMode(20522, "无效的会议保护方式"),
    InvalidCallPolicy(20523, "无效的终端呼叫策略"),
    CalledTerminalsReachLicenseAllowd(20565, "呼叫终端数超出License授权点数"),
    CalledTerminalsReachConfTerminals(20566, "呼叫终端数超过会议终端数"),
    ConfRecording(20801, "当前正在进行会议录像"),
    ConfNotRecording(20802, "当前不在进行会议录像"),
    Mixing(20811, "当前正在进行会议混音"),
    NotMixing(20812, "当前不在进行会议混音"),
    NotPauseMix(20813, "当前未被暂停会议混音"),
    GKRejectCall(21505, "呼叫被GK拒绝"),
    InvalidCallSignalAddr(21506, "无效呼叫信令地址"),
    ReduplicateName(21508, "MCU别名或E164号或会议E164号与GK上其他网络实体重复"),
    PeerBusy(21509, "对方忙"),
    GkUnknownReason(21510, "GK不明原因"),
    NormalHangUp(21511, "正常挂断"),
    GkOperTimeout(21513, "GK操作超时"),
    MCURegisterGKFail(21521, "MCU未成功注册GK"),
    InternalError(21522, "呼叫时发生内部错误"),
    NotAllowedSecondBroadcastSrc(21745, "有终端正在发双流, 不能设置第二广播源"),
    ExistSecondBroadcastSrc(21746, "有第二广播源, 不能发双流"),
    DesignatedTerminalNotInConf(22004, "指定终端不在此会议中"),
    TerminalCannotSendMedia(22005, "终端为收视终端, 不能发送媒体数据"),
    TerminalNotSupportOper(22006, "终端为收视终端, 不能进行此项操作"),
    NotAllowedSilenceOper(22007, "硬件检测失败，无法进行远端静音操作"),
    DeviceNotExist(22008, "设备不存在，无法进行此操作"),
    TerminalCannotLeftConf(22009, "终端不能离开原会议加入新会议"),
    TerminalCannotProcessCmd(22010, "终端忙, 无法处理此命令"),
    McuReject(22014, "MCU操作拒绝"),
    ReachMaxConfs(27505, "已达最大会议数"),
    HasNoResourceForCreateConf(27506, "无创会所需资源"),
    ConfCreated(27507, "会议已开启"),
    ConferenceNotExist(27508, "会议不存在"),
    NeedIFrameError(28513, "请求关键帧错误"),
    SetPayloadError(28516, "设置PayLoad错误"),
    ChannelNotSupportCap(28518, "通道不支持当前能力"),
    ApplySocketFail(28519, "申请端口失败"),
    CreateResourceFail(28520, "创建资源失败"),
    GetBasObjectError(28532, "获取Bas对象错误"),
    SetEncodeParamError(28534, "设置编码参数失败"),
    MessageDealTimeout(28535, "消息处理超时"),
    SaveAudioBasMap(29104, "保存音频Bas句柄映射表错误"),
    Unknown(99999, "Unknown");

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static McuStatus valueOf(int statusCode) {
        McuStatus status = resolve(statusCode);
        if (status == null) {
            throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
        } else {
            return status;
        }
    }

    public static McuStatus resolve(int statusCode) {
        McuStatus[] mcuStatuses = values();
        int statusNum = mcuStatuses.length;

        for(int index = 0; index < statusNum; ++index) {
            McuStatus status = mcuStatuses[index];
            if (status.value == statusCode) {
                return status;
            }
        }

        return McuStatus.Unknown;
    }

    private final int value;
    private final String description;

    McuStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
