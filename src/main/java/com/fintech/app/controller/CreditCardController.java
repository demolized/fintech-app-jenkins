package com.fintech.app.controller;

import com.fintech.app.entity.CreditCard;
import com.fintech.app.repository.CreditCardRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cards")
public class CreditCardController {

    private final CreditCardRepository cardRepo;

    public CreditCardController(CreditCardRepository cardRepo) {
        this.cardRepo = cardRepo;
    }

    // Show credit card creation form (GET /cards/new)
    @GetMapping("/new")
    public String showAddCardForm(Model model) {
        model.addAttribute("card", new CreditCard());
        return "credit-card"; // maps to templates/credit-card.html
    }

    // Show card list and creation form (GET /cards)
    @GetMapping
    public String showCardForm(Model model) {
        model.addAttribute("card", new CreditCard());
        model.addAttribute("cards", cardRepo.findAll());
        return "credit-card"; // updated to match the actual template name
    }

    // Save new card (POST /cards)
    @PostMapping
    public String addCard(@ModelAttribute CreditCard card) {
        if (card != null) {
            cardRepo.save(card);
        }
        return "redirect:/cards";
    }
}
