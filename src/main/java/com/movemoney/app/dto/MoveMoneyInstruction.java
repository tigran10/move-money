package com.movemoney.app.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.movemoney.domain.Account;
import com.movemoney.domain.Amount;

public final class MoveMoneyInstruction {


    private final Account.Id sourceAccountId;
    private final Account.Id targetAccountId;
    private final Amount amount;

    @JsonCreator
    public MoveMoneyInstruction(
            @JsonProperty("sourceAccountId") Account.Id sourceAccountId,
            @JsonProperty("targetAccountId") Account.Id targetAccountId,
            @JsonProperty("amount") Amount amount) {
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
    }

    public Account.Id getSourceAccountId() {
        return sourceAccountId;
    }

    public Account.Id getTargetAccountId() {
        return targetAccountId;
    }

    public Amount getAmount() {
        return amount;
    }
}
