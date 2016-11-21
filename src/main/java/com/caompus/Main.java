package com.caompus;

import com.caompus.userVerticle.LoginVerticle;
import com.caompus.webVerticle.HttpService;
import io.vertx.core.Vertx;

/**
 * Created by credtitone on 2016/10/22.
 */
public class Main {
    public static void main(String[] args) {
        Vertx vertx  = Vertx.vertx();
        vertx.deployVerticle(HttpService.class.getName());
        vertx.deployVerticle(LoginVerticle.class.getName());
    }
}
