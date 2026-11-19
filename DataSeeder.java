import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

public final class DataSeeder {
    private final Database database;
    private final CustomerDAO customers;
    private final UserDAO users;
    private final AccountDAO accounts;
    private final TransactionDAO transactions;

    public DataSeeder(Database database) {
        this.database = database; customers = new CustomerDAO(database); users = new UserDAO(database);
        accounts = new AccountDAO(database); transactions = new TransactionDAO(database);
    }

    public void seedIfEmpty() throws java.sql.SQLException {
        database.inTransaction(c -> {
            try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM customers"); java.sql.ResultSet rs = ps.executeQuery()) {
                rs.next(); if (rs.getLong(1) != 0) return null;
            }
            users.create(c, new BankUser("teller", PasswordHasher.hash("1234"), "TELLER", null));
            List<String> names = Arrays.asList("John Sample", "Mary Sample", "Peter Sample", "Thato Sample", "Neo Sample", "Kago Sample", "Naledi Sample", "Lesedi Sample", "Amantle Sample", "Keitumetse Sample");
            int accountIndex = 1001;
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                boolean employed = i != 9;
                Customer customer = new Customer(name, "Gaborone, Botswana", i == 9 ? CustomerType.COMPANY : CustomerType.INDIVIDUAL,
                        employed, employed ? "Sample Employer " + (i + 1) : "", employed ? "Gaborone, Botswana" : "");
                customers.create(c, customer);
                String username = i == 0 ? "john" : "customer" + (i + 1);
                users.create(c, new BankUser(username, PasswordHasher.hash("1234"), "CUSTOMER", customer.getId()));
                int accountsForCustomer = i == 0 ? 2 : 1;
                for (int j = 0; j < accountsForCustomer; j++) {
                    AccountType type;
                    if (i == 0 && j == 0) type = AccountType.SAVINGS;
                    else if (i == 0) type = AccountType.INVESTMENT;
                    else if (i == 9) type = AccountType.SAVINGS;
                    else type = new AccountType[] { AccountType.CHEQUE, AccountType.INVESTMENT, AccountType.SAVINGS }[(i - 1) % 3];
                    BigDecimal opening = type == AccountType.INVESTMENT ? new BigDecimal("800.00") : new BigDecimal("500.00");
                    String number = String.format("AC%04d", accountIndex++);
                    Account account = AccountFactory.create(type, number, customer.getId(), "Main Branch", opening);
                    accounts.create(c, account);
                    transactions.create(c, new BankingTransaction(number, TransactionType.OPENING_DEPOSIT, opening, opening,
                            java.time.LocalDateTime.now(), "Sample opening deposit"));
                }
            }
            return null;
        });
    }
}
