package com.fintech.app.service;

import com.fintech.app.entity.Expense;
import com.fintech.app.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository repo;

    public ExpenseService(ExpenseRepository repo) {
        this.repo = repo;
    }

    // 📅 Expenses for current month (all cards)
    public List<Expense> findMonthlyExpenses() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        return repo.findByDateBetween(start, end);
    }

    // 💳 Expenses for a specific card this month
    public List<Expense> findExpensesByCardIdThisMonth(Long cardId) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        return repo.findByCardIdAndDateBetween(cardId, start, end);
    }

    // 💰 Total spent for a specific card this month
    public double calculateTotalForCardThisMonth(Long cardId) {
        return findExpensesByCardIdThisMonth(cardId).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public Expense save(Expense expense) {
        if (expense != null) {
            return repo.save(expense);
        }
        return null;
    }
}
