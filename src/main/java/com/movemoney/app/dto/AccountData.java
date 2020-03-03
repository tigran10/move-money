package com.movemoney.app.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.movemoney.domain.Amount;

import java.util.Objects;

public class AccountData {

    private String firstName;
    private Amount ongoingBalance;

    private AccountData(
            String firstName,
            Amount ongoingBalance) {

        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.ongoingBalance = Objects.requireNonNull(ongoingBalance, "ongoingBalance");
    }

    @JsonCreator
    public static AccountData of(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("ongoingBalance") Amount ongoingBalance
    ) {
        return new AccountData(firstName, ongoingBalance);
    }

    public String getFirstName() {
        return firstName;
    }

    public Amount getOngoingBalance() {
        return ongoingBalance;
    }
}
