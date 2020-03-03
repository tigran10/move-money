package com.movemoney.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movemoney.app.serializer.LocalDateTimeDeserializer;
import com.movemoney.app.serializer.LocalDateTimeSerializer;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class BacsTransaction extends ReflectionEqualsHashCodeToString {


    @NotNull
    @JsonProperty("transactionId")
    private Id id;

    private Account.Id sourceAccountId;
    private Account.Id targetAccountId;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime transactionDate;
    private Amount amount;

    private BacsTransaction(Id id,
                            Account.Id sourceAccountId,
                            Account.Id targetAccountId,
                            LocalDateTime transactionDate,
                            Amount amount) {
        this.id = Objects.requireNonNull(id, "id");
        this.sourceAccountId = Objects.requireNonNull(sourceAccountId, "accountId");
        this.targetAccountId = Objects.requireNonNull(targetAccountId, "targetAccountId");
        this.transactionDate = Objects.requireNonNull(transactionDate, "transactionDate");
        this.amount = amount;
    }

    @JsonCreator
    public static BacsTransaction of(Id id,
                                     Account.Id sourceAccountId,
                                     Account.Id targetAccountId,
                                     LocalDateTime transactionDate,
                                     Amount amount) {
        return new BacsTransaction(id, sourceAccountId, targetAccountId, transactionDate, amount);
    }

    public static class Id extends IdentifierUUID<BacsTransaction> {
        public Id() {
        }

        @JsonCreator
        public Id(String stringEncodedUuid) {
            super(stringEncodedUuid);
        }

        public static Id autoGenerated() {
            return new Id(UUID.randomUUID().toString());
        }
    }


    public Id getId() {
        return id;
    }

    public Account.Id getSourceAccountId() {
        return sourceAccountId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public Amount getAmount() {
        return amount;
    }

    public Account.Id getTargetAccountId() {
        return targetAccountId;
    }
}
