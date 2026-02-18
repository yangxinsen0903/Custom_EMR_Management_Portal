package com.sunbox;

/**
 * Created by jw on 2015/8/3.
 */
public class Global {

    public static final String SESSION_USERNAME="wxusername";
    public static final String SESSION_USERID="gdwsiuserid";//用户IDuserid
    public static final String SESSION_USEROUCODE="wxuseroucode";
    public static final String SESSION_OpenId ="gdwsiopenid";//OpenID
    public static final String SESSION_WXID ="gdwsiwxid";//OpenID
    public static final String SESSION_UserCity ="wxusercity";
    public static final String SESSION_USERHASH = "userHash";
    public static final String SESSION_CHANNELID = "Channel";//APP渠道还是微信
    public static final String SESSION_ISREGISTER="isRegistered";
    public static final String SESSION_USERTYPE="AllVIP";//是否会员
    public static final String SESSION_USERTYPE_ACT="ActVIP";//是否卡粉
    public static final String SESSION_USERTYPE_OIL="InUser";//是否内部用户
    public static final String SESSION_CHECKCODE="CheckCode";
    public static final String SESSION_DELACCOUNT_CODE="CheckDeleteAccountCode";
    public static final String SESSION_CHECKWEIXINREDBAGCODE="CheckWeinXinRedBagCode";
    public static final String SESSION_CHECKWEIXINRRCCODE="CheckWeinXinRRCCode";
    public static final String SESSION_SHAREUSERREDBAGCODE="ShareUserRedBagCode";
    public static final String SESSION_SHAREUSERGROUPREDBAGCODE="ShareUserGroupRedBagCode";
    public static final String SESSION_SHOPID = "shopId";         //当前登录门店ID

    public static final String User_GDAdminMac="gdadminmac";

    // region 访问令牌

    public static final String COOKIE_ACCESS_TOKEN = "accesstoken";
    public static final String PROPERTIES_ACCESS_TOKEN = "AccessTokenRadomString";

    // endregion

    //public static final String VIPUSERID = "VIPUserid";

    //region ̨session û ûid ûusercode
    public  static final String SESSION_USERNAME_END="username";
    public static final String SESSION_USERID_END="userid_end";
    public static final String SESSION_USEROUCODE_END="useroucode";
    public static final String SESSION_USEROUCODENAME_END="useroucodename";
    public static final String SESSION_USEROULABEL_END="useroulabel";
    public static final String SESSION_USEROULABELNAME_END="useroulabelname";
    public static final String SESSION_USEROUCODE_END1="useroucode1";
    public static final String SESSION_USEROUCODENAME_END1="useroucodename1";
    public static final String SESSION_USEROULABEL_END1="useroulabel1";
    public static final String SESSION_USEROULABELNAME_END1="useroulabelname1";
    //endregion

    //region 缓存信息
    public static final String WX_INDEX_AD="wx_index_ad";

    public static final String WX_INDEX_ActStartTime="starttime";



    //region mch
    public static final String UserID="UserID";
    public static final String MCH_UserID="MCH_UserID";
    public static final String MCH_NO="MCH_No";
    public static final String MCH_NAME="MCH_Name";
    public static final String MCH_TYPE="MCH_Type";
    //endregion

    public static final String LOGIN_USER_ID="loginUserId";// 已经登录的标记， 登录成功后作为cookie的name记录进cookie
    //dict 字典   销售频道
    public static final String DICT_SALE = "销售频道";
    public static final String DICT_SALE_POPULARITY  = "人气推荐";

    // region
    public static final String URL_PRODUCT_DETAIL = "/api/product/productInfo?productid=";

    // endregion

    // 默认每页数量
    public static final int PAGE_SIZE = 10;

    // region 活动相关的常量

    // 1团购, 2预售, 3秒杀, 4其他活动类型
    public static final String ACTIVITY_TYPE_GROUP = "1";
    public static final String ACTIVITY_TYPE_PRESALE = "2";
    public static final String ACTIVITY_TYPE_SECKILL = "3";
    public static final String ACTIVITY_TYPE_OTHER = "4";

    // 0正常活动状态, 1结束活动状态
    public static final String ACTIVITY_STATUS_ACTIVE = "0";
    public static final String ACTIVITY_STATUS_DONE = "1";

    public static final String CHANNEL_TYPE_POPULAR = "1";  // 热卖,人气商品

    // 销售频道
    public static final String SALECHANNEL_QRCODE = "9999";  // 扫描购

    // endregion

    // 商品类型
    public static final String PRODUCT_TYPE_VIRTUAL = "200";  // 虚拟商品
    public static final String PRODUCT_TYPE_HOTEL = "210";  // 酒店
    public static final String PRODUCT_TYPE_GATE = "220";  // 门票
    public static final String PRODUCT_TYPE_VOCAL = "230";//演唱会

    // 订单类型
    public static final int ORDER_TYPE_NORMAL = 100;  // 普通订单
    public static final int ORDER_TYPE_SHARE = 110;  // 分享订单
    public static final int ORDER_TYPE_VIRTUAL = 200;  // 虚拟商品订单
    public static final int ORDER_TYPE_QRCODE = 300;  // 扫码购订单
    public static final int ORDER_TYPE_YYG=400;  //一元购订单
    public static final int ORDER_TYPE_NEW_GIFTS=401;  //新手大礼包
    public static final int ORDER_TYPE_COUPON_100_28=402;  //100减28券(20190401至20190630油非互动)
    public static final int ORDER_TYPE_BATCH=500; //批量订单
    public static final int ORDER_TYPE_CLS=600;
    public static final int ORDER_TYPE_QJF=700; //全积分
    public static final int ORDER_TYPE_QJD=800; //全金豆订单
    public static final int ORDER_TYPE_CYD=810; //柴油豆订单
    public static final int ORDER_TYPE_DGZF=900; //对公支付

    // region 订单渠道

    public static final String ORDER_CHANNEL_WECHAT = "100";  // 微信

    // endregion

    // region 日志审计
    public static final int LOG_AUDIT_CART = 2000;
    public static final String LOG_AUDIT_CART_TEXT = "购物车结算";

    public static final int LOG_AUDIT_CHECKOUT = 2010;
    public static final String LOG_AUDIT_CHECKOUT_TEXT = "立即结算";

    public static final int LOG_AUDIT_PAYMENT = 2020;
    public static final String LOG_AUDIT_PAYMENT_TEXT = "立即支付";

    public static final int LOG_AUDIT_ORDER = 2030;
    public static final String LOG_AUDIT_ORDER_TEXT = "订单";
    // endregion

    public static final String LOG_Product="9000-商品详情";
    public static final String LOG_ORDER="10000-创建订单:";
    public static final String LOG_ORDER_CREATE="10002-创建订单:";
    public static final String LOG_ORDER_PAY="10003-支付订单:";
    public static final String LOG_ORDER_WX="10004-订单微信支付回调:";
    public static final String LOG_ORDER_QRCODE="10005-扫码订单:";
    public static final String LOG_ORDER_HX="10006-海信";
    public static final String LOG_ORDER_CANCEL="10007-取消订单";
    public static final String LOG_ORDER_MS="10008-秒杀";
    public static final String LOG_COUPON="10009-优惠券";

    public static final String LOG_LOGIN="90000-登录处理";
    public static final String LOG_EXCPETION="90009-异常捕获";

    public static final String LOG_COOKIES="30001-Cookies";

    public static final String LOG_APPHOME="30002-APPHOME";

    public static final String LOG_APP_FZS="40000-充值";

    public static final String LOG_XCX_LOGIN="50000-小程序";

    public static final String LOG_XCX_PRODUCTINFO="50001-小程序商品详情";

    public static final String LOG_XCX_PAY="50002-小程序支付";

    public static final String LOG_SSO="60000-单点登录";

}
