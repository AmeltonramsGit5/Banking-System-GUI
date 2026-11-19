import java.math.BigDecimal;

public final class SavingsAccount extends Account implements InterestBearing {
    private static final BigDecimal MONTHLY_RATE = new BigDecimal("0.0005");

    public SavingsAccount(String number, long customerId, String branch, BigDecimal balance) {
        super(number, customerId, branch, balance);
    }

    @Override public AccountType getType() { return AccountType.SAVINGS; }
    @Override protected boolean allowsWithdrawals() { return false; }
    @Override public BigDecimal monthlyInterestRate() { return MONTHLY_RATE; }
    @Override public BigDecimal applyMonthlyInterest() {
        BigDecimal interest = money(getBalance().multiply(monthlyInterestRate()));
        creditInterest(interest);
        return interest;
    }
}
