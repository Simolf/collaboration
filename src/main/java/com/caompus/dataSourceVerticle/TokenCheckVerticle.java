package com.caompus.dataSourceVerticle;

import com.caompus.util.Key;
import com.caompus.util.ReturnStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;

/**
 * Created by CXX on 2016/11/16.
 * token验证，每次操作之前先验证token是否失效
 */
public class TokenCheckVerticle extends AbstractVerticle{

    private static String paramMissDes = "缺少参数";
    private static String tokenUnavailableDes = "token失效,请重新登录";
    private static String successDes = "token验证成功，更新失效时间";

    @Override
    public void start() {
        vertx.eventBus().consumer(this.getClass().getName(),handler->{
           checkToken(handler);
        });
    }

    public void checkToken(Message<Object> handler){
        JsonObject retJson = new JsonObject();
        retJson.put("status", ReturnStatus.missParameterCode);
        retJson.put("data",paramMissDes);

        String token = handler.body().toString();
        LocalMap<String,Long> timeMap = vertx.sharedData().getLocalMap(Key.TIME_MAP);    //token->time
        LocalMap<String,JsonObject> permissionMap = vertx.sharedData().getLocalMap(Key.PERMISSION_MAP);//token -> 权限


        /**
         * token验证
         */
        if (token == null || "".equals(token) || !timeMap.keySet().contains(token)){
            retJson.put("status",ReturnStatus.tokenUnavailableCode);
            retJson.put("data",tokenUnavailableDes);
            handler.reply(retJson.toString());
        }

        Long time = timeMap.get(token);
        //token超时，清除MAP中相应的内容
        if (System.currentTimeMillis() > time){
            retJson.put("status",ReturnStatus.tokenUnavailableCode);
            retJson.put("data",tokenUnavailableDes);
            timeMap.remove(token);
            if (permissionMap.keySet().contains(token)){
                permissionMap.remove(token);
            }
            handler.reply(retJson.toString());
        }else {
            //token有效，更新token失效时间
            timeMap.put(token,System.currentTimeMillis() + 2*60*60*1000);
            retJson.put("status",ReturnStatus.successCode);
            retJson.put("data",successDes);
        }



    }
}
