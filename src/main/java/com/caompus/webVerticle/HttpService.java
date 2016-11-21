package com.caompus.webVerticle;

import com.caompus.userVerticle.LoginVerticle;
import com.caompus.util.Constants;
import com.caompus.util.ReturnStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by credtitone on 2016/10/22.
 */
public class HttpService extends AbstractVerticle{

    Logger logger = Logger.getLogger(HttpService.class.getName());

    Map map = new HashMap();
    ObjectMapper mapper = new ObjectMapper();
    String className= "";

    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();//创建服务器
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        //接收post请求处理接口
        router.post().handler(routingContext -> {
            handle(routingContext);
        });
        //接收get请求处理接口
        router.get().handler(routingContext -> {
            handle(routingContext);
        });

        //系统监听端口  80
        server.requestHandler(router::accept).listen(Integer.parseInt(Constants.getInstance().getProperty("OuterServicePort")));

    }

    public void handle(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();//响应
        map.clear();

        for (Map.Entry<String,String> entry:routingContext.request().params().entries()){
            map.put(entry.getKey(),entry.getValue());
        }
        try{

            getClassName(routingContext.request());
            //标识符
            String uuid = UUID.randomUUID().toString()+"_"+className;
            String json = "";
            map.put("uuid",uuid);
            //传递的参数
            json = mapper.writeValueAsString(map);

            logger.info("接受请求:"+uuid+" "+json);

            if (className == null || "".equals(className)){
                JsonObject failJson = ReturnStatus.getStatusObj(ReturnStatus.typeOfMissParameter);
                failJson.put("data",new JsonArray());
                logger.info(uuid+"返回:"+failJson);
                response.end(failJson.toString());
                return;
            }
            //超时时间
            DeliveryOptions options = new DeliveryOptions().setSendTimeout(10*1000);
            vertx.eventBus().send(className,json,options,ar->{
                if (ar.succeeded()){
                    String resultString = ar.result().body().toString();
                    logger.info(uuid+"返回:"+resultString);
                    response.end(resultString);
                }else {
                    JsonObject failJson = ReturnStatus.getStatusObj(ReturnStatus.typeOfInnerError);
                    failJson.put("data",new JsonArray());
                    String failString = failJson.toString();
                    response.end(failString);
                    System.out.println("event bus failed to receive reply message!");
                    logger.error(uuid+"返回"+failString+" "+ar.cause().getMessage());
                }
            });

        }catch (Exception e){
            e.printStackTrace();
            JsonObject failJson = ReturnStatus.getStatusObj(ReturnStatus.typeOfInnerError);
            failJson.put("data",new JsonArray());
            response.end(failJson.toString());
            logger.error(e.getMessage());
        }
    }
    public void getClassName(HttpServerRequest request){
        className = "";
//        String requestIP = request.remoteAddress().host();   //获取发送请求的IP地址,用于消息推送
        if (request.uri().contains("login")){
            map.put("method","login");
            className = LoginVerticle.class.getName();
        }
        else if(request.uri().contains("logout")){
            map.put("method","logout");
            className = LoginVerticle.class.getName();
        }else if (request.uri().contains("register")){
            map.put("method","register");
            className = LoginVerticle.class.getName();
        }
    }
}
