package com.expensesplitter.service;

import com.expensesplitter.model.Expense;
import com.expensesplitter.model.ExpenseCategory;
import com.expensesplitter.model.ExpenseShare;
import com.expensesplitter.model.SplitType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExpenseSplittingServiceTest {

    @Test
    public void testEqualSplitAllocatesRemainderToLastParticipant() {
        ExpenseSplittingService service = new ExpenseSplittingService();
Expense expense = Expense.builder()
                .withGroupId(1L)
                .withPaidByUserId(1L)
                .withDescription("Test Expense")
                .withAmount(new BigDecimal("100.00"))
                .withCurrency("USD")
                .withCategory(new ExpenseCategory("Test"))
                .withSplitType(SplitType.EQUAL)
                .build();
        expense.setId(1L);

        List<Long> participants = Arrays.asList(1L, 2L, 3L);
        List<ExpenseShare> shares = service.splitEqual(expense, participants);

        assertEquals(3, shares.size());
        BigDecimal total = shares.stream().map(ExpenseShare::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.00"), total);
    }

    @Test
    public void testCustomSplitMustSumToTotal() {
        ExpenseSplittingService service = new ExpenseSplittingService();
Expense expense = Expense.builder()
                .withGroupId(1L)
                .withPaidByUserId(1L)
                .withDescription("Test Expense")
                .withAmount(new BigDecimal("50.00"))
                .withCurrency("USD")
                .withCategory(new ExpenseCategory("Test"))
                .withSplitType(SplitType.CUSTOM)
                .build();
        expense.setId(1L);

        var custom = new java.util.HashMap<Long, BigDecimal>();
        custom.put(1L, new BigDecimal("25.00"));
        custom.put(2L, new BigDecimal("25.00"));

        List<ExpenseShare> shares = service.splitCustom(expense, custom);
        assertEquals(2, shares.size());
    }
}
