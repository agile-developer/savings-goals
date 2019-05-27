package com.starling.assignment.service;

import com.starling.assignment.client.StarlingApiClient;
import com.starling.assignment.model.Account;
import com.starling.assignment.model.Balance;
import com.starling.assignment.model.SavingsGoal;
import com.starling.assignment.model.Transaction;

import java.util.List;

public class StarlingBankingServiceImpl implements BankingService {

    private final StarlingApiClient apiClient;

    public StarlingBankingServiceImpl(StarlingApiClient apiClient) {

        this.apiClient = apiClient;
    }

    @Override
    public List<Account> getAccounts() {

        return apiClient.getAccounts();
    }

    @Override
    public Balance getAccountBalance(String accountUid) {

        return apiClient.getAccountBalance(accountUid);
    }

    @Override
    public List<Transaction> getTransactions(String accountUid, String categoryUid) {

        return apiClient.getTransactionFeed(accountUid, categoryUid);
    }

    @Override
    public List<SavingsGoal> getSavingsGoals(String accountUid) {

        return apiClient.getSavingsGoals(accountUid);
    }

    @Override
    public SavingsGoal getSavingsGoal(String accountUid, String savingsGoalUid) {

        return apiClient.getSavingsGoal(accountUid, savingsGoalUid);
    }

    @Override
    public String createSavingsGoal(String accountUid, BankingService.SavingsGoalRequest savingsGoalRequest) {

        return apiClient.createSavingsGoal(accountUid, savingsGoalRequest);
    }

    @Override
    public boolean transferMoneyToSavingsGoal(String accountUid, String savingsGoalUid, String transferUid, BankingService.SavingsGoalAmount savingGoalAmount) {

        return apiClient.transferMoneyToSavingsGoal(accountUid, savingsGoalUid, transferUid, savingGoalAmount);
    }
}
