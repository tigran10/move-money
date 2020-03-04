package com.movemoney.app;

import com.movemoney.domain.Account;
import com.movemoney.domain.Amount;
import com.movemoney.domain.BacsTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class Fixtures {

    public static Amount hundred = Amount.of(BigDecimal.valueOf(100.00), com.movemoney.domain.Currency.GBP);
    public static Amount fifty = Amount.of(BigDecimal.valueOf(50.00), com.movemoney.domain.Currency.GBP);
    public static Amount fifteen = Amount.of(BigDecimal.valueOf(15.00), com.movemoney.domain.Currency.GBP);
    public static Amount ten = Amount.of(BigDecimal.valueOf(10.00), com.movemoney.domain.Currency.GBP);

    public static Account.Id borisId = new Account.Id("9295f9da-eb16-4ca1-a69f-0da52e80d299");
    public static Account.Id theresaId = new Account.Id("187f199b-e226-4ba3-942c-34743f92ef16");
    public static Account.Id davidId = new Account.Id("fe18d59c-0a38-4f93-9210-694af2d630cd");
    public static BacsTransaction.Id borisSendsMoneyToTheresaTransactionId = new BacsTransaction.Id("0bb83b76-39a5-4491-93bc-c2af560861ca");
    public static BacsTransaction.Id theresaSendsMoneyToBorisTransactionId = new BacsTransaction.Id("fe18d59c-0a38-4f93-9210-694af2d630cd");

    public static LocalDateTime knownDate = LocalDateTime.of(2000,12, 12, 11, 11, 11);

    public static Account boris = Account.of(
            borisId,
            "Boris",
            hundred);

    public static Account theresa = Account.of(
            theresaId,
            "Theresa",
            fifty);

    public static Account david = Account.of(
            theresaId,
            "David",
            fifty);


    public static BacsTransaction borisSendsMoneyToTheresa = BacsTransaction.of(
            borisSendsMoneyToTheresaTransactionId,
            borisId,
            theresaId,
            knownDate,
            hundred
    );

    public static BacsTransaction theresaSendsMoneyToBoris = BacsTransaction.of(
            theresaSendsMoneyToBorisTransactionId,
            borisId,
            theresaId,
            knownDate,
            hundred
    );
}