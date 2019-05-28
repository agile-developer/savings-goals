package com.starling.assignment.service;

import com.starling.assignment.model.Account;
import com.starling.assignment.model.Amount;
import com.starling.assignment.model.RoundUp;
import com.starling.assignment.model.SavingsGoal;
import com.starling.assignment.model.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class RoundUpServiceTest {

    @Mock
    private BankingService bankingService;

    private RoundUpService roundUpService;

    @Before
    public void beforeTest() {

        roundUpService = new RoundUpService(bankingService);
    }

    @Test
    public void transactions_within_date_range_are_returned() {

        Account account = mock(Account.class);
        List<Transaction> transactions = createTransactions();
        when(bankingService.getTransactions(account.getAccountUid(), account.getDefaultCategory())).thenReturn(transactions);

        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.of("UTC"));
        Date from = Date.from(today.minusDays(4).toInstant());
        Date to = Date.from(today.toInstant());

        List<Transaction> result = roundUpService.getTransactions(account, from, to);

        verify(bankingService, only()).getTransactions(account.getAccountUid(), account.getDefaultCategory());
        assertThat(result.size()).isEqualTo(4);
        assertThat(result).contains(transactions.get(0), transactions.get(1), transactions.get(2), transactions.get(3));
    }

    @Test
    public void invalid_date_range_returns_empty_transactions_list() {

        Account account = mock(Account.class);
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.of("UTC"));
        Date from = Date.from(today.plusDays(4).toInstant());
        Date to = Date.from(today.toInstant());

        List<Transaction> result = roundUpService.getTransactions(account, from, to);

        assertThat(result).isEmpty();
    }

    @Test
    public void only_eligible_outbound_transactions_produce_expected_round_up() {

        List<Transaction> transactions = createTransactions();

        Amount roundUpAmount = roundUpService.calculateRoundUp(transactions);

        assertThat(roundUpAmount).isNotNull();
        assertThat(roundUpAmount.getMinorUnits()).isEqualTo(158L);
    }

    @Test
    public void select_savings_goals_with_enough_target_for_round_up_amount() {

        SavingsGoal goal1 = new SavingsGoal("1", "Goal 1",
            new Amount("GBP", 10000), new Amount("GBP", 9000));
        SavingsGoal goal2 = new SavingsGoal("2", "Goal 2",
            new Amount("GBP", 20000), new Amount("GBP", 11000));
        SavingsGoal goal3 = new SavingsGoal("3", "Goal 3",
            new Amount("GBP", 30000), new Amount("GBP", 27000));
        List<SavingsGoal> savingsGoals = Arrays.asList(goal1, goal2, goal3);

        when(bankingService.getSavingsGoals(anyString())).thenReturn(savingsGoals);

        List<SavingsGoal> result = roundUpService.fetchSavingsGoalsForAmount(
            "1", new Amount("GBP", 3000));

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(goal2, goal3);
    }

    @Test
    public void transfer_round_up_to_savings_goal_invoked_correctly() {

        Account account = mock(Account.class);
        Amount roundUpAmount = new Amount("GBP", 158);
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.of("UTC"));
        Date from = Date.from(today.minusDays(4).toInstant());
        Date to = Date.from(today.toInstant());

        when(bankingService.transferMoneyToSavingsGoal(any(), anyString(), anyString(),
            any(BankingService.SavingsGoalAmount.class))).thenReturn(true);

        RoundUp roundUp = roundUpService.transferRoundUpToSavingsGoal(account, roundUpAmount, from, to, "1");

        assertThat(roundUp).isNotNull();
        assertThat(roundUp.getAmount().getMinorUnits()).isEqualTo(158L);
        assertThat(roundUp.getSavingsGoalUid()).isEqualTo("1");
        assertThat(roundUp.getStart()).isEqualTo(from);
        assertThat(roundUp.getEnd()).isEqualTo(to);
    }

    private List<Transaction> createTransactions() {

        ZonedDateTime today = Instant.now().atZone(ZoneId.of("UTC"));

        Amount amount1 = new Amount("GBP", 87);
        Transaction transaction1 = new Transaction("1", "1", amount1,
            Date.from(today.minusDays(1).toInstant()), Transaction.Direction.OUT,
            Transaction.Status.SETTLED, "MASTER_CARD");

        Amount amount2 = new Amount("GBP", 520);
        Transaction transaction2 = new Transaction("2", "1", amount2,
            Date.from(today.minusDays(2).toInstant()), Transaction.Direction.OUT,
            Transaction.Status.SETTLED, "MASTER_CARD");

        Amount amount3 = new Amount("GBP", 435);
        Transaction transaction3 = new Transaction("3", "1", amount3,
            Date.from(today.minusDays(3).toInstant()), Transaction.Direction.OUT,
            Transaction.Status.SETTLED, "DIRECT_DEBIT");

        Amount amount4 = new Amount("GBP", 2200);
        Transaction transaction4 = new Transaction("4", "1", amount4,
            Date.from(today.minusDays(4).toInstant()), Transaction.Direction.IN,
            Transaction.Status.SETTLED, "FPS_IN");

        Amount amount5 = new Amount("GBP", 1100);
        Transaction transaction5 = new Transaction("1", "1", amount5,
            Date.from(today.minusDays(5).toInstant()), Transaction.Direction.IN,
            Transaction.Status.SETTLED, "FPS_IN");

        Amount amount6 = new Amount("GBP", 158);
        Transaction transaction6 = new Transaction("6", "1", amount6,
            Date.from(today.minusDays(6).toInstant()), Transaction.Direction.OUT,
            Transaction.Status.SETTLED, "INTERNAL_TRANSFER");

        return Arrays.asList(transaction1, transaction2, transaction3, transaction4, transaction5, transaction6);
    }
}
