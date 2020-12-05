package com.movemoney.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.UUID;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public abstract class IdentifierUUID<REFERENCED_TYPE> implements Comparable<UUID> {

    private final UUID uuid;

    protected IdentifierUUID() {
        uuid = UUID.randomUUID();
    }

    @JsonCreator
    protected IdentifierUUID(String stringEncodedUuid) {
        uuid = UUID.fromString(stringEncodedUuid);
    }

    @JsonValue
    public String asString() {
        return uuid.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        IdentifierUUID other = (IdentifierUUID) obj;

        return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
    }


    @Override
    public int compareTo(UUID o) {
        return this.uuid.compareTo(o);
    }
}