package com.caompus.dataSourceVerticle;

import com.caompus.util.Common;
import com.caompus.util.Key;
import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by credtitone on 2016/10/25.
 */
public class MessageVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(this.getClass().getName(),handler->{
            handle(handler);
        });
    }

    private void handle(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String method = paramObj.containsKey("method") ? paramObj.getString("method") : "";

        if ("saveMessage".equals(method)){
            saveMessage(handler);
        }else if ("realMessage".equals(method)){
            realMessage(handler);
        }
    }

    /**
     * 保存消息
     * @param handler
     */
    private void saveMessage(Message<Object> handler){
        JsonObject retJson = new JsonObject();
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String userId = paramObj.getValue("userId").toString();
        String target = paramObj.getValue("target").toString();
        String content = paramObj.getString("content");
        int type = paramObj.getInteger("type");

        String sql = "insert into t_message(sender,target,content,type)" +
                " values(?,?,?,?)";
        JsonArray values = new JsonArray();
        values.add(userId);
        values.add(target);
        values.add(content);
        values.add(type);
        JsonObject queryObj = new JsonObject();
        queryObj.put(Common.METHOD,Common.METHOD_INSERT);
        queryObj.put(Common.SQL_KEY,sql);
        queryObj.put(Common.VALUES_KEY,values);

        vertx.eventBus().send(DataBaseOperationVerticle.class.getName(),queryObj.toString(),message->{
            if (message.succeeded()){
                JsonObject respObj = new JsonObject(message.result().body().toString());
                if (respObj.getBoolean("isSuccess")){
                    retJson.put("status", ReturnStatus.SC_OK);
                    handler.reply(retJson.toString());
                }else {
                    retJson.put("status",ReturnStatus.SC_FAIL);
                    handler.reply(retJson.toString());
                }
            }else {
                retJson.put("status",ReturnStatus.SC_FAIL);
                handler.reply(retJson.toString());
            }
        });
    }

    /**
     * 实时消息
     * @param handler
     */
    private void realMessage(Message<Object> handler){
        JsonObject retJson = new JsonObject();
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String userId = paramObj.containsKey("userId")?paramObj.getValue("userId").toString():"";

        if (StringUtils.isEmpty(userId)){
            retJson.put("status",ReturnStatus.SC_FAIL);
            handler.reply(retJson.toString());
            return;
        }
        LocalMap<String,String> messageMap = vertx.sharedData().getLocalMap(Key.MESSAGE_MAP);//userId ->messageContent
        if (messageMap.keySet().contains(userId)){
            retJson.put("status",ReturnStatus.SC_OK);
            retJson.put("hasMessage",true);
            retJson.put("content",messageMap.get(userId));
            handler.reply(retJson.toString());
        }else {
            retJson.put("status",ReturnStatus.SC_OK);
            retJson.put("hasMessage",false);
            retJson.put("content","");
            handler.reply(retJson.toString());
        }
    }
}
