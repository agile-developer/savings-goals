package com.starling.assignment.model;

import java.util.Date;

public class Transaction {

    public enum Direction {
        IN("IN"), OUT("OUT");

        public final String direction;

        Direction(String direction) {
            this.direction = direction;
        }
    }

    public enum Status {
        SETTLED("SETTLED"), PENDING("PENDING"), DECLINED("DECLINED");

        public final String status;

        Status(String status) {
            this.status = status;
        }
    }

    private final String feedItemUid;
    private final String categoryUid;
    private final Amount amount;
    private final Date transactionTime;
    private final Direction direction;
    private final Status status;
    private final String source;

    public Transaction(String feedItemUid, String categoryUid, Amount amount, Date transactionTime, Direction direction, Status status, String source) {
        this.feedItemUid = feedItemUid;
        this.categoryUid = categoryUid;
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.direction = direction;
        this.status = status;
        this.source = source;
    }

    public String getFeedItemUid() {
        return feedItemUid;
    }

    public String getCategoryUid() {
        return categoryUid;
    }

    public Amount getAmount() {
        return amount;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public Direction getDirection() {
        return direction;
    }

    public Status getStatus() {
        return status;
    }

    public String getSource() {
        return source;
    }
}
