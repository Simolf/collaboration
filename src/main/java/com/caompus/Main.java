package com.caompus;

import com.caompus.dataSourceVerticle.DataBaseOperationVerticle;
import com.caompus.dataSourceVerticle.MemberOperateServiceVerticle;
import com.caompus.task.ItemOperationVerticle;
import com.caompus.task.taskVerticle;
import com.caompus.task.TaskOperationVerticle;
import com.caompus.userVerticle.LoginVerticle;
import com.caompus.userVerticle.UserInfoVerticle;
import com.caompus.webVerticle.HttpService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * Created by credtitone on 2016/10/22.
 */
public class Main {
    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name","io.vertx.core.logging.Log4jLogDelegateFactory");

        VertxOptions options = new VertxOptions();
        options.setMaxWorkerExecuteTime(2*60*1000);
        options.setWorkerPoolSize(10);

        Vertx vertx  = Vertx.vertx(options);

        DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true).setMultiThreaded(true);

        vertx.deployVerticle(HttpService.class.getName());
        vertx.deployVerticle(LoginVerticle.class.getName());
        vertx.deployVerticle(MemberOperateServiceVerticle.class.getName());
        vertx.deployVerticle(DataBaseOperationVerticle.class.getName(),deploymentOptions);
        vertx.deployVerticle(UserInfoVerticle.class.getName());

        //项目详情页
        vertx.deployVerticle(taskVerticle.class.getName());
        vertx.deployVerticle(ItemOperationVerticle.class.getName());
        vertx.deployVerticle(TaskOperationVerticle.class.getName());
    }
}
