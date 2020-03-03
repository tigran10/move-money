package com.movemoney.app.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class MoveMoneyResult {

    final private String status;

    private MoveMoneyResult(String status) {
        this.status = Objects.requireNonNull(status, "status");
    }


    @JsonCreator
    public static MoveMoneyResult of(
            @JsonProperty("status") String status
    ) {
        return new MoveMoneyResult(status);
    }


    public String getStatus() {
        return status;
    }
}