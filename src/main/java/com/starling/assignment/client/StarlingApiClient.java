package com.starling.assignment.client;

import com.starling.assignment.model.Account;
import com.starling.assignment.model.Balance;
import com.starling.assignment.model.SavingsGoal;
import com.starling.assignment.model.Transaction;
import com.starling.assignment.service.BankingService;

import java.util.List;

public interface StarlingApiClient {

    List<Account> getAccounts();

    Balance getAccountBalance(String accountUid);

    List<Transaction> getTransactionFeed(String accountUid, String categoryUid);

    List<SavingsGoal> getSavingsGoals(String accountUid);

    SavingsGoal getSavingsGoal(String accountUid, String savingsGoalUid);

    String createSavingsGoal(String accountUid, BankingService.SavingsGoalRequest savingsGoalRequest);

    boolean transferMoneyToSavingsGoal(String accountUid, String savingsGoalUid, String transferUid, BankingService.SavingsGoalAmount savingGoalAmount);
}
