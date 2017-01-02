package com.caompus.task;

import com.caompus.dataSourceVerticle.MemberOperateServiceVerticle;
import com.caompus.dataSourceVerticle.MessageVerticle;
import com.caompus.item.ItemVerticle;
import com.caompus.util.Common;
import com.caompus.util.Key;
import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author chenxiaoxin
 * @title:
 * @date 2016/12/30
 * @className TaskVerticle
 */
public class TaskVerticle extends AbstractVerticle {
    Logger logger = Logger.getLogger(TaskVerticle.class.getName());

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
        }else if("createTask".equals(method)){
            createTask(hanlder);
        }else if ("getTaskDetailById".equals(method)){
            getTaskDetailById(hanlder);
        }else if ("deleteTaskById".equals(method)){
            deleteTaskById(hanlder);
        }else if ("updateTaskStatus".equals(method)){
            updateTaskStatus(hanlder);
        }else if ("inviteParticipant".equals(method)){
            inviteParticipant(hanlder);
        }

    }

    /**
     * 项目具体页
     *
     * @param handler
     */
    private void handleItemDetail(Message<Object> handler) {
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String userId = paramObj.containsKey("phone") ? paramObj.getValue("phone").toString() : "";
        int projectId = paramObj.containsKey("projectId") ? Integer.parseInt(paramObj.getValue("projectId").toString()) : 0;

        Future failFuture = Future.future();
        Future userInfoFuture = Future.future();
        Future itemInfoFuture = Future.future();
        Future taskInfoFuture = Future.future();

        failFuture.setHandler(ar -> {
            String data = failFuture.result().toString();
            JsonObject retJosn = new JsonObject();
            retJosn.put("status", "302");
            retJosn.put("data", data);
            handler.reply(retJosn);
        });


        if (StringUtils.isEmpty(userId) || projectId == 0) {
            failFuture.complete("参数为空");
            return;
        }
        //获取用户信息
        JsonObject userReqObj = new JsonObject().put("method", "getPersonInfo").put("phone", userId);
        vertx.eventBus().send(MemberOperateServiceVerticle.class.getName(), userReqObj.toString(), userInfoFuture.completer());

        //获取项目信息
        JsonObject itemReqObj = new JsonObject().put("method", "getItemDetail").put("projectId", projectId);
        vertx.eventBus().send(ItemVerticle.class.getName(), itemReqObj.toString(), itemInfoFuture.completer());

        //获取用户当前项目任务信息
        JsonObject taskReqObj = new JsonObject().put("method", "getTaskList").put("userId", userId).put("projectId", projectId);
        vertx.eventBus().send(TaskOperationVerticle.class.getName(), taskReqObj.toString(), taskInfoFuture.completer());

        //处理返回结果,三个请求都返回结果时进行处理
        CompositeFuture.all(userInfoFuture, itemInfoFuture, taskInfoFuture).setHandler(ar -> {
            if (userInfoFuture.succeeded() && itemInfoFuture.succeeded() && taskInfoFuture.succeeded()) {
                JsonObject retJson = new JsonObject().put("status", 200);

                JsonObject userResObj = new JsonObject(((Message<Object>) userInfoFuture.result()).body().toString());
                retJson.put("user", userResObj.getJsonObject(Common.USER_RET_KEY));

                JsonObject itemResJson = new JsonObject(((Message<Object>) itemInfoFuture.result()).body().toString());
                retJson.put("project", itemResJson.getJsonObject(Common.PROJECT_RET_KEY));

                JsonObject taskResJson = new JsonObject(((Message<Object>) taskInfoFuture.result()).body().toString());
                retJson.put("task", taskResJson.getJsonArray(Common.TASK_RET_KEY));

                logger.info("---taskIndex--:" + retJson.toString());
                handler.reply(retJson.toString());
            } else {
                JsonObject retJson = new JsonObject();
                retJson.put("status", 301);
                handler.reply(retJson.toString());
            }

        });


    }

    /**
     * 创建任务前端传phone、项目id、新建任务内容、参与者姓名与id至后台
     * @param handler
     */
    private void createTask(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        JsonObject retJson = new JsonObject();

        String userId = paramObj.containsKey("phone")?paramObj.getValue("phone").toString():"";
        int projectId = paramObj.containsKey("projectId")?Integer.parseInt(paramObj.getString("projectId")):0;
        String taskContent = paramObj.containsKey("taskContent")?paramObj.getString("taskContent"):"";
        String participantId = paramObj.containsKey("participantId")?paramObj.getString("participantId"):"";
        String participantName = paramObj.containsKey("participantName")?paramObj.getString("participantName"):"";

        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(taskContent) || StringUtils.isEmpty(participantId)
                || StringUtils.isEmpty(participantName) || projectId == 0){
            retJson.put("status",ReturnStatus.SC_FAIL);
            retJson.put("data","参数为空");
            logger.error("create task param has null");
            handler.reply(retJson.toString());
            return;
        }

        vertx.eventBus().send(TaskOperationVerticle.class.getName(),paramObj.toString(),message->{
            if (message.succeeded()){
                logger.info("create task message"+message.result().body().toString());
                JsonObject ret = new JsonObject(message.result().body().toString());
                handler.reply(ret.toString());
            }else {
                retJson.put("status", ReturnStatus.SC_FAIL);
                retJson.put("data","回调失败");
                handler.reply(retJson.toString());
            }
        });
    }

    /**
     * 获取指定ID的任务内容
     * @param handler
     */
    private void getTaskDetailById(Message<Object> handler){
        JsonObject paramObject= new JsonObject(handler.body().toString());
        JsonObject retJson = new JsonObject();

        String taskIdStr = paramObject.containsKey("taskId")?paramObject.getValue("taskId").toString():"";

        if (StringUtils.isEmpty(taskIdStr)){
            retJson.put("status",ReturnStatus.SC_FAIL);
            retJson.put("data","参数为空");
            logger.info("getTaskDetailById error"+retJson.toString());
            handler.reply(retJson.toString());
            return;
        }

        vertx.eventBus().send(TaskOperationVerticle.class.getName(),paramObject.toString(),message->{
            if (message.succeeded()){
                handler.reply(message.result().body().toString());
            }else {
                logger.error("getTaskDetailById"+message.cause().getMessage());
                retJson.put("status",ReturnStatus.SC_FAIL);
                retJson.put("task",new JsonObject());
                handler.reply(retJson.toString());
            }
        });
    }

    /**
     * 删除任务
     * @param handler
     */
    private void deleteTaskById(Message<Object> handler){
        JsonObject retJson= new JsonObject();
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String taskId = paramObj.containsKey("taskId")?paramObj.getValue("taskId").toString():"";

        if (StringUtils.isEmpty(taskId)){
            retJson.put("status",ReturnStatus.SC_FAIL);
            retJson.put("data","参数为空");
            handler.reply(retJson.toString());
            return;
        }

        vertx.eventBus().send(TaskOperationVerticle.class.getName(),paramObj.toString(),message->{
            if (message.succeeded()){
                handler.reply(message.result().body().toString());
            }else {
                retJson.put("status",ReturnStatus.SC_FAIL);
                handler.reply(retJson.toString());
            }
        });

    }

    /**
     * 更新任务状态
     * @param handler
     */
    private void updateTaskStatus(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        JsonObject retJson = new JsonObject();

        String taskId = paramObj.containsKey("taskId")?paramObj.getValue("taskId").toString():"";
        String status = paramObj.containsKey("status")?paramObj.getValue("status").toString():"";

        if (StringUtils.isEmpty(taskId) || StringUtils.isEmpty(status)){
            retJson.put("status",ReturnStatus.SC_FAIL);
            logger.error("updateTaskStatus 参数为空");
            handler.reply(retJson.toString());
            return;
        }

        vertx.eventBus().send(TaskOperationVerticle.class.getName(),paramObj.toString(),message->{
            if (message.succeeded()){
                handler.reply(message.result().body().toString());
            }else {
                retJson.put("status",ReturnStatus.SC_FAIL);
                handler.reply(retJson.toString());
            }
        });
    }

    /**
     * 邀请成员
     * @param handler
     */
    private void inviteParticipant(Message<Object>handler){
        JsonObject retJson = new JsonObject();

        JsonObject paramObj = new JsonObject(handler.body().toString());
        String projectId = paramObj.containsKey("projectId")?paramObj.getValue("projectId").toString():"";
        String target = paramObj.containsKey("target")?paramObj.getValue("target").toString():"";
        String userId = paramObj.containsKey("userId")?paramObj.getValue("userId").toString():"";
        String userName = paramObj.containsKey("userName")?paramObj.getValue("userName").toString():"";
        String projectName = paramObj.containsKey("projectName")?paramObj.getValue("projectName").toString():"";

        if (StringUtils.isEmpty(projectId) || StringUtils.isEmpty(userId) || StringUtils.isEmpty(target)
                ||StringUtils.isEmpty(userName) || StringUtils.isEmpty(projectName)){
            retJson.put("status",ReturnStatus.SC_FAIL);
            logger.info("inviteParticipant 参数为空");
            handler.reply(retJson.toString());
            return;
        }
        String content = userName+" 将您加入项目 "+projectName;
        paramObj.put("content",content);
        paramObj.put("method","saveMessage");
        LocalMap<String, String> tokenMap = vertx.sharedData().getLocalMap(Key.TOKEN_MAP);//userPhone->token
        LocalMap<String,String> messageMap = vertx.sharedData().getLocalMap(Key.MESSAGE_MAP);//userId ->messageContent
        if (tokenMap.keySet().contains(target)){
            paramObj.put("type",1);
            messageMap.put(target,content);
        }else {
            paramObj.put("type",0);
        }

        vertx.eventBus().send(MessageVerticle.class.getName(),paramObj.toString(),message->{
            if (message.succeeded()){
                handler.reply(message.result().body().toString());
            }else {
                retJson.put("status",ReturnStatus.SC_FAIL);
                handler.reply(retJson.toString());
            }
        });
    }



}
