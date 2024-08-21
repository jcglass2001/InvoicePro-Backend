package com.jcg.bankingapi.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;

import static com.twilio.rest.api.v2010.account.Message.creator;


public class SmsUtils {
    @Value("${twilio.from_number}")
    private String FROM_NUMBER;
    @Value("${twilio.sid_key}")
    private String SID_KEY;
    @Value("${twilio.token_key}")
    private String TOKEN_KEY;

//    public static void sendSMS(String to, String messageBody){
//        Twilio.init(SID_KEY, TOKEN_KEY);
//        Message message = creator(new PhoneNumber("+1" + to), new PhoneNumber(FROM_NUMBER), messageBody).create();
//        System.out.println(message);
//    }
}
