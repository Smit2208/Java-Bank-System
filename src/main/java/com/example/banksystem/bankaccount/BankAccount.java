package com.example.banksystem.bankaccount;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BankAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    private long accountNumber;
    private String password;
    private int balance;
    private String accountType;
    private List<String> transactions = new ArrayList<>();

    public BankAccount(long accountNumber, String password, int initialFunds, String accountType) {
        this.accountNumber = accountNumber;
        this.password = password;
        this.balance = initialFunds;
        this.accountType = accountType;
        transactions.add("Account opened | Balance: $" + balance + " | Type: " + accountType);
    }

    public static BankAccount createNewAccount(String password, int initialFunds, String accountType) throws IOException {
        long acct = generateAccountNumber();
        BankAccount account = new BankAccount(acct, password, initialFunds, accountType);
        account.save();
        return account;
    }

    public static BankAccount loadAccount(long accountNumber) throws IOException, ClassNotFoundException {
        File file = dataFile(accountNumber);
        if (!file.exists()) {
            throw new FileNotFoundException("Account file not found for account " + accountNumber);
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object o = in.readObject();
            return (BankAccount) o;
        }
    }

    public boolean checkPassword(String pw) {
        return password != null && password.equals(pw);
    }

    public synchronized int deposit(int amount) throws BankAccountException, IOException {
        if (amount <= 0) throw new BankAccountException("Deposit amount must be greater than zero.");
        balance += amount;
        transactions.add("Deposit: $" + amount + " | Balance: $" + balance);
        save();
        return balance;
    }

    public synchronized int withdraw(int amount) throws BankAccountException, IOException {
        if (amount <= 0) throw new BankAccountException("Withdraw amount must be greater than zero.");
        if (balance < amount) throw new BankAccountException("Insufficient funds. Current balance: $" + balance);
        balance -= amount;
        transactions.add("Withdraw: $" + amount + " | Balance: $" + balance);
        save();
        return balance;
    }

    public int getBalance() {
        return balance;
    }

    public List<String> getTransactionHistory() {
        return new ArrayList<>(transactions);
    }

    public List<String> getDepositHistory() {
        List<String> out = new ArrayList<>();
        for (String s : transactions) if (s.startsWith("Deposit:")) out.add(s);
        return out;
    }

    public List<String> getWithdrawalHistory() {
        List<String> out = new ArrayList<>();
        for (String s : transactions) if (s.startsWith("Withdraw:")) out.add(s);
        return out;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    private static long generateAccountNumber() {
        long t = System.currentTimeMillis();
        int r = new Random().nextInt(9000) + 1000;
        return Long.parseLong(String.valueOf(t) + String.valueOf(r));
    }

    public void save() throws IOException {
        File dir = new File("Project/data");
        if (!dir.exists()) dir.mkdirs();
        File file = dataFile(accountNumber);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(this);
        }
    }

    private static File dataFile(long accountNumber) {
        return new File("Project/data/account_" + accountNumber + ".dat");
    }
}
