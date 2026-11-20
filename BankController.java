import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Comparator;

// Controller coordinates the boundary, domain model, and JDBC data access layer.
public class BankController {
    private final Database database;
    private final CustomerDAO customerDAO;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final InterestRunDAO interestRunDAO;
    private final Map<String, Integer> failedLogins = new HashMap<>();
    private BankUser currentUser;

    public BankController() { this(new Database()); }

    public BankController(Database database) {
        this.database = database;
        customerDAO = new CustomerDAO(database);
        userDAO = new UserDAO(database);
        accountDAO = new AccountDAO(database);
        transactionDAO = new TransactionDAO(database);
        interestRunDAO = new InterestRunDAO(database);
        try {
            database.initialize();
            new DataSeeder(database).seedIfEmpty();
            applyMonthlyInterestForPeriod(YearMonth.now());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not initialize the banking database: " + ex.getMessage(), ex);
        }
    }

    public String login(String username, String password) {
        String key = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        if (key.isEmpty() || password == null || password.isEmpty()) return "Enter your username and password.";
        int fails = failedLogins.getOrDefault(key, 0);
        if (fails >= 3) return "Account locked after 3 failed attempts.";
        try {
            BankUser user = userDAO.findByUsername(key);
            if (user == null || !PasswordHasher.verify(password, user.getPasswordHash())) {
                fails++;
                failedLogins.put(key, fails);
                return fails >= 3 ? "Account locked after 3 failed attempts." : "Wrong username or password. Attempts left: " + (3 - fails);
            }
            failedLogins.put(key, 0);
            currentUser = user;
            return "OK";
        } catch (Exception ex) { return "Login failed: " + ex.getMessage(); }
    }

    public void logout() { currentUser = null; }
    public String getCurrentUser() { return currentUser == null ? "" : currentUser.getUsername(); }
    public String getRole() { return currentUser == null ? "" : currentUser.getRole(); }

    public List<Account> getVisibleAccounts() {
        String access = requireSignedIn();
        if (access != null) throw new IllegalStateException(access);
        try {
            return "TELLER".equals(getRole())
                    ? accountDAO.findAll()
                    : accountDAO.findByCustomer(currentUser.getCustomerId());
        } catch (Exception ex) {
            throw new IllegalStateException(message(ex), ex);
        }
    }

    public List<BankingTransaction> getTransactionsForAccount(String accountNumber) {
        String access = requireSignedIn();
        if (access != null) throw new IllegalStateException(access);
        try {
            String number = normalizeAccountNumber(accountNumber);
            Account account = accountDAO.findByNumber(number);
            if (account == null) throw new IllegalArgumentException("Account not found.");
            String ownerError = checkOwnership(account);
            if (ownerError != null) throw new IllegalArgumentException(ownerError);
            List<BankingTransaction> transactions = transactionDAO.findByAccount(number);
            transactions.sort(Comparator.comparing(BankingTransaction::getOccurredAt).reversed()
                    .thenComparing(Comparator.comparingLong(BankingTransaction::getId).reversed()));
            return transactions;
        } catch (Exception ex) {
            throw new IllegalStateException(message(ex), ex);
        }
    }

    public String registerCustomer(String name, String address, String type, boolean employed,
                                   String employerName, String employerAddress) {
        String access = requireRole("TELLER");
        if (access != null) return access;
        try {
            CustomerType customerType = CustomerType.fromLabel(type);
            if (customerType == CustomerType.COMPANY) employed = false;
            Customer customer = new Customer(name, address, customerType, employed, employerName, employerAddress);
            String username = uniqueUsername(name);
            String temporaryPassword = String.valueOf(100000 + new java.security.SecureRandom().nextInt(900000));
            database.inTransaction(c -> {
                customerDAO.create(c, customer);
                userDAO.create(c, new BankUser(username, PasswordHasher.hash(temporaryPassword), "CUSTOMER", customer.getId()));
                return null;
            });
            return "Registered. Login: " + username + " / " + temporaryPassword;
        } catch (Exception ex) { return message(ex); }
    }

    // Kept as a clear migration message for callers using the old demo signature.
    public String registerCustomer(String name, String type, boolean employed) {
        return "Registration now requires the customer's address and employer details.";
    }

    public String openAccount(String type, BigDecimal openingDeposit) {
        String access = requireRole("CUSTOMER");
        if (access != null) return access;
        try {
            AccountType accountType = AccountType.fromLabel(type);
            if (openingDeposit == null || openingDeposit.signum() < 0) return "Deposit cannot be negative.";
            BigDecimal deposit = openingDeposit.setScale(2, java.math.RoundingMode.HALF_UP);
            if (accountType == AccountType.INVESTMENT && deposit.compareTo(InvestmentAccount.MINIMUM_OPENING_DEPOSIT) < 0)
                return "Investment needs at least BWP500.00.";
            Customer owner = customerDAO.findById(currentUser.getCustomerId());
            if (owner == null) return "Customer record not found.";
            if (accountType == AccountType.CHEQUE && (!owner.isEmployed() || owner.getType() != CustomerType.INDIVIDUAL))
                return "Only working individuals can open a Cheque account.";
            String number = "AC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
            Account account = AccountFactory.create(accountType, number, owner.getId(), "Main Branch", deposit);
            database.inTransaction(c -> {
                accountDAO.create(c, account);
                transactionDAO.create(c, new BankingTransaction(number, TransactionType.OPENING_DEPOSIT, deposit,
                        deposit, LocalDateTime.now(), "Account opened"));
                return null;
            });
            return "Account opened. Number: " + number;
        } catch (Exception ex) { return message(ex); }
    }

    public String deposit(String accountNumber, BigDecimal amount) {
        String access = requireSignedIn();
        if (access != null) return access;
        try {
            String number = normalizeAccountNumber(accountNumber);
            return database.inTransaction(c -> {
                Account account = accountDAO.findByNumber(c, number);
                if (account == null) throw new IllegalArgumentException("Account not found.");
                String ownerError = checkOwnership(account);
                if (ownerError != null) throw new IllegalArgumentException(ownerError);
                account.deposit(amount);
                accountDAO.update(c, account);
                transactionDAO.create(c, new BankingTransaction(number, TransactionType.DEPOSIT, amount,
                        account.getBalance(), LocalDateTime.now(), "Cash deposit"));
                return "Deposited. New balance: BWP " + format(account.getBalance());
            });
        } catch (Exception ex) { return message(ex); }
    }

    public String withdraw(String accountNumber, BigDecimal amount) {
        String access = requireSignedIn();
        if (access != null) return access;
        try {
            String number = normalizeAccountNumber(accountNumber);
            return database.inTransaction(c -> {
                Account account = accountDAO.findByNumber(c, number);
                if (account == null) throw new IllegalArgumentException("Account not found.");
                String ownerError = checkOwnership(account);
                if (ownerError != null) throw new IllegalArgumentException(ownerError);
                account.withdraw(amount);
                accountDAO.update(c, account);
                transactionDAO.create(c, new BankingTransaction(number, TransactionType.WITHDRAWAL, amount,
                        account.getBalance(), LocalDateTime.now(), "Cash withdrawal"));
                return "Withdrawn. New balance: BWP " + format(account.getBalance());
            });
        } catch (Exception ex) { return message(ex); }
    }

    public String getBalance(String accountNumber) {
        String access = requireSignedIn();
        if (access != null) return access;
        try {
            Account account = accountDAO.findByNumber(normalizeAccountNumber(accountNumber));
            if (account == null) return "Account not found.";
            String ownerError = checkOwnership(account);
            return ownerError == null ? account.getAccountNumber() + " balance: BWP " + format(account.getBalance()) : ownerError;
        } catch (Exception ex) { return message(ex); }
    }

    public String getSummary() {
        String access = requireSignedIn();
        if (access != null) return access;
        try {
            List<Account> accounts = "TELLER".equals(getRole()) ? accountDAO.findAll() : accountDAO.findByCustomer(currentUser.getCustomerId());
            StringBuilder text = new StringBuilder();
            BigDecimal total = BigDecimal.ZERO;
            for (Account account : accounts) {
                text.append(account.getAccountNumber()).append("  ").append(account.getType().getLabel()).append("  BWP ")
                        .append(format(account.getBalance())).append("\n");
                total = total.add(account.getBalance());
            }
            text.append("TOTAL: BWP ").append(format(total));
            return text.toString();
        } catch (Exception ex) { return message(ex); }
    }

    public String getTransactionHistory(String accountNumber) {
        String access = requireSignedIn();
        if (access != null) return access;
        try {
            String number = normalizeAccountNumber(accountNumber);
            Account account = accountDAO.findByNumber(number);
            if (account == null) return "Account not found.";
            String ownerError = checkOwnership(account);
            if (ownerError != null) return ownerError;
            List<BankingTransaction> rows = transactionDAO.findByAccount(number);
            if (rows.isEmpty()) return "No transactions recorded.";
            StringBuilder text = new StringBuilder();
            for (BankingTransaction tx : rows) {
                text.append(tx.getOccurredAt().toString().replace('T', ' ')).append("  ")
                        .append(tx.getType()).append("  BWP ").append(format(tx.getAmount()))
                        .append("  Balance BWP ").append(format(tx.getBalanceAfter())).append("\n");
            }
            return text.toString();
        } catch (Exception ex) { return message(ex); }
    }

    public String applyMonthlyInterest() {
        String access = requireRole("TELLER");
        if (access != null) return access;
        try { return applyMonthlyInterestForPeriod(YearMonth.now()); }
        catch (Exception ex) { return message(ex); }
    }

    private String applyMonthlyInterestForPeriod(YearMonth month) throws Exception {
        return database.inTransaction(c -> {
            if (interestRunDAO.exists(c, month)) return "Monthly interest was already applied for " + month + ".";
            int count = 0;
            for (Account account : accountDAO.findAll(c)) {
                BigDecimal interest = account.applyMonthlyInterest();
                if (interest.signum() > 0) {
                    accountDAO.update(c, account);
                    transactionDAO.create(c, new BankingTransaction(account.getAccountNumber(), TransactionType.INTEREST,
                            interest, account.getBalance(), LocalDateTime.now(), "Monthly interest for " + month));
                    count++;
                }
            }
            interestRunDAO.markApplied(c, month);
            return "Monthly interest applied to " + count + " eligible accounts for " + month + ".";
        });
    }

    private String requireSignedIn() {
        if (currentUser == null) return "Please log in first.";
        if (!"CUSTOMER".equals(currentUser.getRole()) && !"TELLER".equals(currentUser.getRole())) return "This user role cannot access banking operations.";
        return null;
    }

    private String requireRole(String role) {
        String signedIn = requireSignedIn();
        if (signedIn != null) return signedIn;
        return role.equals(currentUser.getRole()) ? null : "This action requires the " + role.toLowerCase(Locale.ROOT) + " role.";
    }

    private String checkOwnership(Account account) {
        if (currentUser == null) return "Please log in first.";
        if ("TELLER".equals(currentUser.getRole())) return null;
        if (!"CUSTOMER".equals(currentUser.getRole())) return "This user role cannot access accounts.";
        return currentUser.getCustomerId() != null && account.getCustomerId() == currentUser.getCustomerId()
                ? null : "You can only use your own accounts.";
    }

    private String uniqueUsername(String name) throws Exception {
        String base = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (base.isEmpty()) base = "customer";
        if (base.length() > 45) base = base.substring(0, 45);
        String candidate = base;
        int suffix = 2;
        while (userDAO.findByUsername(candidate) != null) candidate = base + suffix++;
        return candidate;
    }

    private String normalizeAccountNumber(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Enter an account number.");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String format(BigDecimal amount) { return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(); }
    private String message(Exception ex) { return ex.getMessage() == null ? "Request failed." : ex.getMessage(); }
}
