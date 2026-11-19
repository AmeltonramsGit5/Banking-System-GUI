import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class Account {
    private final String accountNumber;
    private final long customerId;
    private final String branch;
    private BigDecimal balance;

    protected Account(String accountNumber, long customerId, String branch, BigDecimal balance) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) throw new IllegalArgumentException("Account number is required.");
        if (customerId <= 0) throw new IllegalArgumentException("A customer must own each account.");
        this.accountNumber = accountNumber.trim();
        this.customerId = customerId;
        this.branch = branch == null || branch.trim().isEmpty() ? "Main" : branch.trim();
        this.balance = money(balance);
        if (this.balance.signum() < 0) throw new IllegalArgumentException("Balance cannot be negative.");
    }

    public abstract AccountType getType();
    protected abstract boolean allowsWithdrawals();

    public final String getAccountNumber() { return accountNumber; }
    public final long getCustomerId() { return customerId; }
    public final String getBranch() { return branch; }
    public final BigDecimal getBalance() { return balance; }

    public final void deposit(BigDecimal amount) {
        BigDecimal value = positiveAmount(amount);
        balance = money(balance.add(value));
    }

    public final void withdraw(BigDecimal amount) {
        if (!allowsWithdrawals()) throw new IllegalStateException("Withdrawals are not allowed on Savings accounts.");
        BigDecimal value = positiveAmount(amount);
        if (value.compareTo(balance) > 0) throw new IllegalStateException("Insufficient funds.");
        balance = money(balance.subtract(value));
    }

    public BigDecimal applyMonthlyInterest() { return BigDecimal.ZERO.setScale(2); }

    protected final void creditInterest(BigDecimal amount) {
        BigDecimal value = money(amount);
        if (value.signum() < 0) throw new IllegalArgumentException("Interest cannot be negative.");
        balance = money(balance.add(value));
    }

    protected static BigDecimal money(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("Enter an amount.");
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    protected static BigDecimal positiveAmount(BigDecimal amount) {
        BigDecimal value = money(amount);
        if (value.signum() <= 0) throw new IllegalArgumentException("Amount must be more than 0.");
        return value;
    }
}
