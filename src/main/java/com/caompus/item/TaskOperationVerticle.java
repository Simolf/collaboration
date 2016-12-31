package com.caompus.item;

import com.caompus.dataSourceVerticle.DataBaseOperationVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author chenxiaoxin
 * @title:
 * @date 2016/12/30
 * @className TaskOperationVerticle
 */
public class TaskOperationVerticle extends AbstractVerticle {
    Logger logger = Logger.getLogger(TaskOperationVerticle.class.getName());

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(this.getClass().getName(), handler -> {
            handle(handler);
        });
    }

    public void handle(Message<Object> handler) {
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String method = paramObj.containsKey("method") ? paramObj.getValue("method").toString() : "";
        if ("taskDetail".equals(method)){

        }
    }

    /**
     * 任务详情
     * @param handler
     */
    public void handleTaskDetail(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String userId = paramObj.containsKey("userId")?paramObj.getValue("userId").toString():"";
        String itemId = paramObj.containsKey("itemId")?paramObj.getValue("itemId").toString():"";

        Future failFuture = Future.future();
        failFuture.setHandler(ar->{
           JsonObject retJson = new JsonObject();
            retJson.put("status","302");
            retJson.put("data","参数为空");
            handler.reply(retJson.toString());
            return;
        });

        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(itemId)){
            failFuture.complete();
        }

        String sql = "select taskId,taskContent,createTime,creator,creatorId,participant::json,projectId,status " +
                "form t_task_detail where projectId = ? and userId = ?";
        JsonObject queryObj = new JsonObject();
        JsonArray values = new JsonArray().add(itemId).add(userId);
        queryObj.put("method","select");
        queryObj.put("sqlString",sql);
        queryObj.put("values",values);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(), response->{
            if (response.succeeded()) {
                JsonObject respObj = new JsonObject(response.result().toString());
                if (respObj.getString("status").equals("200")) {
                    logger.info("TaskDetail response"+respObj.toString());
                    handler.reply(respObj.toString());
                } else {
                    JsonObject retJson = new JsonObject();
                    retJson.put("status", "301");
                    retJson.put("data", "数据库查询失败");
                    handler.reply(retJson.toString());
                }
            }else {
                JsonObject retJson = new JsonObject();
                retJson.put("status","301");
                retJson.put("data","数据库回调失败");
                handler.reply(retJson.toString());
            }
        });
    }
}
