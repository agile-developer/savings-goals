package com.starling.assignment.model;

import java.util.Date;

public class RoundUp {

    private final Amount amount;
    private final String accountUid;
    private final String savingsGoalUid;
    private final Date start;
    private final Date end;

    public RoundUp(Amount amount, String accountUid, String savingsGoalUid, Date start, Date end) {

        this.amount = amount;
        this.accountUid = accountUid;
        this.savingsGoalUid = savingsGoalUid;
        this.start = start;
        this.end = end;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getAccountUid() {
        return accountUid;
    }

    public String getSavingsGoalUid() {
        return savingsGoalUid;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}
