package com.movemoney;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.intapp.vertx.guice.GuiceVerticleFactory;
import com.intapp.vertx.guice.GuiceVertxDeploymentManager;
import com.intapp.vertx.guice.VertxModule;
import com.movemoney.app.MoveMoneyWorker;
import com.movemoney.app.ServerVerticle;
import com.movemoney.app.ServiceBinder;
import com.movemoney.app.ServiceLauncher;
import com.movemoney.app.config.AppConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import java.util.logging.Logger;


public class Main {
    private final static Logger LOGGER = Logger.getLogger("Main");

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();
        Injector injector = Guice.createInjector(
                new VertxModule(vertx),
                new ServiceBinder());

        GuiceVerticleFactory guiceVerticleFactory = new GuiceVerticleFactory(injector);
        vertx.registerVerticleFactory(guiceVerticleFactory);

        GuiceVertxDeploymentManager deploymentManager = new GuiceVertxDeploymentManager(vertx);
        deploymentManager.deployVerticle(ServiceLauncher.class);

        AppConfig appConfig = injector.getInstance(AppConfig.class);

        DeploymentOptions serverOpts = new DeploymentOptions()
                .setWorkerPoolSize(appConfig.getServerThreads());

        DeploymentOptions workerOpts = new DeploymentOptions()
                .setWorker(true)
                .setWorkerPoolSize(appConfig.getWorkerThreads());

        deploymentManager.deployVerticle(ServerVerticle.class, serverOpts);
        deploymentManager.deployVerticle(MoveMoneyWorker.class, workerOpts);
    }
}