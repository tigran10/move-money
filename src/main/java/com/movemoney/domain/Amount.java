package com.movemoney.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Amount extends ReflectionEqualsHashCodeToString {

    @JsonProperty
    @NotNull
    private BigDecimal value;

    @JsonProperty
    @NotNull
    private Currency currency;


    private Amount(BigDecimal value,
                   Currency currency) {
        this.value = Objects.requireNonNull(value, "value");
        this.currency = Objects.requireNonNull(currency, "currency");
    }


    @JsonCreator
    public static Amount of(
            @JsonProperty("value") BigDecimal value,
            @JsonProperty("currency") Currency currency) {
        return new Amount(value, currency);
    }

    public BigDecimal getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @NotNull
    public BigDecimal displayValue() {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

}
