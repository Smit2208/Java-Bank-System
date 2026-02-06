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

    public synchronized int transfer(long toAccountNumber, int amount) throws BankAccountException, IOException, ClassNotFoundException {
        if (amount <= 0) throw new BankAccountException("Transfer amount must be greater than zero.");
        if (balance < amount) throw new BankAccountException("Insufficient funds. Current balance: $" + balance);
        if (toAccountNumber == accountNumber) throw new BankAccountException("Cannot transfer to the same account.");

        BankAccount toAccount = loadAccount(toAccountNumber);
        balance -= amount;
        toAccount.balance += amount;

        transactions.add("Transfer out: $" + amount + " to " + toAccountNumber + " | Balance: $" + balance);
        toAccount.transactions.add("Transfer in: $" + amount + " from " + accountNumber + " | Balance: $" + toAccount.balance);

        save();
        toAccount.save();
        return balance;
    }

    public synchronized void calculateInterest() throws IOException {
        if ("Saving".equals(accountType) && balance > 0) {
            double interestRate = 0.02; // 2% annual interest
            int interest = (int) (balance * interestRate / 12); // Monthly interest
            if (interest > 0) {
                balance += interest;
                transactions.add("Interest: $" + interest + " | Balance: $" + balance);
                save();
            }
        }
    }

    public String getPassword() {
        return password;
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

    public void changePassword(String newPassword) throws IOException {
        this.password = newPassword;
        transactions.add("Password changed");
        save();
    }

    public synchronized int payBill(String billType, int amount) throws BankAccountException, IOException {
        if (amount <= 0) throw new BankAccountException("Bill amount must be greater than zero.");
        if (balance < amount) throw new BankAccountException("Insufficient funds. Current balance: $" + balance);

        balance -= amount;
        transactions.add("Bill Payment: " + billType + " - $" + amount + " | Balance: $" + balance);
        save();
        return balance;
    }

    private static long generateAccountNumber() {
        long t = System.currentTimeMillis();
        int r = new Random().nextInt(9000) + 1000;
        return Long.parseLong(String.valueOf(t) + String.valueOf(r));
    }

    public void save() throws IOException {
        String userDir = System.getProperty("user.dir");
        File dir = new File(userDir, "Project/data");
        if (!dir.exists()) dir.mkdirs();
        File file = dataFile(accountNumber);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(this);
        }
    }

    private static File dataFile(long accountNumber) {
        String userDir = System.getProperty("user.dir");
        File dir = new File(userDir, "Project/data");
        return new File(dir, "account_" + accountNumber + ".dat");
    }
}
