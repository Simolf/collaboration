package com.caompus.dataSourceVerticle;

import com.caompus.util.Constants;
import io.vertx.core.AbstractVerticle;;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * Created by credtitone on 2016/10/25.
 */
public class DataBaseOperationVerticle extends AbstractVerticle {

    Logger logger = Logger.getLogger(DataBaseOperationVerticle.class.getName());
    private JsonObject config;
    private JDBCClient client;
    private JsonObject retJson;

    public void init() {
        config = new JsonObject();
        config.put("jdbcUrl", Constants.getInstance().getProperty("jdbcUrl"));
        config.put("maximumPoolSize", Constants.getInstance().getProperty("maximumPoolSize"));
        config.put("username", Constants.getInstance().getProperty("username"));
        config.put("password", Constants.getInstance().getProperty("password"));
        config.put("provider_class", Constants.getInstance().getProperty("provider_class"));
        client = JDBCClient.createShared(vertx, config);
        retJson = new JsonObject();
    }

    @Override
    public void start() throws Exception {
        init();
        vertx.eventBus().consumer(this.getClass().getName(), handler -> {
            handle(handler);
        });
    }

    private void handle(Message<Object> handler) {
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String method = paramObj.containsKey("method") ? paramObj.getValue("method").toString() : "";

        if (method.equals("select")) {
            select(handler);
        }
    }

    private void select(Message<Object> handler) {
        JsonObject paramObj = new JsonObject(handler.body().toString());
        String sql = paramObj.containsKey("sqlString") ? paramObj.getValue("sqlString").toString() : "";
        JsonArray values = paramObj.containsKey("values") ? paramObj.getJsonArray("values") : new JsonArray();
        client.getConnection(connectionHandler -> {
            if (connectionHandler.succeeded()) {
                SQLConnection connection = connectionHandler.result();
                connection.queryWithParams(sql, values, queryHandler -> {
                    if (queryHandler.succeeded()) {
                        ResultSet resultSet = queryHandler.result();
                        List<JsonObject> list = resultSet.getRows();
                        retJson.put("status", "200");
                        retJson.put("data", list);
                        logger.info(retJson.toString());
                        handler.reply(retJson.toString());
                    } else {
                        fail(handler, queryHandler.cause().getCause().toString());
                    }
                }).close();
            }else {
                fail(handler,connectionHandler.cause().getCause().toString());
            }
        });
    }
    private void fail(Message<Object> handler,String info){
        JsonObject ret = new JsonObject();
        ret.put("status","201");
        ret.put("data","操作失败");
        logger.error("数据库操作错误："+info);
        handler.reply(ret);
    }
}
