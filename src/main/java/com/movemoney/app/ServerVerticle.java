package com.movemoney.app;

import com.google.inject.Inject;
import com.movemoney.app.config.API;
import com.movemoney.app.config.AppConfig;
import com.movemoney.app.config.GlobalHandlers;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerVerticle extends AbstractVerticle {

    private final static Logger LOGGER = Logger.getLogger("ServerVerticle");

    private final MoveMoneyController controller;

    private final AppConfig appConfig;

    @Inject
    public ServerVerticle(MoveMoneyController controller, AppConfig appConfig) {
        this.controller = controller;
        this.appConfig = appConfig;
    }

    @Override
    public void start(Future<Void> future) {

        int PORT = appConfig.getPort();

        Router movemoneyRouter = controller.getRouter();

        Router mainRouter = Router.router(vertx);
        mainRouter.route().consumes("application/json");
        mainRouter.route().produces("application/json");

        Set<String> allowHeaders = getAllowedHeaders();
        Set<HttpMethod> allowMethods = getAllowedMethods();

        mainRouter.route().handler(BodyHandler.create());
        mainRouter.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods));

        mainRouter.mountSubRouter(API.MOVEMONEY_API, movemoneyRouter);


        mainRouter.get(API.LB_CHECK).handler(GlobalHandlers::lbCheck);
        mainRouter.route().failureHandler(GlobalHandlers::error);

        // Create the http server and pass it the router
        vertx.createHttpServer()
                .requestHandler(mainRouter)
                .listen(PORT, res -> {
                    if (res.succeeded()) {
                        LOGGER.log(Level.INFO, "Server listening on port " + PORT);
                        future.complete();
                    } else {
                        LOGGER.log(Level.SEVERE, "Failed to launch server");
                        future.fail(res.cause());
                    }
                });
    }

    private Set<String> getAllowedHeaders() {
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        return allowHeaders;
    }

    private Set<HttpMethod> getAllowedMethods() {
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);
        return allowMethods;
    }
}