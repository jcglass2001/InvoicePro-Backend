package com.jcg.bankingapi.constant;

public class Constants {
    //Security
    public static final String[] PUBLIC_URLS = {
            "/user/authenticate/**", "/user/register/**", "/user/verify/**",
            "/user/resetpassword/**", "/user/refresh/token/**", "/user/profile/**", "/user/image/**",
            "/user/reset/password/**", "/actuator/env"
    };
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String[] PUBLIC_ROUTES = {"/user/authenticate", "/user/register","/user/refresh/token", "/user/image/**", "/user/reset/password"};
    public static final String HTTP_OPTIONS_METHOD = "OPTIONS";
    //Token Provider
    public static final String JCG_LLC = "";
    public static final String CUSTOMER_MANAGEMENT_SERVICE = "";
    public static final String AUTHORITIES = "";
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 1_800_000;
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 432_00_000;
}
