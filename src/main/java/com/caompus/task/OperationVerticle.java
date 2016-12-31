package com.caompus.task;

import com.caompus.dataSourceVerticle.MemberOperateServiceVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * @author chenxiaoxin
 * @title:
 * @date 2016/12/30
 * @className OperationVerticle
 */
public class OperationVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(this.getClass().getName(),handler->{
           handle(handler);
        });
    }

    public void handle(Message<Object> handler){
        String paramStr = handler.body().toString();
        JsonObject paramObj = new JsonObject(paramStr);

        String method = paramObj.containsKey("method")?paramObj.getValue("method").toString():"";

        if ("itemDetail".equals(method)){
            getItemDetail(handler);
        }
    }

    /**
     * 获取项目信息
     * @param handler
     */
    public void getItemDetail(Message<Object> handler){
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String itemId = paramObj.getString("itemId");

//        String sql = "select "
    }
}
