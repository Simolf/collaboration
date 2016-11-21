package com.caompus.dataSourceVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;


/**
 * Created by credtitone on 2016/10/25.
 */
public class DataBaseOperationVerticle extends AbstractVerticle {

    String url  = "";
    String param = "";


    public void getContent(){
        HttpClient client = vertx.createHttpClient();
        HttpClientRequest request = client.get(80,url,param,ar->{

        });
        request.exceptionHandler(exception->{
            System.out.println(exception.getCause().toString());
        }).end();
    }




}
