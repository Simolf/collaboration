package com.caompus.dataSourceVerticle;

import com.caompus.util.Common;
import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import javafx.beans.property.adapter.ReadOnlyJavaBeanBooleanProperty;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by credtitone on 2016/10/23.
 */
public class MemberOperateServiceVerticle extends AbstractVerticle{
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Logger logger = Logger.getLogger(MemberOperateServiceVerticle.class);

    public static final Set methodName = new HashSet(){{
        add("login");
        add("register");
        add("getPersonInfo");
        add("isRegister");


    }};

    @Override
    public void start(){
        vertx.eventBus().consumer(this.getClass().getName(),handler->{
            logger.info("====MemberOperateServiceVerticle start===");
           handle(handler);
        });
    }
    public void handle(Message<Object> handler){
        JsonObject retObj = ReturnStatus.getStatusObj(ReturnStatus.successCode);
        retObj.put("data",new JsonArray());

        try {
            JsonObject paramObj = new JsonObject(handler.body().toString());
            if (paramObj.isEmpty()){
                retObj = ReturnStatus.getStatusObj(ReturnStatus.missParameterCode);
                retObj.put("data",new JsonArray());
                handler.reply(retObj);
                return;
            }
            String method = paramObj.containsKey("method")?paramObj.getString("method"):"";
            //校验方法
            if (!methodName.contains(method)){
                retObj = ReturnStatus.getStatusObj(ReturnStatus.parameterErrorCode);
                retObj.put("data",new JsonArray());
                handler.reply(retObj);
                return;
            }
            if ("login".equals(method)){
                loginMethod(handler,paramObj);
            }else if ("register".equals(method)){
                registerMethod(handler,paramObj);
            }else if ("getPersonInfo".equals(method)){
                userInfoMethod(handler);
            }else if ("isRegister".equals(method)){
                isRegister(handler);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 查询用户信息
     * @param handler
     * @param paramObj
     */
    public void loginMethod(Message<Object> handler, JsonObject paramObj){
        logger.info("====MemberOperateServiceVerticle Login===="+paramObj.toString());
        JsonObject paramJson = paramObj.containsKey("param")?paramObj.getJsonObject("param"):new JsonObject();
        String phone = paramJson.containsKey("userId")?paramJson.getValue("userId").toString():"";
        String password = paramJson.containsKey("password")?paramJson.getValue("password").toString():"";

        Future queryMemberFuture = Future.future();
        Future failFuture = Future.future();

        if ("".equals(phone) || "".equals(password)){
            failFuture.complete("账号或密码为空");
            return;
        }

        String sqlString = "select phone \"id\", user_name \"userName\" from t_user_base_info where phone = ? and password = ?";
        JsonArray valueArray = new JsonArray();
        valueArray.add(phone);
        valueArray.add(password);
        JsonObject sqlObj = new JsonObject();
        sqlObj.put("sqlString", sqlString);
        sqlObj.put("method","select");
        sqlObj.put("values",valueArray);

        logger.info("====login==="+sqlObj.toString());


        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),sqlObj.toString(),queryMemberFuture.completer());

        queryMemberFuture.compose(futureHandler->{
            String queryResult = ((Message<Object>)queryMemberFuture.result()).body().toString();
            JsonObject queryObj = new JsonObject(queryResult);
            JsonObject retJson = new JsonObject();
            if (!queryObj.getJsonArray("data").isEmpty()){
                retJson.put("data",queryObj.getJsonArray("data").getJsonObject(0));
            }else {
                retJson.put("data",new JsonObject());
            }
            handler.reply(retJson.toString());
        },failFuture);

        failFuture.setHandler(fail->{
            JsonObject retJson = new JsonObject();
            retJson.put("status","201");
            retJson.put("data","操作失败");
            handler.reply(retJson.toString());
        });

    }

    /**
     * 用户注册插入数据
     * @param handler
     * @param paramObj
     */
    public void registerMethod(Message<Object> handler,JsonObject paramObj){
        JsonObject paramJson = paramObj.containsKey("param")?paramObj.getJsonObject("param"):new JsonObject();
        String userId = paramJson.containsKey("userId")?paramJson.getValue("userId").toString():"";
        String password = paramJson.containsKey("password")?paramJson.getValue("password").toString():"";
        String userName = paramJson.containsKey("userName")?paramJson.getValue("userName").toString():"";

        Future failFuture = Future.future();
        Future registerFuture = Future.future();

        if ("".equals(userId) || "".equals(password) || "".equals(userName)){
            failFuture.complete("参数为空");
            return;
        }

        String sqlString = "insert into t_user_base_info(phone,user_name,password)" +
                "values(?,?,?)";
        JsonArray valueArray = new JsonArray();
        valueArray.add(userId);
        valueArray.add(userName);
        valueArray.add(password);
        JsonObject sqlObj = new JsonObject();
        sqlObj.put("method","insert");
        sqlObj.put("sqlString",sqlString);
        sqlObj.put("values",valueArray);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),sqlObj.toString(),messageAsyncResult -> {
            if (messageAsyncResult.succeeded()) {
                JsonObject retJson = new JsonObject();
                String result = messageAsyncResult.result().body().toString();
                JsonObject respObj = new JsonObject(result);
                if (respObj.getBoolean("isSuccess")) {
                    retJson.put("status", ReturnStatus.SC_OK);
                    retJson.put("user", new JsonObject().put("id", userId).put("name", userName));
                    handler.reply(retJson.toString());
                } else {
                    retJson.put("status", ReturnStatus.SC_OK);
                    handler.reply(retJson.toString());
                }
            }else {
                handler.reply(new JsonObject().put("status",ReturnStatus.SC_FAIL));
            }
        });



        failFuture.setHandler(fail->{
            handler.fail(201,failFuture.result().toString());
            logger.error(failFuture.result().toString());
        });
    }

    /**
     * 获取用户信息
     * @param handler
     */
    public void userInfoMethod(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String phone = paramObj.getString("phone");

        String sql = "select phone \"id\",user_name \"userName\" from t_user_base_info where phone = ?";
        JsonArray values = new JsonArray();
        values.add(phone);
        JsonObject sqlObj = new JsonObject();
        sqlObj.put("method","select");
        sqlObj.put("sqlString",sql);
        sqlObj.put("values",values);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),sqlObj.toString(),message->{
           if(message.succeeded()){
               String queryStr = ((Message<Object>)message.result()).body().toString();
               logger.info("userInfo getresponse"+queryStr);
               JsonObject queryObj = new JsonObject(queryStr);
               JsonObject retObj = new JsonObject();
               if (!queryObj.getJsonArray("data").isEmpty()){
                   retObj.put(Common.USER_RET_KEY,queryObj.getJsonArray("data").getJsonObject(0));
               }else {
                   retObj.put(Common.USER_RET_KEY,new JsonObject());
               }
               logger.info("userInfo response"+retObj.toString());
               handler.reply(retObj.toString());
           }
        });
    }

    /**
     * 判断用户是否已注册
     * @param handler
     */
    public void isRegister(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String userId = paramObj.getValue("userId").toString();

        String sql = "select phone,user_name from t_user_base_info where phone=?";
        JsonArray values = new JsonArray();
        values.add(userId);
        JsonObject queryObj = new JsonObject();
        queryObj.put(Common.METHOD,Common.METHOD_SELECT);
        queryObj.put(Common.SQL_KEY,sql);
        queryObj.put(Common.VALUES_KEY,values);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),message->{
            JsonObject retJson = new JsonObject();
            if (message.succeeded()){
                JsonObject respObj = new JsonObject(message.result().body().toString());
                if (!respObj.getJsonArray("data").isEmpty()){
                    retJson.put("status",ReturnStatus.SC_OK);
                    retJson.put("isExist",true);
                    logger.info("isRegister"+retJson.toString());
                    handler.reply(retJson.toString());
                }else {
                    retJson.put("status",ReturnStatus.SC_OK);
                    retJson.put("isExist",false);
                    logger.info("isRegister"+retJson.toString());
                    handler.reply(retJson.toString());
                }
            }else {
                retJson.put("status",ReturnStatus.SC_FAIL);
                logger.info("isRegister 回调失败"+message.cause().getMessage());
            }
        });
    }
}
