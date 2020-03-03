package com.movemoney.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Currency {
    GBP("British Pound", "£", "001");

    @JsonProperty
    private String name;
    @JsonProperty
    private String symbol;
    @JsonProperty
    private String code;


    Currency(String name, String symbol, String code) {
        this.name = name;
        this.symbol = symbol;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCode() {
        return code;
    }
}
