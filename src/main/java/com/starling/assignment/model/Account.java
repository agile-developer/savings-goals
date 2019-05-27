package com.starling.assignment.model;

import java.util.Date;

public class Account {

    private final String accountUid;
    private final String defaultCategory;
    private final String currency;
    private final Date createdAt;

    public Account(String accountUid, String defaultCategory, String currency, Date createdAt) {
        this.accountUid = accountUid;
        this.defaultCategory = defaultCategory;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public String getAccountUid() {
        return accountUid;
    }

    public String getDefaultCategory() {
        return defaultCategory;
    }

    public String getCurrency() {
        return currency;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
