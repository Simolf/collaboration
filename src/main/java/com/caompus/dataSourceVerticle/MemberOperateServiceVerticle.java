package com.caompus.dataSourceVerticle;

import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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
       add("innerQueryMember");
    }};

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(this.getClass().getName(),handler->{
           handle(handler);
        });
    }
    public void handle(Message<Object> handler){
        JsonObject retObj = ReturnStatus.getStatusObj(ReturnStatus.typeOfSuccess);
        retObj.put("data",new JsonArray());

        try {
            JsonObject paramObj = new JsonObject(handler.body().toString());
            if (paramObj.isEmpty()){
                retObj = ReturnStatus.getStatusObj(ReturnStatus.typeOfMissParameter);
                retObj.put("data",new JsonArray());
                handler.reply(retObj);
                return;
            }
            String method = paramObj.containsKey("method")?paramObj.getString("method"):"";
            //校验方法
            if (!methodName.contains(method)){
                retObj = ReturnStatus.getStatusObj(ReturnStatus.typeOfParameterError);
                retObj.put("data",new JsonArray());
                handler.reply(retObj);
                return;
            }
            if ("innerQueryMember".equals(method)){
                innerQueryMember(handler,paramObj);
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
    public void innerQueryMember(Message<Object> handler,JsonObject paramObj){
        String userName = paramObj.containsKey("userName")?paramObj.getValue("userName").toString():"";
        String password = paramObj.containsKey("password")?paramObj.getValue("password").toString():"";

//        String sqlString = "select * from user where userName = "+userName;
        StringBuffer sqlbuffer = new StringBuffer().append("select * from user where userName = '").append(userName)
                .append("' and password='").append(password).append("';");
        JsonObject sqlObj = new JsonObject();
        sqlObj.put("sqlString",sqlbuffer.toString());
        sqlObj.put("method","select");

        Future queryMemberFuture = Future.future();
        Future failFuture = Future.future();

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),sqlObj.toString(),queryMemberFuture.completer());

        queryMemberFuture.compose(futureHandler->{
            String queryResult = ((Message<Object>)queryMemberFuture.result()).body().toString();
            JsonArray retArray = new JsonArray(queryResult);
            if (retArray.size() == 0){
                failFuture.complete("帐号或密码错误");
            }else {
                JsonObject unitJson = retArray.getJsonObject(0);
                handler.reply(unitJson.toString());

            }
        },failFuture);

        failFuture.setHandler(fail->{
            if (failFuture.succeeded()){
                handler.fail(201,failFuture.result().toString());
                logger.error(failFuture.result().toString());
            }else {
                handler.fail(201,failFuture.result().toString());
                logger.error(failFuture.result().toString());
            }
        });

    }
    public void test(){

    }
}
