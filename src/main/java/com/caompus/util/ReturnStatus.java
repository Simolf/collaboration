package com.caompus.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by credtitone on 2016/10/23.
 * 返回状态码
 */
public class ReturnStatus {
    public static final String typeOfPasswordError = "passwordError";
    public static final int passwordErrorCode = 100;
    public static final String passwordErrorDesc = "密码错误";

    public static final String typeOfMissParameter = "missParamter";
    public static final int missParameterCode = 101;
    public static final String missParameterDesc = "参数不能为空";

    public static final String typeOfParameterError = "parameterError";
    public static final int parameterErrorCode = 102;
    public static final String parameterErrorDesc = "参数错误";

    public static final String typeOfSuccess = "success";
    public static final int successCode = 200;
    public static final String successDesc = "成功";

    public static final String typeOfTokenUnavailable = "tokenUnavailable";
    public static final int tokenUnavailableCode = 300;
    public static final String tokenUnavailableDesc = "Token失效";

    public static final String typeOfInnerError = "innerError";
    public static final int innerErrorCode = 500;
    public static final String innerErrorDesc = "内部执行错误";

    public static final int SC_FAIL = 400;
    public static final int SC_OK = 200;



    public static JsonObject getStatusObj(int codeType){
        JsonObject retJson = new JsonObject();
        int status = 0;
        String statusDesc = "";

        if (codeType == ReturnStatus.passwordErrorCode){
            status = ReturnStatus.passwordErrorCode;
            statusDesc = ReturnStatus.passwordErrorDesc;
        } else if (codeType == ReturnStatus.missParameterCode) {
            status = ReturnStatus.missParameterCode;
            statusDesc = ReturnStatus.missParameterDesc;
        }else if (codeType == ReturnStatus.parameterErrorCode){
            status = ReturnStatus.parameterErrorCode;
            statusDesc = ReturnStatus.parameterErrorDesc;
        }else if (codeType == ReturnStatus.tokenUnavailableCode){
            status = ReturnStatus.tokenUnavailableCode;
            statusDesc = ReturnStatus.tokenUnavailableDesc;
        }else if (codeType == ReturnStatus.innerErrorCode){
            status = ReturnStatus.innerErrorCode;
            statusDesc = ReturnStatus.innerErrorDesc;
        } else if (codeType == ReturnStatus.successCode) {
            status = successCode;
            statusDesc = successDesc;
        }
        retJson.put("status",status);
        retJson.put("status_desc",statusDesc);
        return retJson;
    }

    public static String getFailReturnObj(String key){
        JsonObject retJson = new JsonObject();
        retJson.put("status",301);
        retJson.put(key,new JsonObject());
        return retJson.toString();
    }

    public static String getFailReturnArray(String key){
        JsonObject retJson = new JsonObject();
        retJson.put("status","301");
        retJson.put(key,new JsonArray());
        return retJson.toString();
    }
}
