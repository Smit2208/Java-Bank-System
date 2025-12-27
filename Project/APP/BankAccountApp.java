package Project.APP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import Project.BankAccount.BankAccount;
import Project.BankAccount.BankAccountException;

public class BankAccountApp {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("***********************************");
        System.out.println("******** Welcome to ACME Bank ******");
        System.out.println("***********************************");

        BankAccount account = null;
        while (account == null) {
            System.out.print("Do you have an existing account? (y/n): ");
            String have = input.next();
            if (have.equalsIgnoreCase("n")) {
                System.out.print("Choose a password for the new account: ");
                String pw = input.next();
                System.out.print("Enter initial funds: ");
                int userFund = input.nextInt();
                try {
                    account = BankAccount.createNewAccount(pw, userFund);
                    System.out.println("Account created. Your account number is: " + account.getAccountNumber());
                } catch (IOException e) {
                    System.out.println("Error creating account: " + e.getMessage());
                }
            } else {
                System.out.print("Enter account number: ");
                long acct = input.nextLong();
                System.out.print("Enter password: ");
                String pw = input.next();
                try {
                    account = BankAccount.loadAccount(acct);
                    if (!account.checkPassword(pw)) {
                        System.out.println("Invalid password. Try again.");
                        account = null;
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Account not found. Try again.");
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error loading account: " + e.getMessage());
                }
            }
        }

        while (true) {
            System.out.println();
            System.out.println("1- Check account balance.");
            System.out.println("2- Make withdrawal");
            System.out.println("3- Make deposit");
            System.out.println("4- View all transactions");
            System.out.println("5- View deposit history");
            System.out.println("6- View withdrawal history");
            System.out.println("7- Exit");
            System.out.print("Selection: ");
            int choice = input.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("Current account balance is $" + account.getBalance());
                    break;
                case 2:
                    try {
                        System.out.print("Enter the amount to withdraw: ");
                        int withdrawMoney = input.nextInt();
                        System.out.println("Current account balance is $" + account.withdraw(withdrawMoney));
                    } catch (BankAccountException e) {
                        System.out.println(e.getMessage());
                    } catch (IOException e) {
                        System.out.println("Error saving account: " + e.getMessage());
                    }
                    break;
                case 3:
                    try {
                        System.out.print("Enter the amount to deposit: ");
                        int depositMoney = input.nextInt();
                        System.out.println("Current account balance is $" + account.deposit(depositMoney));
                    } catch (BankAccountException e) {
                        System.out.println(e.getMessage());
                    } catch (IOException e) {
                        System.out.println("Error saving account: " + e.getMessage());
                    }
                    break;
                case 4:
                    printList("Transaction History", account.getTransactionHistory());
                    break;
                case 5:
                    printList("Deposit History", account.getDepositHistory());
                    break;
                case 6:
                    printList("Withdrawal History", account.getWithdrawalHistory());
                    break;
                case 7:
                    System.out.println("Thank you for using ACME Bank.");
                    input.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid selection. Please try again.");
            }
        }
    }

    private static void printList(String title, List<String> list) {
        System.out.println("--- " + title + " ---");
        if (list.isEmpty()) {
            System.out.println("No entries found.");
        } else {
            for (String s : list) {
                System.out.println(s);
            }
        }
    }
}
