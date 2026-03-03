package com.yiyitech.mf.constants;

/**
 * @author tianhn
 * @version 1.0
 * @ClassName com.yiyitech.ads.constants.Constants
 * @Description 常量
 * @date 2021/6/28 16:54
 */
public class CommonConstants {
    //默认数据表状态值
    public static final boolean FORM_STATUS_EFFECTIVE = true;
    public static final boolean FORM_STATUS_INVALID = false;

    //默认排序
    public static final Integer ASC_ORDER = 1;
    public static final Integer DESC_ORDER = 2;

    public static final int REDIS_DB = 1;
    //缓存前缀：sp / ads
    public static final String SP_WM_PREFIX_KEY = "sp:wm:";
    public static final String SP_PREFIX_KEY = "sp:";
    public static final String ADS_PREFIX_KEY = "ads:";
    //ads：账号令牌
    public static final String ACCOUNT_ACCESS_TOKEN = "account:accessToken:";
    public static final String ACCOUNT_REFRESH_TOKEN = "account:refreshToken:";

    //机器人发送去重
    public static final String SEND_DEDUP_NS = "wecom:send:dedup:";

    /**
     * ads任务
     */
    //解析报告任务：加锁
    public static final String GETREPORT_LOCK = "getReport:lock:";
    //自动否词任务：加锁
    public static final String ANTO_NEG_LOCK = "auto:neg:lock";
    //日报机器人推送任务：加锁
    public static final String ANTO_DAILY_BOBOT_PUSH_LOCK = "auto:daily:robot:push:lock";
    //
    public static final String ACCESS_TOKEN_REDIS_KEY = "access_token";

    /**
     * sp任务
     */
    //创建sp报告
    public static final String CREATE_REPORT = "create:report:";
    //获取sp报告
    public static final String GET_REPORT = "get:report:";
    //解析sp报告
    public static final String PARSE_REPORT = "parse:report:";
    //sp报告翻译+覆盖率计算
    public static final String TERM_COVERGE = "term:coverage:";
    //获取ABA类目信息
    public static final String CATEGOR_ENRICH = "category:enrich:";
    //aba词总表缓存标识：翻译
    public static final String TERM_SUMMARY_TRANSLATE = "term_summary:translate:";
    //aba词总表缓存标识：趋势
    public static final String TERM_SUMMARY_TREND = "term_summary:trend:";
    //aba词总表缓存标识：seed
    public static final String TERM_SUMMARY_FIRST_SEEN = "term_summary:seen:";

    //创建、获取、解析sp-Category报告
    public static final String CREATE_CATEGORY_REPORT = "create:category:report:";
    public static final String GET_CATEGORY_REPORT = "get:category:report:";
    public static final String PARSE_CATEGORY_REPORT = "parse:category:report:";

    //雷达机器人推送机会词：加锁
    public static final String RADAR_OPPORTUNITY_PUSH_LOCK = "radar:opportunity:push:lock";

    //活动，活动组，关键词状态（可用、暂停、归档）
    public static final String ENABLED_STATE = "ENABLED";
    public static final String PAUSED_STATE = "PAUSED";
    public static final String ARCHIVED_STATE = "ARCHIVED";

    //关键词匹配状态（词组、精准、广泛、靶向匹配、自定义靶向匹配）
    public static final String BROAD = "BROAD";
    public static final String EXACT = "EXACT";
    public static final String PHRASE = "PHRASE";
    public static final String TARGETING_EXPRESSION = "TARGETING_EXPRESSION";
    public static final String TARGETING_EXPRESSION_PREDEFINED = "TARGETING_EXPRESSION_PREDEFINED";

    //北京时间，美西(洛杉矶)时间
    public static final String BEIJING_TIME = "beijingTime";
    public static final String LOS_ANGELES_TIME = "pacificTime";
}
