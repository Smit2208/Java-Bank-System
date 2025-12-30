package com.example.banksystem;

import Project.BankAccount.BankAccount;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AccountInspector {
    public static void main(String[] args) {
        File dataDir = new File("Project/data");
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            System.out.println("Data directory not found: " + dataDir.getAbsolutePath());
            return;
        }

        File[] files = dataDir.listFiles((dir, name) -> name.startsWith("account_") && name.endsWith(".dat"));
        if (files == null || files.length == 0) {
            System.out.println("No account files found.");
            return;
        }

        System.out.println("Found " + files.length + " account(s):");
        System.out.println();

        for (File file : files) {
            String filename = file.getName();
            String accountNumStr = filename.replace("account_", "").replace(".dat", "");
            try {
                long accountNumber = Long.parseLong(accountNumStr);
                BankAccount account = BankAccount.loadAccount(accountNumber);
                System.out.println("Account Number: " + account.getAccountNumber());
                System.out.println("Account Type: " + account.getAccountType());
                System.out.println("Balance: $" + account.getBalance());
                System.out.println("Password: " + (account.checkPassword("") ? "[EMPTY PASSWORD]" : "[PROTECTED - Use password recovery in app]"));
                System.out.println("Transactions: " + account.getTransactionHistory().size());
                System.out.println("Recent transactions:");
                for (String trans : account.getTransactionHistory()) {
                    System.out.println("  - " + trans);
                }
                System.out.println("---");
            } catch (Exception e) {
                System.out.println("Error reading account " + accountNumStr + ": " + e.getMessage());
            }
        }
    }
}