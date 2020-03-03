package com.movemoney.service;

import com.movemoney.domain.Account;
import com.movemoney.domain.Amount;

import java.math.BigDecimal;
class Fixtures {

    public static Amount hundred = Amount.of(BigDecimal.valueOf(100.00), com.movemoney.domain.Currency.GBP);
    public static Amount fifty = Amount.of(BigDecimal.valueOf(50.00), com.movemoney.domain.Currency.GBP);
    public static Amount fifteen = Amount.of(BigDecimal.valueOf(15.00), com.movemoney.domain.Currency.GBP);
    public static Amount ten = Amount.of(BigDecimal.valueOf(10.00), com.movemoney.domain.Currency.GBP);

    public static Account.Id borisId = new Account.Id("9295f9da-eb16-4ca1-a69f-0da52e80d299");
    public static Account.Id theresaId = new Account.Id("187f199b-e226-4ba3-942c-34743f92ef16");

    public static Account boris = Account.of(
            borisId,
            "Boris",
            hundred);

    public static Account theresa = Account.of(
            theresaId,
            "Theresa",
            fifty);
}