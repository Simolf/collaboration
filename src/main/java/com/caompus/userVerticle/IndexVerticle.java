package com.caompus.userVerticle;

import com.caompus.item.ItemVerticle;
import com.caompus.item.PersonalOperateServiceVerticle;
import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;
import org.omg.CORBA.Object;

import java.util.UUID;

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
        }
    }
    public void handleIndex(Message<Object> handler){
        String paramString = handler.body().toString();
        JsonObject paramJson = new JsonObject(paramString);

        String phone = paramJson.containsKey("phone") ? paramJson.getValue("phone").toString() : "";

        if ("".equals(phone) ) {
            JsonObject retJson = new JsonObject();
            retJson = ReturnStatus.getStatusObj(ReturnStatus.missParameterCode);
            logger.info("参数为空" + paramJson);
            handler.reply(retJson.toString());
            return;
        }

        JsonObject queryJson = new JsonObject();
        queryJson.put("method", "index");
        queryJson.put("param", paramJson.toString());

        String uuid = UUID.randomUUID().toString();
        Future failFuture = Future.future();

        DeliveryOptions options = new DeliveryOptions().setSendTimeout(10 * 1000);

        vertx.eventBus().send(ItemVerticle.class.getName(), queryJson.toString(), options, ar -> {
            if (ar.succeeded()){
               handler.reply(ar.result().body().toString());
            }else {
                String str = "数据库连接失败" + ar.cause();
                failFuture.fail(str);
                logger.error(uuid + " " + str);
            }
        });


    }
}
