package com.caompus.item;

import com.caompus.dataSourceVerticle.DataBaseOperationVerticle;
import com.caompus.task.TaskOperationVerticle;
import com.caompus.util.Common;
import com.caompus.util.Constants;
import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2016/12/31.
 */
public class ItemVerticle extends AbstractVerticle{
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
        if ("getItemList".equals(method)){
            handleProject(handler);
        }else if ("getProjectById".equals(method)){
            getProjectById(handler);
        }else if ("createProject".equals(method)){
            createProject(handler);
        }else if ("deleteProject".equals(method)){
            deleteProject(handler);
        }
    }

    /**
     * 当前用户项目列表
     * @param handler
     */
    private void handleProject(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String userId = paramObj.containsKey("userId")?paramObj.getValue("userId").toString():"";

        Future failFuture = Future.future();
        failFuture.setHandler(ar->{
            JsonObject retJson = new JsonObject();
            retJson.put("status",302);
            retJson.put("data","参数为空");
            handler.reply(retJson.toString());
            return;
        });

        if (StringUtils.isEmpty(userId)){
            failFuture.complete();
        }

        String sql1 = "select project_id \"projectId\",creator,creator_id \"creatorId\",participant::json,project_name \"name\",brief from t_project  " +
                "where project_id in " +
                "(select project_id from t_project_belong where user_id = ? ) ";
        JsonObject queryObj = new JsonObject();
        JsonArray values = new JsonArray();
        values.add(userId);
        queryObj.put("method","select");
        queryObj.put("sqlString",sql1);
        queryObj.put("values",values);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(), response->{
            if (response.succeeded()) {
                JsonObject querryObj = new JsonObject(response.result().body().toString());
                JsonObject resObj = new JsonObject();
                if (!querryObj.getJsonArray("data").isEmpty()) {
                    logger.info("project_belong"+querryObj.toString());
                    resObj.put("status",200);
                    resObj.put("project",querryObj.getJsonArray("data"));
                    handler.reply(resObj.toString());
                } else {
                    JsonObject retJson = new JsonObject();
                    retJson.put("status",301);
                    retJson.put("projecct", new JsonArray());
                    handler.reply(retJson.toString());
                }
            }else {
                JsonObject retJson = new JsonObject();
                retJson.put("status",301);
                retJson.put("project",new JsonArray());
                handler.reply(retJson.toString());
            }
        });
    }

    /**
     * 获取指定project_id的项目信息
     * @param handler
     */
    private void getProjectById(Message<Object> handler){
        String paramString = handler.body().toString();
        JsonObject paramObj = new JsonObject(paramString);

        int projectId = Integer.parseInt(paramObj.getValue("projectId").toString());


        String sql = "select project_id \"projectId\",creator,creator_id \"creatorId\",participant::json,project_name \"name\",brief " +
                "from t_project where project_id = ?";
        JsonObject queryObj = new JsonObject();
        JsonArray values = new JsonArray().add(projectId);
        queryObj.put("method", Common.METHOD_SELECT);
        queryObj.put(Common.SQL_KEY,sql);
        queryObj.put(Common.VALUES_KEY,values);


        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),response->{
            if (response.succeeded()){
                JsonObject respObj = new JsonObject(response.result().body().toString());
                JsonObject retJson = new JsonObject();
                if (!respObj.getJsonArray("data").isEmpty()){
                    retJson.put("status", ReturnStatus.successCode);
                    retJson.put(Common.PROJECT_RET_KEY,respObj.getJsonArray("data").getJsonObject(0));
                    logger.info("sigle project detail"+retJson.toString());
                    handler.reply(retJson.toString());
                }else {
                    handler.reply(ReturnStatus.getFailReturnObj(Common.PROJECT_RET_KEY));
                }
            }else {
                handler.reply(ReturnStatus.getFailReturnObj(Common.PROJECT_RET_KEY));
            }
        });

    }

    /**
     * 创建项目
     * @param handler
     */
    private void createProject(Message<Object> handler){
        JsonObject retJson = new JsonObject();
        retJson.put("status",ReturnStatus.SC_FAIL);
        retJson.put("project",new JsonObject());


        JsonObject paramObj = new JsonObject(handler.body().toString());
        String userId = paramObj.getValue("userId").toString();
        String projectName = paramObj.getValue("projectName").toString();

        String sql = "insert into t_project(creator_id,project_name) " +
                "values(?,?) returning project_id";
        String belongSql = "insert into t_project_belong(user_id,project_id,belong)" +
                " values(?,?,?)";

        JsonArray values = new JsonArray();
        values.add(userId);
        values.add(projectName);
        JsonObject queryObj = new JsonObject();
        queryObj.put(Common.METHOD,Common.METHOD_SELECT);
        queryObj.put(Common.SQL_KEY,sql);
        queryObj.put(Common.VALUES_KEY,values);

        Future detailFuture = Future.future();
        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),detailFuture.completer());

        detailFuture.setHandler(detailHandler->{
           if (detailFuture.succeeded()){
               String detailRes = ((Message<Object>)detailFuture.result()).body().toString();
               JsonObject detailResObj = new JsonObject(detailRes);
               if (!detailResObj.getJsonArray("data").isEmpty()){
                   int projectId = detailResObj.getJsonArray("data").getJsonObject(0).getInteger("project_id");
                   JsonArray belongValues = new JsonArray();
                   belongValues.add(userId);
                   belongValues.add(projectId);
                   belongValues.add(1);
                   JsonObject belongQueryObj = new JsonObject();
                   belongQueryObj.put(Common.SQL_KEY,belongSql);
                   belongQueryObj.put(Common.METHOD,Common.METHOD_INSERT);
                   belongQueryObj.put(Common.VALUES_KEY,belongValues);
                   vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),belongQueryObj.toString(),message->{
                       if (message.succeeded()){
                           JsonObject respObj = new JsonObject(message.result().body().toString());
                           if (respObj.getBoolean("isSuccess")){
                               logger.info("insert into t_project_belong return"+respObj.toString());
                               retJson.put("status",ReturnStatus.SC_OK);
                               retJson.put("project",new JsonObject().put("id",userId).put("name",projectName));
                               logger.info("return create project"+retJson.toString());
                               handler.reply(retJson.toString());
                           }else {
                               handler.reply(retJson.toString());
                           }
                       }else {
                           handler.reply(retJson.toString());
                       }
                   });
               }else {
                   handler.reply(retJson.toString());
               }
           }else {
               handler.reply(retJson.toString());
           }
        });
    }

    /**
     * 删除项目
     * @param handler
     */
    private void deleteProject(Message<Object> handler){
        JsonObject retJson = new JsonObject();

        JsonObject paramObj = new JsonObject(handler.body().toString());

        int projectId = Integer.parseInt(paramObj.getValue("projectId").toString());

        String sql = "delete from t_project where project_id = ?";
        String sql1 = "delete from t_project_belong where project_id = ?";
        String sql2 = "delete from t_task_detail where project_id = ?";
        JsonArray values = new JsonArray();
        values.add(projectId);
        JsonObject queryObj = new JsonObject();
        queryObj.put(Common.METHOD,Common.METHOD_DELETE);
        queryObj.put(Common.VALUES_KEY,values);

        Future future = Future.future();
        Future future1 = Future.future();
        Future future2 = Future.future();

        queryObj.put(Common.SQL_KEY,sql);
        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),future.completer());

        queryObj.put(Common.SQL_KEY,sql1);
        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),future1.completer());

        queryObj.put(Common.SQL_KEY,sql2);
        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),future2.completer());

        CompositeFuture.all(future,future1,future2).setHandler(message->{
           if (future.succeeded() && future1.succeeded()&& future2.succeeded()){
               retJson.put("status",ReturnStatus.SC_OK);
               handler.reply(retJson.toString());
           }else {
               retJson.put("status",ReturnStatus.SC_FAIL);
               handler.reply(retJson.toString());
           }
        });

    }
}
