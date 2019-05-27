package com.starling.assignment.service;

import com.starling.assignment.model.Account;
import com.starling.assignment.model.Amount;
import com.starling.assignment.model.RoundUp;
import com.starling.assignment.model.Transaction;
import com.starling.assignment.model.Transaction.Direction;
import com.starling.assignment.model.Transaction.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * Service that implements criteria for transactions considered <i>eligible</i> for round-up,
 * as well as round-up calculation. This class is meant to encapsulate <i>business-logic</i>, so it could implement a
 * separate '<i>calculateRoundDown</i>' method for 'IN' transactions, for example, using a different calculation from
 * the {@link #calculateRoundUp(List)}.
 *
 * Uses an instance of {@link BankingService} to access the core banking (Starling) API.
 *
 */
public class RoundUpService {

    private final BankingService bankingService;

    private static final String INTERNAL_TRANSFER = "INTERNAL_TRANSFER";

    public RoundUpService(BankingService bankingService) {

        this.bankingService = bankingService;
    }

    /**
     * Fetch transactions for the given account and filter-in (select) transactions that fall within the {@code from}
     * and {@code to} dates.
     *
     * @param account account to retrieve transactions for
     * @param from starting date, which should be older than {@code to}, time component should be midnight
     * @param to ending date, which should be today, time component should be midnight
     * @return list of transactions falling within the given date range
     */
    public List<Transaction> getTransactions(Account account, Date from, Date to) {

        if (account == null || !to.after(from)) return new ArrayList<>();

        List<Transaction> transactions = bankingService.getTransactions(
            account.getAccountUid(), account.getDefaultCategory());
        if (transactions == null || transactions.isEmpty()) return new ArrayList<>();

        return transactions.stream().filter(transaction -> {
            Date transactionTime = transaction.getTransactionTime();
            return transactionTime.after(from) && transactionTime.before(to);
        }).collect(Collectors.toList());
    }

    /**
     * Calculate round-up amount for given transactions. This method only considers 'OUT', 'SETTLED' and
     * non-'INTERNAL_TRANSFER' transactions to calculate round-up amount.
     *
     * @param transactions list of candidate transactions to calculate round-up amount
     * @return instance of {@link RoundUp}
     */
    public Amount calculateRoundUp(List<Transaction> transactions) {

        if (transactions == null || transactions.isEmpty()) return new Amount("GBP", 0L);

        Predicate<Transaction> outgoingExternal =
            transaction -> Direction.OUT.equals(transaction.getDirection())
                && (Status.SETTLED.equals(transaction.getStatus()))
                && (!INTERNAL_TRANSFER.equals(transaction.getSource()));

        ToLongFunction<Transaction> outgoingRoundUp = transaction -> {
            long amount = transaction.getAmount().getMinorUnits();
            long remainder = amount % 100L;

            return (remainder == 0L) ? 0L : (100L - remainder);
        };

        long transactionsRoundUp = calculateRounding(transactions, outgoingExternal, outgoingRoundUp);

        return new Amount("GBP", transactionsRoundUp);
    }

    /**
     * Transfer the given {@code roundUpAmount} from {@code account} to the provided {@code savingsGoalUid}.
     *
     * @param account account to transfer money from
     * @param roundUpAmount round-up amount to transfer
     * @param from start date of transactions in round-up
     * @param to end date of transactions in round-up
     * @param savingsGoalUid uid of savings-goal to which round-up is to be transferred
     * @return boolean value indicating success (true) or failure (false) of transfer
     */
    public RoundUp transferRoundUpToSavingsGoal(Account account, Amount roundUpAmount, Date from, Date to,
                                                String savingsGoalUid) {

        if (roundUpAmount.getMinorUnits() == 0L) return null;

        BankingService.SavingsGoalAmount savingsGoalAmount = new BankingService.SavingsGoalAmount(roundUpAmount);
        boolean transferred = bankingService.transferMoneyToSavingsGoal(
            account.getAccountUid(), savingsGoalUid, UUID.randomUUID().toString(), savingsGoalAmount);

        return (transferred) ? new RoundUp(roundUpAmount, account.getAccountUid(), savingsGoalUid, from, to) : null;
    }

    private long calculateRounding(List<Transaction> transactions, Predicate<Transaction> selector,
                                  ToLongFunction<Transaction> roundUpCalculator) {

        return transactions.stream().filter(selector).mapToLong(roundUpCalculator).sum();
    }
}
