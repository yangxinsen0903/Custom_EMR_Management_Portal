package com.sunbox.service.consts;

public class SheinParamConstant {

    public static final String SHEIN_UID = "uid";
    public static final String SHEIN_NAME = "name";
    public static final String SHEIN_RESULTS = "results";
    public static final String SHEIN_CMDB_SERVICE_URL = "/api/v3/cmdb/service/";
    public static final String SHEIN_CMDB_SYSTEM_URL = "/api/v3/cmdb/system/";
    public static final String SHEIN_XTOKEN_NAME = "x-token";

    public static final String VALID = "VALID";
    // 暂时没用到 INVALID
    public static final String INVALID = "INVALID";
    public static final String EXPIRED = "EXPIRED";

    public static final String READ = "READ";
    public static final String READWRITE = "READWRITE";

    public static final Long TIME_24H = 60 * 60 * 24L;

    public static final String APITOKEN = "apitoken";

    public static final String REQUEST_TYPE_CREATE = "create";
    public static final String REQUEST_TYPE_DESTORY = "destory";
    //agree，refuse，back，revoke
    public static final String APPROVAL_STATE_INIT = "init";
    public static final String APPROVAL_STATE_AGREE = "agree";
    public static final String APPROVAL_STATE_REFUSE = "refuse";
    public static final String APPROVAL_STATE_BACK = "back";
    public static final String APPROVAL_STATE_REVOKE = "revoke";


}
