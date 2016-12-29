package com.caompus.dataSourceVerticle;

import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
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
        add("login");
        add("register");

    }};

    @Override
    public void start(){
        vertx.eventBus().consumer(this.getClass().getName(),handler->{
            logger.info("====MemberOperateServiceVerticle start===");
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
            if ("login".equals(method)){
                loginMethod(handler,paramObj);
            }else if ("register".equals(method)){
                registerMethod(handler,paramObj);
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
        String phone = paramJson.containsKey("phone")?paramJson.getValue("phone").toString():"";
        String password = paramJson.containsKey("password")?paramJson.getValue("password").toString():"";

        Future queryMemberFuture = Future.future();
        Future failFuture = Future.future();

        if ("".equals(phone) || "".equals(password)){
            failFuture.complete("账号或密码为空");
            return;
        }

        String sqlString = "select * from user_base_info where phone = ? and password = ?";
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
            if (queryObj.getString("status").equals("200")){
                retJson.put("status","200");
                retJson.put("data",queryObj.getJsonArray("data").getJsonObject(0));
            }else {
                retJson.put("status","201");
                retJson.put("data","账号密码错误");
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
        String phone = paramJson.containsKey("phone")?paramJson.getValue("phone").toString():"";
        String password = paramJson.containsKey("password")?paramJson.getValue("password").toString():"";
        String userName = paramJson.containsKey("userName")?paramJson.getValue("userName").toString():"";

        Future failFuture = Future.future();
        Future registerFuture = Future.future();

        if ("".equals(phone) || "".equals(password) || "".equals(userName)){
            failFuture.complete("参数为空");
            return;
        }

        String sqlString = "insert into user_base_info(phone,user_name,password)" +
                "values(?,?,?)";
        JsonArray valueArray = new JsonArray();
        valueArray.add(phone);
        valueArray.add(userName);
        valueArray.add(password);
        JsonObject sqlObj = new JsonObject();
        sqlObj.put("method","insert");
        sqlObj.put("sqlString",sqlString);
        sqlObj.put("values",valueArray);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),sqlObj.toString(),messageAsyncResult -> {
            String result = messageAsyncResult.result().body().toString();
            System.out.println(result);
        });



        failFuture.setHandler(fail->{
            handler.fail(201,failFuture.result().toString());
            logger.error(failFuture.result().toString());
        });
    }
    public void test(){

    }
}
