package com.caompus.file;

import com.caompus.dataSourceVerticle.DataBaseOperationVerticle;
import com.caompus.util.Common;
import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;

import java.util.Collection;

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
        JsonObject retJson = new JsonObject();

        JsonObject paramObj = new JsonObject(handler.body().toString());
        int projectId = paramObj.getInteger("projectId");
        String fileName = paramObj.getString("fileName");
        String filePath = paramObj.getString("filePath");

        String sql = "insert into t_file(project_id,file_name,file_path) " +
                "values(?,?,?)";
        JsonArray values = new JsonArray();
        values.add(projectId);
        values.add(fileName);
        values.add(filePath);
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
}
