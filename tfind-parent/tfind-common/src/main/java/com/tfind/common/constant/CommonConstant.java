package com.tfind.common.constant;

public class CommonConstant {

    public static final String ADMIN_USER_ID = "A001";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    public static final Integer DEL_TRUE = 1;
    public static final Integer DEL_FALSE = 0;

    public static final int SOFT_DELETE_DAYS = 30;

    public static final String REDIS_USER_PREFIX = "tfind:user:";
    public static final String REDIS_TOILET_PREFIX = "tfind:toilet:";
    public static final String REDIS_TOKEN_PREFIX = "tfind:token:";

    public static final String MQ_USER_QUEUE = "tfind.user.queue";
    public static final String MQ_TOILET_QUEUE = "tfind.toilet.queue";

    private CommonConstant() {
    }
}
