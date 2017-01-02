package com.caompus.task;

import com.caompus.dataSourceVerticle.DataBaseOperationVerticle;
import com.caompus.util.Common;
import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import scala.Int;

import java.text.SimpleDateFormat;
import java.util.Date;

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
        if ("getTaskList".equals(method)){
            handleTaskDetailList(handler);
        }else if ("getTaskDetailById".equals(method)){
            getTaskDetailById(handler);
        }else if ("createTask".equals(method)){
            createTask(handler);
        }else if ("deleteTaskById".equals(method)){
            deleteTaskById(handler);
        }else if ("updateTaskStatus".equals(method)){
            updateTaskStatus(handler);
        }
    }

    /**
     * 任务列表详情
     * @param handler
     */
    public void handleTaskDetailList(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String userId = paramObj.containsKey("userId")?paramObj.getValue("userId").toString():"";
        int projectId = paramObj.getInteger("projectId");

        Future failFuture = Future.future();
        failFuture.setHandler(ar->{
           JsonObject retJson = new JsonObject();
            retJson.put("status","302");
            retJson.put("data","参数为空");
            handler.reply(retJson.toString());
            return;
        });

        if (StringUtils.isEmpty(userId) || projectId==0){
            failFuture.complete();
        }

        String sql = "select task_id \"taskId\",project_id \"projectId\", create_time \"createTime\"," +
                "status,participant_id \"participantId\",participant_name \"participantName\" " +
                "from t_task_detail where project_id = ? and participant_id = ?";
        JsonObject queryObj = new JsonObject();
        JsonArray values = new JsonArray().add(projectId).add(userId);
        queryObj.put("method","select");
        queryObj.put("sqlString",sql);
        queryObj.put("values",values);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(), response->{
            if (response.succeeded()) {
                JsonObject respObj = new JsonObject(response.result().body().toString());
                if (!respObj.getJsonArray("data").isEmpty()) {
                    logger.info("TaskDetail response"+respObj.toString());
                    JsonObject retJson = new JsonObject();
                    retJson.put(Common.TASK_RET_KEY,respObj.getJsonArray("data"));
                    logger.info("==task response:=="+respObj.toString());
                    handler.reply(retJson.toString());
                } else {
                    JsonObject retJson = new JsonObject();
                    retJson.put(Common.TASK_RET_KEY,new JsonArray());
                    handler.reply(retJson.toString());
                }
            }else {
                JsonObject retJson = new JsonObject();
                retJson.put(Common.TASK_RET_KEY,new JsonArray());
                handler.reply(retJson.toString());
            }
        });
    }

    /**
     * 根据任务ID获取任务详情
     * @param hadler
     */
    public void getTaskDetailById(Message<Object> hadler){
        JsonObject paramObj = new JsonObject(hadler.body().toString());

        int taskId = Integer.parseInt(paramObj.getString("taskId"));
        String sql = "select task_id \"taskId\",project_id \"projectId\", create_time \"createTime\"," +
                "status,participant_id \"participantId\",participant_name \"participantName\" " +
                "from t_task_detail where task_id = ?";
        JsonArray values = new JsonArray().add(taskId);
        JsonObject queryObj = new JsonObject();
        queryObj.put(Common.SQL_KEY,sql);
        queryObj.put(Common.METHOD,Common.METHOD_SELECT);
        queryObj.put(Common.VALUES_KEY,values);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),message->{
            if (message.succeeded()){
                JsonObject respObj = new JsonObject(message.result().body().toString());
                if (!respObj.getJsonArray("data").isEmpty()){
                    JsonObject retJson = new JsonObject();
                    retJson.put("status",200);
                    retJson.put(Common.TASK_RET_KEY,respObj.getJsonArray("data").getJsonObject(0));
                    hadler.reply(retJson.toString());
                }else {
                    hadler.reply(ReturnStatus.getFailReturnObj(Common.TASK_RET_KEY));
                }
            }else {
                hadler.reply(ReturnStatus.getFailReturnObj(Common.TASK_RET_KEY));
            }
        });
    }

    /**
     * 创建任务
     * @param handler
     */
    public void createTask(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String userId = paramObj.getValue("phone").toString();
        int projectId =Integer.parseInt(paramObj.getString("projectId"));
        String taskContent = paramObj.getString("taskContent");
        String participantId =paramObj.getString("participantId");
        String participantName = paramObj.getString("participantName");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String sql = "insert into t_task_detail(project_id,create_time,participant_id," +
                "participant_name,task_content,status) values(?,?,?,?,?,?)";
        JsonArray values = new JsonArray();
        values.add(projectId);
        values.add(format.format(new Date()));
        values.add(participantId);
        values.add(participantName);
        values.add(taskContent);
        values.add(1);
        JsonObject queryObj = new JsonObject();
        queryObj.put(Common.METHOD,Common.METHOD_INSERT);
        queryObj.put(Common.VALUES_KEY,values);
        queryObj.put(Common.SQL_KEY,sql);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),message->{
            JsonObject retJson = new JsonObject();
           if (message.succeeded()){
               JsonObject respObj = new JsonObject(message.result().body().toString());
               if (respObj.getBoolean("isSuccess")){
                   retJson.put("status",ReturnStatus.SC_OK);
                   logger.info("create task response"+retJson.toString());
               }else {
                   logger.error("create task insert error:isSuccess:fail");
                   retJson.put("status",ReturnStatus.SC_FAIL);
               }
               handler.reply(retJson.toString());
           }else {
               retJson.put("status",ReturnStatus.SC_FAIL);
               logger.error("create task insert reply error"+message.cause().getMessage());
               handler.reply(retJson.toString());
           }
        });
    }

    /**
     * 删除任务
     * @param handler
     */
    public void deleteTaskById(Message<Object>handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        int taskId = Integer.parseInt(paramObj.getString("taskId"));

        String sql = "delete from t_task_detail where task_id = ?";
        JsonArray values = new JsonArray();
        values.add(taskId);
        JsonObject queryObj = new JsonObject();
        queryObj.put(Common.METHOD,Common.METHOD_DELETE);
        queryObj.put(Common.SQL_KEY,sql);
        queryObj.put(Common.VALUES_KEY,values);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),message->{
            JsonObject retJson = new JsonObject();
           if (message.succeeded()){
               JsonObject respObj = new JsonObject(message.result().body().toString());
               if (respObj.getBoolean("isSuccess")){
                   retJson.put("status",ReturnStatus.SC_OK);
                   handler.reply(retJson.toString());
               }else {
                   retJson.put("status",ReturnStatus.SC_FAIL);
                   logger.error("==deleteTaskById"+message.cause().getMessage());
                   handler.reply(retJson.toString());
               }
           }else {
               retJson.put("status",ReturnStatus.SC_FAIL);
               handler.reply(retJson.toString());
           }
        });
    }

    /**
     * 更新任务转改
     * @param handler
     */
    public void updateTaskStatus(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        int taskId = Integer.parseInt(paramObj.getValue("taskId").toString());
        int status = Integer.parseInt(paramObj.getValue("status").toString());

        String sql = "update t_task_detail set status = ? where task_id = ?";
        JsonArray values = new JsonArray();
        values.add(status);
        values.add(taskId);
        JsonObject queryObj = new JsonObject();
        queryObj.put(Common.METHOD,Common.METHOD_UPDATE);
        queryObj.put(Common.SQL_KEY,sql);
        queryObj.put(Common.VALUES_KEY,values);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),message->{
            JsonObject retJson = new JsonObject();
           if (message.succeeded()){
               JsonObject respObj = new JsonObject(message.result().toString());
               if (respObj.getBoolean("isSuccess")){
                   retJson.put("status",ReturnStatus.SC_OK);
               }else {
                   retJson.put("status",ReturnStatus.SC_FAIL);
               }
               handler.reply(handler);
           }else {
               retJson.put("status",ReturnStatus.SC_FAIL);
               handler.reply(retJson.toString());
           }
        });
    }
}
