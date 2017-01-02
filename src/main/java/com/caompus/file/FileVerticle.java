package com.caompus.file;

import com.caompus.dataSourceVerticle.DataBaseOperationVerticle;
import com.caompus.util.Common;
import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * @ClassName:FileVerticle
 * @author:chenxiaoxin
 * @date:2017/1/2
 */
public class FileVerticle extends AbstractVerticle {

    Logger logger = Logger.getLogger(FileVerticle.class.getName());
    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(this.getClass().getName(),handler->{
            handle(handler);
        });
    }

    private void handle(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String method = paramObj.containsKey("method")?paramObj.getValue("method").toString():"";
        if (method.equals("getFileList")){
            getFileList(handler);
        }else if (method.equals("saveFile")){
            saveFile(handler);
        }
    }
    /**
     * 保存文件
     * @param handler
     */
    private void saveFile(Message<Object> handler){
        JsonObject retJson = new JsonObject();

        JsonObject paramObj = new JsonObject(handler.body().toString());
        int projectId = paramObj.getInteger("projectId");
        String userName = paramObj.getString("userName");
        String fileName = paramObj.getString("fileName");
        String filePath = paramObj.getString("filePath");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = format.format(new Date());

        String sql = "insert into t_file(project_id,file_name,file_path,creator,create_time) " +
                "values(?,?,?,?,?)";
        JsonArray values = new JsonArray();
        values.add(projectId);
        values.add(fileName);
        values.add(filePath);
        values.add(userName);
        values.add(date);
        JsonObject queryObj = new JsonObject();
        queryObj.put(Common.METHOD,Common.METHOD_INSERT);
        queryObj.put(Common.SQL_KEY,sql);
        queryObj.put(Common.VALUES_KEY,values);
        logger.info(queryObj.toString());

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),message->{
           if (message.succeeded()){
               JsonObject respObj = new JsonObject(message.result().body().toString());
               if (respObj.getBoolean("isSuccess")){
                   retJson.put("status", ReturnStatus.SC_OK);
               }else {
                   retJson.put("status",ReturnStatus.SC_FAIL);
               }
               handler.reply(retJson.toString());
           }else {
               retJson.put("status",ReturnStatus.SC_FAIL);
               handler.reply(retJson.toString());
           }
        });

    }

    /**
     * 获取文件列表
     * @param handler
     */
    private void getFileList(Message<Object> handler){
        JsonObject retJson = new JsonObject();
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String projectId = paramObj.containsKey("projectId")?paramObj.getValue("projectId").toString():"";

        if (StringUtils.isEmpty(projectId)){
            retJson.put("status",ReturnStatus.SC_FAIL);
            retJson.put("data","参数为空");
            handler.reply(retJson.toString());
            return;
        }

        String sql = "select file_name \"fileName\",file_path \"filePath\",creator,create_time \"create_time\"" +
                " from t_file where project_id = ?";
        JsonArray values = new JsonArray().add(Integer.parseInt(projectId));
        JsonObject queryObj = new JsonObject();
        queryObj.put(Common.METHOD,Common.METHOD_SELECT);
        queryObj.put(Common.SQL_KEY,sql);
        queryObj.put(Common.VALUES_KEY,values);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),message->{
           if (message.succeeded()){
               JsonObject respObj = new JsonObject(message.result().body().toString());
               logger.info(respObj.toString());
               retJson.put("status",ReturnStatus.SC_OK);
               retJson.put("file",respObj.getJsonArray("data"));
               handler.reply(retJson.toString());
           }else {
               retJson.put("status",ReturnStatus.SC_FAIL);
               retJson.put("data","查询文件列表失败");
               logger.error(message.cause().getMessage());
               handler.reply(retJson.toString());
           }
        });
    }
}
