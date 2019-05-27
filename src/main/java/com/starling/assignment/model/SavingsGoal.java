package com.starling.assignment.model;

public class SavingsGoal {

    private final String savingsGoalUid;
    private final String name;
    private final Amount target;
    private final Amount totalSaved;

    public SavingsGoal(String savingsGoalUid, String name, Amount target, Amount totalSaved) {
        this.savingsGoalUid = savingsGoalUid;
        this.name = name;
        this.target = target;
        this.totalSaved = totalSaved;
    }

    public String getSavingsGoalUid() {
        return savingsGoalUid;
    }

    public String getName() {
        return name;
    }

    public Amount getTarget() {
        return target;
    }

    public Amount getTotalSaved() {
        return totalSaved;
    }
}
