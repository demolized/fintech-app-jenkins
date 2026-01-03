package com.fintech.app.controller;

import com.fintech.app.entity.CreditCard;
import com.fintech.app.entity.Expense;
import com.fintech.app.repository.CreditCardRepository;
import com.fintech.app.service.ChartService;
import com.fintech.app.service.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Controller
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CreditCardRepository cardRepo;
    private final ChartService chartService;

    public ExpenseController(ExpenseService expenseService, CreditCardRepository cardRepo, ChartService chartService) {
        this.expenseService = expenseService;
        this.cardRepo = cardRepo;
        this.chartService = chartService;
    }

    // 🏠 Landing Page
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // 📁 Expense List
    @GetMapping("/expenses")
    public String listExpenses(Model model) {
        List<Expense> expenses = expenseService.findMonthlyExpenses();
        List<CreditCard> cards = cardRepo.findAll();

        model.addAttribute("expenses", expenses);
        model.addAttribute("expense", new Expense());
        model.addAttribute("cards", cards);

        return "expenses";
    }

    // ➕ Add Expense
    @PostMapping("/expenses")
    public String addExpense(@ModelAttribute Expense expense) {
        if (expense.getCard() != null && expense.getCard().getId() != null) {
            Long cardId = expense.getCard().getId();
            if (cardId != null) {
                cardRepo.findById(cardId).ifPresent(expense::setCard);
            }
        }
        expenseService.save(expense);
        return "redirect:/expenses";
    }

    // 📊 Dashboard View
    @GetMapping("/expenses/dashboard")
    public String dashboard(@RequestParam(required = false) Long cardId, Model model) {
        List<CreditCard> cards = cardRepo.findAll();
        model.addAttribute("cards", cards);
        model.addAttribute("expense", new Expense()); // ✅ Needed for form binding

        if (cardId != null) {
            model.addAttribute("selectedCardId", cardId);
            model.addAttribute("expenses", expenseService.findExpensesByCardIdThisMonth(cardId));
            model.addAttribute("totalSpent", expenseService.calculateTotalForCardThisMonth(cardId));

            CreditCard selectedCard = cardRepo.findById(cardId).orElse(null);
            model.addAttribute("limit", selectedCard != null ? selectedCard.getCreditLimit() : 0.0);

            // 📊 Chart Data
            Map<String, Double> categoryData = chartService.getCategoryTotals(cardId);
            model.addAttribute("categoryLabels", categoryData.keySet());
            model.addAttribute("categoryData", categoryData.values());

            Map<String, Double> vendorData = chartService.getVendorTotals(cardId);
            model.addAttribute("vendorLabels", vendorData.keySet());
            model.addAttribute("vendorTotals", vendorData.values());

            Map<String, Double> dailyData = chartService.getDailySpend(cardId);
            model.addAttribute("dailyLabels", dailyData.keySet());
            model.addAttribute("dailyTotals", dailyData.values());
        } else {
            // 👇 Prevent Thymeleaf binding errors when no card selected
            model.addAttribute("expenses", List.of());
            model.addAttribute("totalSpent", 0.0);
            model.addAttribute("limit", 0.0);
            model.addAttribute("categoryLabels", List.of());
            model.addAttribute("categoryData", List.of());
            model.addAttribute("vendorLabels", List.of());
            model.addAttribute("vendorTotals", List.of());
            model.addAttribute("dailyLabels", List.of());
            model.addAttribute("dailyTotals", List.of());
        }

        return "dashboard";
    }

    // ⬇️ CSV Export
    @GetMapping("/expenses/export")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.csv");

        List<Expense> expenses = expenseService.findMonthlyExpenses();
        PrintWriter writer = response.getWriter();
        writer.println("Vendor,Amount,Date,Category,Credit Card");

        for (Expense e : expenses) {
            writer.printf("%s,%.2f,%s,%s,%s%n",
                    e.getVendor(),
                    e.getAmount(),
                    e.getDate(),
                    e.getCategory(),
                    e.getCard() != null ? e.getCard().getMaskedNumber() : "N/A");
        }

        writer.flush();
        writer.close();
    }
}
