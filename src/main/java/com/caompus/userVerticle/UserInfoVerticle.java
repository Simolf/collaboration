package com.caompus.userVerticle;

import com.caompus.dataSourceVerticle.MemberOperateServiceVerticle;
import com.caompus.dataSourceVerticle.TokenCheckVerticle;
import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;

/**
 * Created by CXX on 2016/12/19.
 */
public class UserInfoVerticle extends AbstractVerticle {
    Logger logger = Logger.getLogger(UserInfoVerticle.class.getName());

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(this.getClass().getName(), handler -> {
            handle(handler);
        });
    }

    private void handle(Message<Object> handler) {
        String paramString = handler.body().toString();
        JsonObject paramObj = new JsonObject(paramString);

        String token = paramObj.containsKey("phone") ? paramObj.getValue("phone").toString() : "";
        vertx.eventBus().send(TokenCheckVerticle.class.getName(), token, tokenHandler -> {
            if (tokenHandler.succeeded()) {
                JsonObject tokenResultObj = new JsonObject(tokenHandler.result().body().toString());
                if (tokenResultObj.getString("status").equals("200")){
                    //token回调成功，判断method进行相应操作
                    String method = paramObj.containsKey("method") ? paramObj.getValue("method").toString() : "";
                    if (method.equals("personInfo")) {
                        handlePersonInfo(handler);
                    }

                }else {
                    logger.error("====token失效==="+tokenResultObj.toString());
                    handler.reply(tokenResultObj.toString());
                }
            }else {
                logger.error("==tokenCheckVerticle调用失败:=="+tokenHandler.cause().toString());
                JsonObject retObj = ReturnStatus.getStatusObj("500");
                handler.reply(retObj.toString());
            }
        });


    }

    private void handlePersonInfo(Message<Object> handler) {
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String phone = paramObj.containsKey("phone")?paramObj.getValue("phone").toString():"";

        JsonObject queryJson = new JsonObject();
        queryJson.put("method","getPersonInfo");
        queryJson.put("phone",phone);
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(10 * 1000);

        vertx.eventBus().send(MemberOperateServiceVerticle.class.getName(),queryJson.toString(),options,messageAsyncResult -> {
            if (messageAsyncResult.succeeded()){
                String restString = messageAsyncResult.result().body().toString();
                JsonObject restJson = new JsonObject(restString);
                JsonObject retObj = new JsonObject();
                if (!restJson.getJsonObject("data").isEmpty()){
                    retObj.put("status","200");
                    retObj.put("data",restJson.getJsonObject("data"));
                    logger.info("==userInfo return:=="+retObj.toString());
                }else {
                    retObj.put("status","301");
                    retObj.put("data",new JsonObject());
                }
                handler.reply(retObj.toString());
            }
        });
    }
}
