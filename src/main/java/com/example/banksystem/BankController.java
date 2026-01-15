package com.example.banksystem;

import com.example.banksystem.bankaccount.BankAccount;
import com.example.banksystem.bankaccount.BankAccountException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
public class BankController {

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("account") != null) {
            return "redirect:/dashboard";
        }
        return "home";
    }

    @PostMapping("/login")
    public String login(@RequestParam String action, @RequestParam(required = false) Long accountNumber,
                        @RequestParam(required = false) String password, HttpSession session, Model model) {
        if ("existing".equals(action)) {
            try {
                BankAccount account = BankAccount.loadAccount(accountNumber);
                if (account.checkPassword(password)) {
                    session.setAttribute("account", account);
                    return "redirect:/dashboard";
                } else {
                    model.addAttribute("error", "Invalid password.");
                }
            } catch (Exception e) {
                model.addAttribute("error", "Account not found.");
            }
        } else if ("new".equals(action)) {
            model.addAttribute("showCreate", true);
        }
        return "home";
    }

    @PostMapping("/create")
    public String create(@RequestParam String password, @RequestParam int initialFunds,
                         @RequestParam String accountType, HttpSession session, Model model) {
        try {
            BankAccount account = BankAccount.createNewAccount(password, initialFunds, accountType);
            session.setAttribute("account", account);
            return "redirect:/dashboard";
        } catch (IOException e) {
            model.addAttribute("error", "Error creating account.");
            model.addAttribute("showCreate", true);
            return "home";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        BankAccount account = (BankAccount) session.getAttribute("account");
        if (account == null) {
            return "redirect:/";
        }
        // Calculate interest for savings accounts
        try {
            account.calculateInterest();
        } catch (IOException e) {
            // Log error, but don't show to user
        }
        model.addAttribute("account", account);
        return "dashboard";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam int amount, HttpSession session, Model model) {
        BankAccount account = (BankAccount) session.getAttribute("account");
        try {
            account.deposit(amount);
            model.addAttribute("message", "Deposit successful.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("account", account);
        return "dashboard";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam int amount, HttpSession session, Model model) {
        BankAccount account = (BankAccount) session.getAttribute("account");
        try {
            account.withdraw(amount);
            model.addAttribute("message", "Withdrawal successful.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("account", account);
        return "dashboard";
    }

    @GetMapping("/transactions")
    public String transactions(HttpSession session, Model model) {
        BankAccount account = (BankAccount) session.getAttribute("account");
        model.addAttribute("transactions", account.getTransactionHistory());
        return "transactions";
    }

    @GetMapping("/deposits")
    public String deposits(HttpSession session, Model model) {
        BankAccount account = (BankAccount) session.getAttribute("account");
        model.addAttribute("deposits", account.getDepositHistory());
        return "deposits";
    }

    @GetMapping("/withdrawals")
    public String withdrawals(HttpSession session, Model model) {
        BankAccount account = (BankAccount) session.getAttribute("account");
        model.addAttribute("withdrawals", account.getWithdrawalHistory());
        return "withdrawals";
    }

    @PostMapping("/close")
    public String close(HttpSession session, Model model) {
        BankAccount account = (BankAccount) session.getAttribute("account");
        File file = new File("Project/data/account_" + account.getAccountNumber() + ".dat");
        if (file.exists()) {
            file.delete();
        }
        session.invalidate();
        model.addAttribute("message", "Account closed. Final balance: $" + account.getBalance());
        return "home";
    }

    @PostMapping("/recover")
    public String recover(@RequestParam long accountNumber, Model model) {
        try {
            BankAccount account = BankAccount.loadAccount(accountNumber);
            // For demo purposes, we'll show the password. In production, you'd send it via email
            model.addAttribute("message", "Account found! Your password is: '" + account.getPassword() + "' (Note: This is for demo purposes only)");
        } catch (Exception e) {
            model.addAttribute("error", "Account not found.");
        }
        return "home";
    }

    @PostMapping("/payBill")
    public String payBill(@RequestParam String billType, @RequestParam int amount, HttpSession session, Model model) {
        BankAccount account = (BankAccount) session.getAttribute("account");
        try {
            account.payBill(billType, amount);
            model.addAttribute("message", "Bill payment successful.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("account", account);
        return "dashboard";
    }

    @GetMapping("/settings")
    public String settings(HttpSession session, Model model) {
        BankAccount account = (BankAccount) session.getAttribute("account");
        if (account == null) {
            return "redirect:/";
        }
        model.addAttribute("account", account);
        return "settings";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword,
                                 @RequestParam String confirmPassword, HttpSession session, Model model) {
        BankAccount account = (BankAccount) session.getAttribute("account");
        if (!account.checkPassword(currentPassword)) {
            model.addAttribute("error", "Current password is incorrect.");
        } else if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match.");
        } else if (newPassword.length() < 4) {
            model.addAttribute("error", "New password must be at least 4 characters.");
        } else {
            try {
                account.changePassword(newPassword);
                model.addAttribute("message", "Password changed successfully.");
            } catch (IOException e) {
                model.addAttribute("error", "Error changing password.");
            }
        }
        model.addAttribute("account", account);
        return "settings";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
