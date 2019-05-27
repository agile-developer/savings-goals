package com.starling.assignment.service;

import com.starling.assignment.model.Account;
import com.starling.assignment.model.Amount;
import com.starling.assignment.model.Balance;
import com.starling.assignment.model.SavingsGoal;
import com.starling.assignment.model.Transaction;

import java.util.List;

public interface BankingService {

    List<Account> getAccounts();

    Balance getAccountBalance(String accountUid);

    List<Transaction> getTransactions(String accountUid, String categoryUid);

    List<SavingsGoal> getSavingsGoals(String accountUid);

    SavingsGoal getSavingsGoal(String accountUid, String savingsGoalUid);

    String createSavingsGoal(String accountUid, SavingsGoalRequest savingsGoalRequest);

    boolean transferMoneyToSavingsGoal(String accountUid, String savingsGoalUid, String transferUid, SavingsGoalAmount savingGoalsAmount);

    class SavingsGoalRequest {
        private final String name;
        private final String currency;
        private final Amount target;

        public SavingsGoalRequest(String name, String currency, Amount target) {
            this.name = name;
            this.currency = currency;
            this.target = target;
        }

        public String getName() {
            return name;
        }

        public String getCurrency() {
            return currency;
        }

        public Amount getTarget() {
            return target;
        }
    }

    class SavingsGoalAmount {

        private final Amount amount;

        public SavingsGoalAmount(Amount amount) {
            this.amount = amount;
        }

        public Amount getAmount() {
            return amount;
        }
    }
}
