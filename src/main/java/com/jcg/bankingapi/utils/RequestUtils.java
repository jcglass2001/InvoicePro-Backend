package com.jcg.bankingapi.utils;

import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import static nl.basjes.parse.useragent.UserAgent.*;

public class RequestUtils {
    private static final String X_FORWARDED_FOR_HEADER = "X-FORWARDED-FOR";

    public static String getIpAddress(HttpServletRequest request){
        String ipAddress = UNKNOWN_VALUE;
        if(request != null){
            ipAddress = request.getHeader(X_FORWARDED_FOR_HEADER);
            if(ipAddress == null || ipAddress.isEmpty()){
                ipAddress = request.getRemoteAddr();
            }
        }
        return ipAddress;
    }
    public static String getDevice(HttpServletRequest request){
        UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
                .hideMatcherLoadStats()
                .withCache(1000)
                .build();
        UserAgent agent = userAgentAnalyzer.parse(request.getHeader(USERAGENT_HEADER));
        System.out.println(agent);
        return agent.getValue(OPERATING_SYSTEM_NAME) + " - " + agent.getValue(AGENT_NAME) + " - " + agent.getValue(DEVICE_NAME);
    }
}
