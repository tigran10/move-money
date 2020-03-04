package com.movemoney.app;


import com.movemoney.app.config.AppConfig;
import com.movemoney.app.dto.AccountData;
import com.movemoney.domain.Account;
import com.movemoney.storage.Storage;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Optional;

import static com.movemoney.app.Fixtures.*;
import static io.vertx.ext.web.handler.sockjs.impl.JsonCodec.encode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class ServerVerticleTest {

    Integer port;

    @Mock
    Storage storage;


    @BeforeAll
    public void init(Vertx vertx) throws Exception {
        MockitoAnnotations.initMocks(this);
        vertx.deployVerticle(initVerticle(vertx), new DeploymentOptions());

    }

    private ServerVerticle initVerticle(Vertx vertx) throws IOException {
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        MoveMoneyController moveMoneyController = new MoveMoneyController(vertx, storage);
        AppConfig appConfig = new AppConfig(30, 30, port);
        return new ServerVerticle(moveMoneyController, appConfig);
    }

    @Test
    public void testHealhCheck(Vertx vertx, VertxTestContext testContext) {

        //given
        WebClient webClient = WebClient.create(vertx);

        //when
        webClient.get(port, "localhost", "/healthcheck")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {

                        //then
                        assertThat(resp.statusCode()).isEqualTo(200);
                        assertThat(resp.body()).contains("ok");
                        testContext.completeNow();

                    });
                }));
    }

    @Test
    public void testGetAccountsReturnsCorrectJsonArray(Vertx vertx, VertxTestContext testContext) {

        //given
        when(storage.findAccounts()).thenReturn(List.of(boris, Fixtures.theresa));
        WebClient webClient = WebClient.create(vertx);

        //when
        webClient.get(port, "localhost", "/api/accounts")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {

                        //then
                        JSONAssert.assertEquals(encode(List.of(boris, Fixtures.theresa)),
                                resp.body(), JSONCompareMode.LENIENT);
                        assertThat(resp.statusCode()).isEqualTo(200);
                        testContext.completeNow();

                    });
                }));
    }

    @Test
    public void testGetAccountReturnOneCorrectAccount(Vertx vertx, VertxTestContext testContext) {

        //given
        when(storage.findAccount(borisId)).thenReturn(Optional.of(boris));
        WebClient webClient = WebClient.create(vertx);

        //when
        webClient.get(port, "localhost", "/api/accounts/" + borisId.asString())
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        JSONAssert.assertEquals(encode(boris), resp.body(), JSONCompareMode.LENIENT);
                        assertThat(resp.statusCode()).isEqualTo(200);
                        testContext.completeNow();

                    });
                }));
    }

    @Test
    public void testItIsPossibleToAddAccount(Vertx vertx, VertxTestContext testContext) {
        //given
        doNothing().when(storage).createAccount(boris);
        var accountData = AccountData.of(boris.getFirstName(), boris.getOngoingBalance());
        WebClient webClient = WebClient.create(vertx);

        //when
        webClient.post(port, "localhost", "/api/accounts/")
                .putHeader("content-type", "application/json")
                .as(BodyCodec.string())
                .sendJson(accountData, testContext.succeeding(resp -> {
                    testContext.verify(() -> {

                        //then
                        final Account borisReply = Json.decodeValue(resp.body(), Account.class);
                        assertThat(borisReply.getOngoingBalance()).isEqualTo(accountData.getOngoingBalance());
                        assertThat(borisReply.getFirstName()).isEqualTo(accountData.getFirstName());

                        assertThat(resp.statusCode()).isEqualTo(201);
                        testContext.completeNow();

                    });
                }));
    }

    @Test
    public void testAccountTransactionsReturnsCorrectListIfAccountHasBeenCredited(Vertx vertx, VertxTestContext testContext) {

        //given
        when(storage.findTransactions(borisId)).thenReturn(List.of(borisSendsMoneyToTheresa));
        WebClient webClient = WebClient.create(vertx);

        //when
        webClient.get(port, "localhost", "/api/accounts/" + borisId.asString() + "/transactions")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        //then
                        JSONAssert.assertEquals(encode(List.of(borisSendsMoneyToTheresa)), resp.body(), JSONCompareMode.LENIENT);
                        assertThat(resp.statusCode()).isEqualTo(200);
                        testContext.completeNow();

                    });
                }));
    }

    @Test
    public void testAccountTransactionsReturnsEmptyListIfNoTransactionsFound(Vertx vertx, VertxTestContext testContext) {

        //given
        when(storage.findTransactions(borisId)).thenReturn(List.of());
        WebClient webClient = WebClient.create(vertx);

        //when
        webClient.get(port, "localhost", "/api/accounts/" + borisId.asString() + "/transactions")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {

                        //then
                        JSONAssert.assertEquals(encode(List.of()), resp.body(), JSONCompareMode.LENIENT);
                        assertThat(resp.statusCode()).isEqualTo(200);
                        testContext.completeNow();

                    });
                }));
    }


    @Test
    public void testGetAccountReturnCorrectErrorWhenUserNotFound(Vertx vertx, VertxTestContext testContext) {

        //given
        when(storage.findAccount(borisId)).thenReturn(Optional.empty());
        WebClient webClient = WebClient.create(vertx);

        //when
        webClient.get(port, "localhost", "/api/accounts/" + borisId.asString())
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        //then
                        assertThat(resp.statusCode()).isEqualTo(404);
                        testContext.completeNow();

                    });
                }));

    }

    @Test
    public void testGetApiConvertsErrorsToJsonErrors(Vertx vertx, VertxTestContext testContext) {

        //given
        when(storage.findAccounts()).thenThrow(new NullPointerException("Error occurred in test"));
        WebClient webClient = WebClient.create(vertx);

        //when

        webClient.get(port, "localhost", "/api/accounts/")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        //then
                        JSONAssert.assertEquals("{\"status\":500,\"message\":\"Sorry, something went wrong\"}",
                                resp.body(),
                                JSONCompareMode.LENIENT);
                        assertThat(resp.statusCode()).isEqualTo(500);
                        testContext.completeNow();

                    });
                }));

    }

}