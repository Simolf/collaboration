package com.caompus.item;

import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2016/12/30.
 */
public class IndexVerticle extends AbstractVerticle {

    Logger logger = Logger.getLogger(IndexVerticle.class);

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(this.getClass().getName(), (Message<Object> handler) -> {
            handle(handler);
        });
    }
    private void handle(Message<Object> handler) {
        String paramString = handler.body().toString();
        JsonObject paramObj = new JsonObject(paramString);

        String method = paramObj.containsKey("method") ? paramObj.getValue("method").toString() : "";
        if ("index".equals(method)) {
            handleIndex(handler);
        }else if ("createProject".equals(method)){
            createProject(handler);
        }else if ("deleteProject".equals(method)){
            deleteProject(handler);
        }else if ("getProjectById".equals(method)){
            getProjectById(handler);
        }
    }

    /**
     * 首页（项目列表）
     * @param handler
     */
    private void handleIndex(Message<Object> handler){
        String paramString = handler.body().toString();
        JsonObject paramJson = new JsonObject(paramString);

        String userId = paramJson.containsKey("userId") ? paramJson.getValue("userId").toString() : "";

        if ("".equals(userId)) {
            JsonObject retJson = new JsonObject();
            retJson = ReturnStatus.getStatusObj(ReturnStatus.missParameterCode);
            logger.info("参数为空" + paramJson);
            handler.reply(retJson.toString());
            return;
        }

        JsonObject queryJson = new JsonObject();
        queryJson.put("method", "getItemList");
        queryJson.put("userId",userId);

        DeliveryOptions options = new DeliveryOptions().setSendTimeout(10 * 1000);

        vertx.eventBus().send(ItemVerticle.class.getName(), queryJson.toString(), options, ar -> {
            if (ar.succeeded()){
               handler.reply(ar.result().body().toString());
            }else {
                String str = "数据库连接失败" + ar.cause();
                JsonObject retJson = new JsonObject().put("status","301");
                retJson.put("data",new JsonArray());
                handler.reply(retJson.toString());
                logger.error("首页返回错误 " + str);
            }
        });


    }

    /**
     * 创建项目 projectId,projectName
     * @param handler
     */
    private void createProject(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        JsonObject retJson = new JsonObject();

        String userId = paramObj.containsKey("userId")?paramObj.getValue("userId").toString():"";
        String projectName = paramObj.containsKey("projectName")?paramObj.getValue("projectName").toString():"";

        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(projectName)){
            retJson.put("status",ReturnStatus.SC_FAIL);
            logger.error("createProject 参数为空");
            handler.reply(retJson.toString());
            return;
        }
        vertx.eventBus().send(ItemVerticle.class.getName(),paramObj.toString(),message->{
           if (message.succeeded()){
               handler.reply(message.result().body().toString());
           }else {
               retJson.put("status",ReturnStatus.SC_FAIL);
               logger.error("createProject 回调失败");
               handler.reply(retJson.toString());
           }
        });
    }

    /**
     * 删除项目
     * @param handler
     */
    private void deleteProject(Message<Object>handler){
        JsonObject retJson = new JsonObject();

        JsonObject paramObj = new JsonObject(handler.body().toString());
        String projectId = paramObj.containsKey("projectId")?paramObj.getValue("projectId").toString():"";

        if (StringUtils.isEmpty(projectId)){
            retJson.put("status",ReturnStatus.SC_FAIL);
            logger.error("deleteProject 参数为空");
            handler.reply(retJson.toString());
            return;
        }

        vertx.eventBus().send(ItemVerticle.class.getName(),paramObj.toString(),message->{
           if (message.succeeded()){
               handler.reply(message.result().body().toString());
           }else {
               retJson.put("status",ReturnStatus.SC_FAIL);
               logger.error("deleteProject 回调失败");
               handler.reply(retJson.toString());
           }
        });
    }

    /**
     * 根据项目ID获取项目信息
     * @param handler
     */
    private void getProjectById(Message<Object>handler){
        JsonObject retJson = new JsonObject();

        JsonObject paramObj = new JsonObject(handler.body().toString());
        String projectId = paramObj.containsKey("projectId")?paramObj.getValue("projectId").toString():"";

        if (StringUtils.isEmpty(projectId)){
            retJson.put("status",ReturnStatus.SC_FAIL);
            handler.reply(retJson.toString());
            return;
        }

        vertx.eventBus().send(ItemVerticle.class.getName(),paramObj.toString(),message->{
           if (message.succeeded()){
               handler.reply(message.result().body().toString());
           }else {
               retJson.put("status",ReturnStatus.SC_FAIL);
               retJson.put("project",new JsonObject());
               handler.reply(retJson.toString());
           }
        });

    }



}
