package com.movemoney.app;

import com.movemoney.app.config.AppConfig;
import com.movemoney.app.dto.AccountData;
import com.movemoney.domain.Account;
import com.movemoney.service.TransactionManager;
import com.movemoney.storage.Storage;
import io.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Optional;

import static com.movemoney.app.Fixtures.*;
import static com.movemoney.app.ServerVerticleTest.ThrowingHandler.unchecked;
import static io.vavr.control.Try.run;
import static io.vertx.core.json.Json.encode;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class ServerVerticleTest {

    private static Vertx vertx;
    private Integer port;

    @Mock
    Storage storage;

    @Mock
    TransactionManager transactionManager;

    MoveMoneyController moveMoneyController;

    ServerVerticle serverVerticle;

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = Integer.getInteger("http.port", 8080);
    }

    @AfterClass
    public static void unconfigureRestAssured() {
        RestAssured.reset();
    }

    @Before
    public void init(TestContext context) throws IOException, InterruptedException {
        MockitoAnnotations.initMocks(this);
        vertx = Vertx.vertx();

        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        moveMoneyController = new MoveMoneyController(vertx, storage);
        AppConfig appConfig = new AppConfig(30, 30, port);
        serverVerticle = new ServerVerticle(moveMoneyController, appConfig);
        DeploymentOptions serverOpts = new DeploymentOptions().setWorkerPoolSize(appConfig.getServerThreads());

        vertx.deployVerticle(serverVerticle, serverOpts, context.asyncAssertSuccess());

        Thread.sleep(1000);
    }

    @After
    public void resetMocks(){
        Mockito.reset(storage);
        Mockito.reset(transactionManager);
    }


    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testHealhCheck(TestContext context) {
        // This test is asynchronous, so get an async handler to inform the test when we are done.
        final Async async = context.async();

        vertx.createHttpClient().getNow(port, "localhost", "/healthcheck", response -> {
            response.handler(body -> {
                context.assertTrue(body.toString().contains("ok"));
                context.assertEquals(response.statusCode(), 200);
                async.complete();
            });
        });
    }

    @Test
    public void testGetAccountsReturnsCorrectJsonArray(TestContext context) {

        //given
        when(storage.findAccounts()).thenReturn(List.of(Fixtures.boris, Fixtures.theresa));
        final Async async = context.async();

        //when
        vertx.createHttpClient().getNow(port, "localhost", "/api/accounts", response -> {
            response.handler(unchecked(body -> {

                //then
                var check = run(() -> assertEquals(encode(List.of(Fixtures.boris, Fixtures.theresa)),
                        body.toString(), JSONCompareMode.LENIENT)).isSuccess();
                context.assertEquals(response.statusCode(), 200);
                context.assertTrue(check);
                async.complete();
            }));
        });
    }

    @Test
    public void testGetAccountReturnOneCorrectAccount(TestContext context) {

        //given
        when(storage.findAccount(borisId)).thenReturn(Optional.of(Fixtures.boris));
        final Async async = context.async();


        //when
        vertx.createHttpClient().getNow(port, "localhost", "/api/accounts/" + borisId.asString(), response -> {
            response.handler(unchecked(body -> {
                var check = run(() -> assertEquals(encode(Fixtures.boris), body.toString(), JSONCompareMode.LENIENT)).isSuccess();
                context.assertTrue(check);
                context.assertEquals(response.statusCode(), 200);
                async.complete();
            }));
        });
    }

    @Test
    public void testItIsPossibleToAddAccount(TestContext context) {
        //given
        doNothing().when(storage).createAccount(boris);
        final Async async = context.async();
        final String json = Json.encodePrettily(AccountData.of(boris.getFirstName(), boris.getOngoingBalance()));


        //when
        vertx.createHttpClient().post(port, "localhost", "/api/accounts")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", Integer.toString(json.length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        final Account borisReply = Json.decodeValue(body.toString(), Account.class);
                        context.assertEquals(boris.getFirstName(), borisReply.getFirstName());
                        context.assertEquals(boris.getOngoingBalance(), borisReply.getOngoingBalance());
                        context.assertEquals(response.statusCode(), 201);

                        async.complete();
                    });
                })
                .write(json)
                .end();
    }

    @Test
    public void testAccountTransactionsReturnsCorrectListIfAccountHasBeenCredited(TestContext context) {

        //given
        when(storage.findTransactions(borisId)).thenReturn(List.of(borisSendsMoneyToTheresa));
        final Async async = context.async();

        //when
        vertx.createHttpClient().getNow(port, "localhost", "/api/accounts/" + borisId.asString(), response -> {
            response.handler(unchecked(body -> {
                var check = run(() -> assertEquals(encode(List.of(borisSendsMoneyToTheresa)), body.toString(), JSONCompareMode.LENIENT)).isSuccess();

                context.assertTrue(check);
                context.assertEquals(response.statusCode(), 200);
                async.complete();
            }));
        });
    }

    @Test
    public void testAccountTransactionsReturnsEmptyListIfNoTransactionsFound(TestContext context) {

        //given
        when(storage.findTransactions(borisId)).thenReturn(List.of());
        final Async async = context.async();

        //when
        vertx.createHttpClient().getNow(port, "localhost", "/api/accounts/" + borisId.asString(), response -> {
            response.handler(unchecked(body -> {
                var check = run(() -> assertEquals(encode(List.of()), body.toString(), JSONCompareMode.LENIENT)).isSuccess();

                context.assertTrue(check);
                context.assertEquals(response.statusCode(), 200);
                async.complete();
            }));
        });
    }


    @Test
    public void testGetAccountReturnCorrectErrorWhenUserNotFound(TestContext context) {

        //given
        when(storage.findAccount(borisId)).thenReturn(Optional.empty());
        final Async async = context.async();

        //when
        vertx.createHttpClient().getNow(port, "localhost", "/api/accounts/" + borisId.asString(), response -> {
            context.assertEquals(response.statusCode(), 404);
            async.complete();
        });
    }

    @Test
    public void testGetApiConvertsErrorsToJsonErrors(TestContext context) {

        //given
        when(storage.findAccounts()).thenThrow(new NullPointerException("Error occurred in test"));
        final Async async = context.async();

        //when
        vertx.createHttpClient().getNow(port, "localhost", "/api/accounts/", response -> {

            response.handler(unchecked(body -> {
                        var check = run(() ->
                                //then
                                assertEquals("{\"status\":500,\"message\":\"Sorry, something went wrong\"}",
                                        body.toString(),
                                        JSONCompareMode.LENIENT)).isSuccess();

                        context.assertTrue(check);
                        context.assertEquals(response.statusCode(), 500);
                        async.complete();
                    })
            );

        });
    }


    @FunctionalInterface
    public interface ThrowingHandler<T, E extends Throwable> {
        void handle(T t) throws E;

        static <T, E extends Throwable> Handler<T> unchecked(ThrowingHandler<T, E> f) {
            return t -> {
                try {
                    f.handle(t);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }
}