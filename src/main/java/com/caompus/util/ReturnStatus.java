package com.caompus.util;

import io.vertx.core.json.JsonObject;

/**
 * Created by credtitone on 2016/10/23.
 * 返回状态码
 */
public class ReturnStatus {
    public static final String typeOfPasswordError = "passwordError";
    public static final String passwordErrorCode = "100";
    public static final String passwordErrorDesc = "密码错误";

    public static final String typeOfMissParameter = "missParamter";
    public static final String missParameterCode = "101";
    public static final String missParameterDesc = "参数不能为空";

    public static final String typeOfParameterError = "parameterError";
    public static final String parameterErrorCode = "102";
    public static final String parameterErrorDesc = "参数错误";

    public static final String typeOfSuccess = "success";
    public static final String successCode = "200";
    public static final String successDesc = "成功";

    public static final String typeOfTokenUnavailable = "tokenUnavailable";
    public static final String tokenUnavailableCode = "300";
    public static final String tokenUnavailableDesc = "Token失效";

    public static final String typeOfInnerError = "innerError";
    public static final String innerErrorCode = "500";
    public static final String innerErrorDesc = "内部执行错误";

    public static JsonObject getStatusObj(String codeType){
        JsonObject retJson = new JsonObject();
        String status = "";
        String statusDesc = "";

        if (codeType.equals(ReturnStatus.typeOfPasswordError)){
            status = ReturnStatus.passwordErrorCode;
            statusDesc = ReturnStatus.passwordErrorDesc;
        } else if (codeType.equals(ReturnStatus.typeOfMissParameter)) {
            status = ReturnStatus.missParameterCode;
            statusDesc = ReturnStatus.missParameterDesc;
        }else if (codeType.equals(ReturnStatus.typeOfParameterError)){
            status = ReturnStatus.parameterErrorCode;
            statusDesc = ReturnStatus.parameterErrorDesc;
        }else if (codeType.equals(ReturnStatus.typeOfTokenUnavailable)){
            status = ReturnStatus.tokenUnavailableCode;
            statusDesc = ReturnStatus.tokenUnavailableDesc;
        }else if (codeType.equals(ReturnStatus.typeOfInnerError)){
            status = ReturnStatus.innerErrorCode;
            statusDesc = ReturnStatus.innerErrorDesc;
        } else if (codeType.equals(ReturnStatus.typeOfSuccess)) {
            status = successCode;
            statusDesc = successDesc;
        }
        retJson.put("status",status);
        retJson.put("status_desc",statusDesc);
        return retJson;
    }
}
