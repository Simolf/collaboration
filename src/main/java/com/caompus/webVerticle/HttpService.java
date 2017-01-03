package com.caompus.webVerticle;

import com.caompus.dataSourceVerticle.MessageVerticle;
import com.caompus.file.FileVerticle;
import com.caompus.item.IndexVerticle;
import com.caompus.task.TaskVerticle;
import com.caompus.userVerticle.LoginVerticle;
import com.caompus.userVerticle.UserInfoVerticle;
import com.caompus.util.Constants;
import com.caompus.util.ReturnStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by chenxioaxin on 2016/10/22.
 */
public class HttpService extends AbstractVerticle{

    private Logger logger = Logger.getLogger(HttpService.class.getName());

    Map map = new HashMap();
    ObjectMapper mapper = new ObjectMapper();
    String className= "";

    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();//创建服务器
        Router router = Router.router(vertx);

        //静态页面访问模板StaticHandler
        router.route("/page/*").handler(StaticHandler.create("web/page"));

        //请求体访问模板BodyHandler
        router.route().handler(BodyHandler.create());

        //文件上传
        router.post("/uploads").handler(routingContext->{
            Set<FileUpload> uploads = routingContext.fileUploads();
            String projectId = routingContext.request().params().get("projectId");
            Iterator<FileUpload> iterator = uploads.iterator();
            while (iterator.hasNext()){
                FileUpload file = iterator.next();
                upload(projectId,file,routingContext.response());
            }
        });
        //文件下载
        router.get("/download").handler(routingContext->{
            download(routingContext.request(),routingContext.response());
        });

        //接收post请求处理接口
        router.post("/collaboration/*").handler(routingContext -> {
            handle(routingContext);
        });
        //接收get请求处理接口
        router.get("/collaboration/*").handler(routingContext -> {
            handle(routingContext);
        });

        //系统监听端口  80
        server.requestHandler(router::accept).listen(Integer.parseInt(Constants.getInstance().getProperty("OuterServicePort")));

    }

    /**
     * 响应请求（除文件上传下载及静态页面）
     * @param routingContext
     */
    public void handle(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();//响应
        map.clear();

        for (Map.Entry<String,String> entry:routingContext.request().params().entries()){
            map.put(entry.getKey(),entry.getValue());
        }
        try{

            getClassName(routingContext.request());
            //标识符
            String uuid = UUID.randomUUID().toString();
            String json = "";
            map.put("uuid",uuid);
            //传递的参数
            json = mapper.writeValueAsString(map);

            logger.info("接受请求:"+uuid+" "+json);

            if (className == null || "".equals(className)){
                JsonObject failJson = ReturnStatus.getStatusObj(ReturnStatus.missParameterCode);
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
                    System.out.println(resultString);
                    response.end(resultString);
                }else {
                    JsonObject failJson = ReturnStatus.getStatusObj(ReturnStatus.innerErrorCode);
                    failJson.put("data",new JsonArray());
                    String failString = failJson.toString();
                    response.end(failString);
                    System.out.println("event bus failed to receive reply message!");
                    logger.error(uuid+"返回"+failString+" "+ar.cause().getMessage());
                }
            });

        }catch (Exception e){
            e.printStackTrace();
            JsonObject failJson = ReturnStatus.getStatusObj(ReturnStatus.innerErrorCode);
            failJson.put("data",new JsonArray());
            response.end(failJson.toString());
            logger.error(e.getMessage());
        }
    }

    /**
     * 获取处理请求类名及方法
     * @param request
     */
    private void getClassName(HttpServerRequest request){
        className = "";
        if (request.uri().contains("login")){
            map.put("method","login");
            className = LoginVerticle.class.getName();
        }else if(request.uri().contains("logout")){
            map.put("method","logout");
            className = LoginVerticle.class.getName();
        }else if (request.uri().contains("isRegister")){
            map.put("method","isRegister");
            className = LoginVerticle.class.getName();
        }else if (request.uri().contains("register")){
            map.put("method","register");
            className = LoginVerticle.class.getName();
        }else if (request.uri().contains("personInfo")){
            map.put("method","personInfo");
            className = UserInfoVerticle.class.getName();
        }else if (request.uri().contains("realMessage")){
            map.put("method","realMessage");
            className = MessageVerticle.class.getName();
        }
        else if (request.uri().contains("index")){
            map.put("method","index");
            className = IndexVerticle.class.getName();
        }else if (request.uri().contains("itemDetail")){
            map.put("method","itemDetail");
            className = TaskVerticle.class.getName();
        } else if (request.uri().contains("createTask")) {
            map.put("method","createTask");
            className = TaskVerticle.class.getName();
        }else if (request.uri().contains("getTaskDetailById")){
            map.put("method","getTaskDetailById");
            className = TaskVerticle.class.getName();
        }else if (request.uri().contains("deleteTaskById")){
            map.put("method","deleteTaskById");
            className = TaskVerticle.class.getName();
        }else if (request.uri().contains("updateTaskId")){
            map.put("method","updateTaskStatus");
            className = TaskVerticle.class.getName();
        }else if (request.uri().contains("inviteParticipant")){
            map.put("method","inviteParticipant");
            className = TaskVerticle.class.getName();
        }else if (request.uri().contains("createProject")){
            map.put("method","createProject");
            className = IndexVerticle.class.getName();
        }else if (request.uri().contains("deleteProject")){
            map.put("method","deleteProject");
            className = IndexVerticle.class.getName();
        }else if (request.uri().contains("getProjectById")){
            map.put("method","getProjectById");
            className = IndexVerticle.class.getName();
        }else if (request.uri().contains("getFileList")){
            map.put("method","getFileList");
            className = FileVerticle.class.getName();
        }else if (request.uri().contains("getMessageList")){
            map.put("method","getMessageList");
            className = MessageVerticle.class.getName();
        }else if (request.uri().contains("updateProject")){
            map.put("method","updateProject");
            className = IndexVerticle.class.getName();
        }
    }

    /**
     * 文件上传
     * @param id
     * @param file
     * @param response
     */
    private void upload(String id,FileUpload file,HttpServerResponse response){
        JsonObject retJson = new JsonObject();
        if (StringUtils.isEmpty(id)){
            retJson.put("status",ReturnStatus.SC_FAIL);
            retJson.put("data","参数为空");
            response.end(retJson.toString());
        }else {
            int projectId = Integer.parseInt(id);
            JsonObject paramObj = new JsonObject();
            paramObj.put("projectId",projectId);
            paramObj.put("fileName",file.fileName());
            paramObj.put("filePath",file.uploadedFileName());
            paramObj.put("method","savaFile");
            logger.info(paramObj.toString());
            vertx.eventBus().send(FileVerticle.class.getName(),paramObj.toString(),message->{
                if (message.succeeded()){
                    logger.info("message return "+message.result().body().toString());
                    response.end(message.result().body().toString());
                }else {
                    logger.error(message.cause().getMessage());
                    String respStr = new JsonObject().put("status",ReturnStatus.SC_FAIL).toString();
                    response.end(respStr);
                }
            });
        }
    }

    /**
     * 文件下载
     * @param request
     * @param response
     */
    private void download(HttpServerRequest request,HttpServerResponse response){
        String fileName = request.params().get("fileName");
        String filePath = request.params().get("filePath");

        if (StringUtils.isEmpty(fileName)||StringUtils.isEmpty(filePath)){
            String respStr = new JsonObject().put("status",ReturnStatus.SC_FAIL).toString();
            response.end(respStr);
            return;
        }
        response.putHeader("Content-Disposition","attachment; filename="+fileName);
        response.putHeader("ContentType","application/octet-stream");
        response.sendFile(filePath);
    }
}
