package com.caompus.util;

import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author chenxiaoxin
 * @title:
 * @date 2016/12/30
 * @className MD5Util
 */
public class MD5Util {

    /**
     * MD5加密
     * @param str
     * @return
     */
    public static String EncoderByMd5(String str){
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            BASE64Encoder base64Encoder = new BASE64Encoder();
            //加密
            String newStr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
            return newStr;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }




}
