import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BankingIntegrationTest {
    private Database freshDatabase() {
        String url = "jdbc:h2:mem:bank_test_" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1";
        return new Database(url);
    }

    private BankController freshController() { return new BankController(freshDatabase()); }

    @Test
    void depositPersistsBalanceAndTransactionForOwner() {
        BankController bank = freshController();
        assertEquals("Please log in first.", bank.getBalance("AC1001"));
        assertEquals("OK", bank.login("john", "1234"));
        assertTrue(bank.deposit("AC1001", new BigDecimal("25.50")).startsWith("Deposited."));
        assertTrue(bank.getBalance("AC1001").contains("525.75"));
        assertTrue(bank.getTransactionHistory("AC1001").contains("DEPOSIT"));
    }

    @Test
    void customerCannotAccessAnotherCustomersAccountAndSavingsCannotWithdraw() {
        BankController bank = freshController();
        assertEquals("OK", bank.login("john", "1234"));
        assertEquals("You can only use your own accounts.", bank.getBalance("AC1003"));
        assertEquals("You can only use your own accounts.", bank.deposit("AC1003", new BigDecimal("1.00")));
        assertTrue(bank.withdraw("AC1001", new BigDecimal("1.00")).contains("Withdrawals are not allowed"));
    }

    @Test
    void investmentMinimumAndInterestRunAreEnforced() {
        BankController bank = freshController();
        assertEquals("OK", bank.login("john", "1234"));
        assertEquals("Investment needs at least BWP500.00.", bank.openAccount("Investment", new BigDecimal("499.99")));
        assertTrue(bank.openAccount("Investment", new BigDecimal("500.00")).startsWith("Account opened."));
        bank.logout();
        assertEquals("OK", bank.login("teller", "1234"));
        assertTrue(bank.applyMonthlyInterest().contains("already applied"));
    }

    @Test
    void registrationRequiresTellerAndSeedContainsTenCustomers() throws Exception {
        Database database = freshDatabase();
        BankController bank = new BankController(database);
        assertEquals(10, new CustomerDAO(database).findAll().size());
        assertEquals("OK", bank.login("john", "1234"));
        assertEquals("This action requires the teller role.", bank.registerCustomer("New Customer", "Gaborone", "Individual", false, "", ""));
        bank.logout();
        assertEquals("OK", bank.login("teller", "1234"));
        assertTrue(bank.registerCustomer("New Customer", "Gaborone", "Individual", false, "", "").startsWith("Registered."));
    }
    @Test
    void fileBackedH2DatabaseCanSeedAndReopenAcrossDaoConnections() throws Exception {
        java.nio.file.Path directory = java.nio.file.Paths.get("target", "integration-db-" + UUID.randomUUID());
        java.nio.file.Files.createDirectories(directory);
        String url = "jdbc:h2:file:" + directory.resolve("banking").toAbsolutePath().toString().replace('\\', '/') + ";DB_CLOSE_DELAY=-1";
        Database database = new Database(url);
        BankController bank = new BankController(database);
        assertEquals("OK", bank.login("john", "1234"));
        assertTrue(bank.getBalance("AC1001").contains("500.25"));
        try (java.sql.Connection connection = database.getConnection(); java.sql.Statement statement = connection.createStatement()) {
            statement.execute("SHUTDOWN");
        }
    }
}
