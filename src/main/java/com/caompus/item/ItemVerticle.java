package com.caompus.item;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;


/**
 * @author chenxiaoxin
 * @title:
 * @date 2016/12/30
 * @className ItemVerticle
 */
public class ItemVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(this.getClass().getName(), handler -> {
            handle(handler);
        });
    }

    public void handle(Message<Object> hanlder) {
        JsonObject paramObj = new JsonObject(hanlder.body().toString());
        String method = paramObj.containsKey("method") ? paramObj.getString("method") : "";

        if ("itemDetail".equals(method)) {
            handleItemDetail(hanlder);
        }

    }

    /**
     * 项目具体页
     *
     * @param handler
     */
    public void handleItemDetail(Message<Object> handler) {
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String userId = paramObj.containsKey("phone") ? paramObj.getValue("phone").toString() : "";
        String itemId = paramObj.containsKey("itemId") ? paramObj.getValue("itemId").toString() : "";

        Future failFuture = Future.future();
        Future userInfoFuture = Future.future();
        Future itemInfoFuture = Future.future();
        Future taskInfoFuture = Future.future();

        failFuture.setHandler(ar -> {
            String data = failFuture.result().toString();
            JsonObject retJosn = new JsonObject();
            retJosn.put("status","302");
            retJosn.put("data",data);
            handler.reply(retJosn);
        });


        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(itemId)) {
            failFuture.complete("参数为空");
            return;
        }
        //获取用户信息
        JsonObject userReqObj = new JsonObject().put("method","getUserInfo").put("userId",userId);
        vertx.eventBus().send(UserOperationVerticle.class.getName(),userReqObj.toString(),userInfoFuture.completer());

        //获取项目信息
        JsonObject itemReqObj = new JsonObject().put("method","itemDetail").put("itemId",itemId);
        vertx.eventBus().send(ItemOperationVerticle.class.getName(),itemReqObj.toString(),itemInfoFuture.completer());

        //获取用户当前项目任务信息
        JsonObject taskReqObj = new JsonObject().put("method","getTaskId").put("userId",userId).put("itemId",itemId);
        vertx.eventBus().send(TaskOperationVerticle.class.getName(),taskReqObj.toString(),taskInfoFuture.completer());

        //处理返回结果,三个请求都返回结果时进行处理
        CompositeFuture.all(userInfoFuture,itemInfoFuture,taskInfoFuture).setHandler(ar->{
            if (userInfoFuture.succeeded() && itemInfoFuture.succeeded() && taskInfoFuture.succeeded()){
                JsonObject retJson = new JsonObject().put("status","200");
                JsonObject dataObj = new JsonObject();

                JsonObject userResObj = new JsonObject(((Message<Object>)userInfoFuture.result()).body().toString());
                dataObj.put("user",userResObj.getJsonObject("user"));

                JsonObject itemResJson = new JsonObject(((Message<Object>)itemInfoFuture.result()).body().toString());
                dataObj.put("user",userResObj.getJsonObject("userInfo"));

            }


            JsonObject userResObj = new JsonObject(((Message<Object>)userInfoFuture).body().toString());
        });



    }

}
