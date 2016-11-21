package com.caompus.userVerticle;

import com.caompus.dataSourceVerticle.MemberOperateServiceVerticle;
import com.caompus.util.Key;
import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Created by credtitone on 2016/10/23.
 * 获取token，将用户信息存放到共享内存shareData（）中
 *
 */
public class LoginVerticle extends AbstractVerticle{
    Logger logger = Logger.getLogger(LoginVerticle.class);

    @Override
    public void start() throws Exception {
        System.out.println("call LoginVerticle function");
        vertx.eventBus().consumer(this.getClass().getName(),handler->{
            handle(handler);
        });
    }
    public void handle(Message<Object> handler){
        String paramString = handler.body().toString();
        JsonObject paramJson = new JsonObject(paramString);

        String method = paramJson.containsKey("method")?paramJson.getValue("method").toString():"";
        if ("login".equals(method)){
            handleLogin(handler);
        }else if ("logout".equals(method)){
            handleLogout(handler);
        }else if ("register".equals(method)){
            handleRegister(handler);
        }
    }

    /**
     * 处理用户登录，生成token
     * @param handler
     */
    public void handleLogin(Message<Object> handler){
        LocalMap<String,String> tokenMap = vertx.sharedData().getLocalMap(Key.TOKEN_MAP);//userId->token
        LocalMap<String,Long> timeMap = vertx.sharedData().getLocalMap(Key.TIME_MAP);    //token->time
        LocalMap<String,JsonObject> permissionMap = vertx.sharedData().getLocalMap(Key.PERMISSION_MAP);//token -> 权限
        JsonObject ret = ReturnStatus.getStatusObj(ReturnStatus.typeOfTokenUnavailable);
        ret.put("token","");
        ret.put("active_date",2);

        String param = handler.body().toString();
        JsonObject paramJson = new JsonObject(param);

        String uuid = paramJson.containsKey("uuid")?paramJson.getString("uuid"):"";

        //登录
        String userId = paramJson.containsKey("userId")?paramJson.getValue("userId").toString():"";        //帐号
        String password = paramJson.containsKey("password")?paramJson.getValue("password").toString():"";  //密码
//        String requestIP = paramJson.containsKey("requestIP")?paramJson.getValue("requestIP").toString():"";   //请求访问的IP地址

        if ("".equals(userId) || "".equals(password)){
            ret = ReturnStatus.getStatusObj(ReturnStatus.typeOfMissParameter);
            ret.put("token","");
            ret.put("active_date",2);
            logger.info(uuid+" "+ret);
            handler.reply(ret.toString());
            return;
        }

        try{
            JsonObject queryJson = new JsonObject();
            queryJson.put("method","innerQueryMember");
            queryJson.put("data",paramJson);

            DeliveryOptions options = new DeliveryOptions().setSendTimeout(10*1000);

            Future loginFuture = Future.future();
            Future successFuture = Future.future();
            Future failFuture = Future.future();
            String tokenUUID = UUID.randomUUID().toString();
            String tokenKey = userId;

            vertx.eventBus().send(MemberOperateServiceVerticle.class.getName(),queryJson.toString(),options,ar->{
                if (ar.succeeded()){
                    //complete调用loginFuture.setHadler
                    loginFuture.complete(ar.result().body().toString());
                }else {
                    String str = "用户名校验回调失败"+ar.cause();
                    failFuture.fail(str);
                    logger.error(uuid+" "+str);
                }
            });


            CompositeFuture.all(loginFuture,successFuture).setHandler(ar->{

            });

            loginFuture.setHandler(futureHandler->{
               JsonObject finalRet = ReturnStatus.getStatusObj(ReturnStatus.typeOfTokenUnavailable);
                finalRet.put("token","");
                finalRet.put("active_date",2);

                if (loginFuture.succeeded()){
                    String futureRes = loginFuture.result().toString();
                    JsonObject userInfoJson = new JsonObject(futureRes);

                    if (!userInfoJson.isEmpty()){
                        String userName = userInfoJson.containsKey("userName")?userInfoJson.getValue("userName").toString():"";
                        String roleType = userInfoJson.containsKey("roleType")?userInfoJson.getValue("roleType").toString():"";
                        String expirationDate = userInfoJson.containsKey("expirationDate")?userInfoJson.getValue("expirationDate").toString():"";
                        finalRet = ReturnStatus.getStatusObj(ReturnStatus.typeOfSuccess);
                        finalRet.put("active_date", 2);
                        finalRet.put("userName",userName);
                        finalRet.put("token", tokenUUID);
                        finalRet.put("roleType",roleType);
                        //判断token是否失效，未失效则继续使用
                        if (tokenMap.get(tokenKey) != null && timeMap.get(tokenMap.get(tokenKey)) != null) {
                            if (System.currentTimeMillis() <= timeMap.get(tokenMap.get(tokenKey))) {
                                finalRet.put("token", tokenMap.get(tokenKey));
                                logger.info(uuid + "token未过期可继续使用" + finalRet);
                                return;
                            }
                        }
                        //每次保存前先清除
                        removeUserToken(userId);
                        //权限
                        JsonObject permissionObj = new JsonObject();
                        permissionObj.put("userName",userName);
                        permissionObj.put("roleType",roleType);
                        permissionObj.put("expirationDate",expirationDate);
                        permissionMap.put(tokenUUID,permissionObj);
                        //保存新的token
                        tokenMap.put(tokenKey,tokenUUID);
                        //token保存两个小时，登录有效时间为2个小时
                        timeMap.put(tokenUUID,System.currentTimeMillis()+2*60*60*1000);
                        successFuture.complete(finalRet.toString());

                    }else {
                        failFuture.complete(finalRet.toString());
                    }
                }else {
                    failFuture.complete(finalRet.toString());
                }
            });

            successFuture.setHandler(futureHadler->{
               String retString = "";
                if (successFuture.succeeded()){
                    retString = successFuture.result().toString();
                    logger.info(uuid+" "+retString);
                    handler.reply(retString);
                }else {
                    logger.error(uuid+" "+successFuture.cause().getMessage());
                    failFuture.fail(successFuture.cause().getMessage());
                }
            });
            failFuture.setHandler(futureHandler->{
                String retString = "";
                if (failFuture.failed()) {
                    JsonObject finalRet = ReturnStatus.getStatusObj(ReturnStatus.typeOfTokenUnavailable);
                    finalRet.put("token", "");
                    finalRet.put("active_date", 2);
                    retString = finalRet.toString();
                }else {
                    retString = failFuture.result().toString();
                }
                logger.equals(uuid+" "+retString);
                handler.reply(retString);
            });

        }catch (Exception e){
            handler.reply(ret.toString());
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 用户注销
     * @param handler
     */
    public void handleLogout(Message<Object> handler){
        String paramString = handler.body().toString();
        JsonObject paramJson = new JsonObject(paramString);

        JsonObject retJson = new JsonObject();

        String userId = paramJson.containsKey("userId")?paramJson.getValue("userId").toString():"";
        if (userId == null || "".equals(userId)){
            retJson = ReturnStatus.getStatusObj(ReturnStatus.missParameterCode);
            handler.reply(retJson.toString());
        }
        removeUserToken(userId);
        retJson = ReturnStatus.getStatusObj(ReturnStatus.successCode);
        handler.reply(retJson.toString());

    }

    /**
     * 用户注册
     * @param handler
     */
    public void handleRegister(Message<Object> handler){
        String paramString = handler.body().toString();
        JsonObject paramJson = new JsonObject(paramString);

        String phone = paramJson.containsKey("phone")?paramJson.getValue("phone").toString():"";
        String userName = paramJson.containsKey("userName")?paramJson.getValue("userName").toString():"";
        String password = paramJson.containsKey("password")?paramJson.getValue("password").toString():"";

    }

    /**
     * 清除指定用户的token，超时时间、权限
     * @param userId
     */
    public void removeUserToken(String userId){
        LocalMap<String,String> tokenMap = vertx.sharedData().getLocalMap(Key.TOKEN_MAP);
        LocalMap<String,Long> timeMap = vertx.sharedData().getLocalMap(Key.TIME_MAP);
        LocalMap<String,JsonObject> permissionMap = vertx.sharedData().getLocalMap(Key.PERMISSION_MAP);
        String tokenKey = "";
        if (!"".equals(userId)){
            tokenKey = userId;
        }
        String token = tokenMap.get(tokenKey);
        if (token!=null){
            Long timeMapValue = timeMap.get(token);
            if (timeMapValue != null){
                timeMap.remove(token);
            }
            if (permissionMap.get(token) != null){
                permissionMap.remove(token);
            }
            tokenMap.remove(tokenKey);
        }

    }


}
