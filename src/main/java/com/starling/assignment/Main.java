package com.starling.assignment;

import com.starling.assignment.client.StarlingApiClient;
import com.starling.assignment.client.StarlingApiClientImpl;
import com.starling.assignment.model.Account;
import com.starling.assignment.model.Amount;
import com.starling.assignment.model.Balance;
import com.starling.assignment.model.RoundUp;
import com.starling.assignment.model.SavingsGoal;
import com.starling.assignment.model.Transaction;
import com.starling.assignment.service.BankingService;
import com.starling.assignment.service.RoundUpService;
import com.starling.assignment.service.StarlingBankingServiceImpl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String STARLING_BASE_URL = "https://api-sandbox.starlingbank.com";
    private static final String STARLING_API_VERSION = "/api/v2";
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static void main(String[] args) throws Exception {

        System.out.println("Starling Savings-Goals Test");
        System.out.println();

        if (args.length != 2) {
            System.err.println(
                "Usage: java -jar <path/to/executable/savings-goals.jar> <access-token> <days-from-today>");
            System.exit(1);
        }

        String accessToken = args[0];
        long daysFromToday = Long.parseLong(args[1]);
        StarlingApiClient apiClient = new StarlingApiClientImpl(STARLING_BASE_URL, STARLING_API_VERSION, accessToken);
        BankingService bankingService = new StarlingBankingServiceImpl(apiClient);
        RoundUpService roundUpService = new RoundUpService(bankingService);

        List<Account> accounts = bankingService.getAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts found for provided access token. Program will terminate.");
            System.exit(1);
        }

        Account account = accounts.get(0);
        String accountUid = account.getAccountUid();
        System.out.println("Account UID: " + accountUid);

        Balance balance = bankingService.getAccountBalance(accountUid);
        System.out.println("Account Balance: " + balance.getAmount().getDisplayValue());
        System.out.println();

        System.out.println("Calculating round-up...");
        ZonedDateTime ldToday = LocalDate.now().atStartOfDay(ZoneId.of("UTC"));
        ZonedDateTime ldWeekAgo = ldToday.minusDays(daysFromToday);
        Date from = Date.from(ldWeekAgo.toInstant());
        Date to = Date.from(ldToday.toInstant());
        System.out.println("From: " + df.format(from));
        System.out.println("To: " + df.format(to));
        List<Transaction> transactions = roundUpService.getTransactions(account, from, to);
        System.out.println("Transactions: " + transactions.size());

        if (transactions.isEmpty()) {
            System.out.println("No transactions found for the given period. Program will terminate.");
            System.exit(0);
        }

        Amount roundUpAmount = roundUpService.calculateRoundUp(transactions);
        System.out.println("Round-up amount: " + roundUpAmount.getDisplayValue());
        System.out.println();

        if (roundUpAmount.getMinorUnits() == 0) {
            System.out.println("Nothing to transfer. Program will terminate.");
            System.exit(0);
        }

        List<SavingsGoal> savingsGoals = bankingService.getSavingsGoals(accountUid);

        if (savingsGoals.isEmpty()) {
            System.out.println("No svaings-goals found for this account. Program will terminate.");
            System.exit(0);
        }

        System.out.printf("Found %d savings-goals for this account:\n", savingsGoals.size());
        for (int i = 1; i <= savingsGoals.size(); i++) {
            SavingsGoal savingsGoal = savingsGoals.get(i - 1);
            System.out.println(i + ". " + savingsGoal.getName());
            System.out.println("   Target: " + savingsGoal.getTarget().getDisplayValue());
            System.out.println("   Saved: " + savingsGoal.getTotalSaved().getDisplayValue());
        }
        System.out.println();

        System.out.print("Select a savings-goal above to transfer round-up amount, or any other value to skip: ");
        Scanner in = new Scanner(System.in);
        int choice = in.nextInt();

        if (choice < 1 || choice > savingsGoals.size()) {
            System.out.println(
                "You did not choose a savings-goal. Program will terminate without transfer of round-up amount.");
            System.exit(0);
        }

        SavingsGoal targetSavingsGoal = savingsGoals.get(choice - 1);
        System.out.println();
        System.out.printf("Transferring %s to savings-goal: %s\n",
            roundUpAmount.getDisplayValue(), targetSavingsGoal.getName());
        RoundUp roundUp = roundUpService.transferRoundUpToSavingsGoal(
            account, roundUpAmount, from, to, targetSavingsGoal.getSavingsGoalUid());

        if (roundUp == null) {
            System.err.println("Transfer of round-up amount to savings-goal failed!");
            System.exit(1);
        }

        System.out.println("Transfer of round-up amount to savings-goal succeeded!");
        TimeUnit.SECONDS.sleep(2L);
        targetSavingsGoal = bankingService.getSavingsGoal(accountUid, targetSavingsGoal.getSavingsGoalUid());
        System.out.println("Savings-Goal: " + targetSavingsGoal.getName());
        System.out.println("Target: " + targetSavingsGoal.getTarget().getDisplayValue());
        System.out.println("Saved: " + targetSavingsGoal.getTotalSaved().getDisplayValue());
    }
}
