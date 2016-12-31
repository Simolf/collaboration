package com.caompus.item;

import com.caompus.dataSourceVerticle.DataBaseOperationVerticle;
import com.caompus.task.TaskOperationVerticle;
import io.vertx.core.AbstractVerticle;
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
        if ("project".equals(method)){
            handleProject(handler);
        }
    }

    /**
     * 任务详情
     * @param handler
     */
    public void handleProject(Message<Object> handler){
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

        String sql1 = "select creator,creator_id as creatorId,participant::json,project_name as name,brief from t_project  where project_id in " +
                "(select * from t_project_belong where user_id = ? ) ";
        JsonObject queryObj = new JsonObject();
        queryObj.put("method","select");
        queryObj.put("sqlString",sql1);
        queryObj.put("values",userId);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(), response->{
            if (response.succeeded()) {
                JsonObject querryObj = new JsonObject(response.result().toString());
                JsonObject resObj = new JsonObject();
                if (querryObj.getString("status").equals(200)) {
                    logger.info("project_belong"+querryObj.toString());
                    resObj.put("status",200);
                    resObj.put("project",querryObj.getJsonArray("data"));
                    handler.reply(resObj.toString());
                } else {
                    JsonObject retJson = new JsonObject();
                    retJson.put("status", 301);
                    retJson.put("projecct", "数据库查询失败");
                    handler.reply(retJson.toString());
                }
            }else {
                JsonObject retJson = new JsonObject();
                retJson.put("status",301);
                retJson.put("project","数据库回调失败");
                handler.reply(retJson.toString());
            }
        });
    }
}
