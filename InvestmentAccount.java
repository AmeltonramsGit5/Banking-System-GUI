import java.math.BigDecimal;

public final class InvestmentAccount extends Account implements InterestBearing {
    public static final BigDecimal MINIMUM_OPENING_DEPOSIT = new BigDecimal("500.00");
    private static final BigDecimal MONTHLY_RATE = new BigDecimal("0.05");

    public InvestmentAccount(String number, long customerId, String branch, BigDecimal balance) {
        super(number, customerId, branch, balance);
    }

    @Override public AccountType getType() { return AccountType.INVESTMENT; }
    @Override protected boolean allowsWithdrawals() { return true; }
    @Override public BigDecimal monthlyInterestRate() { return MONTHLY_RATE; }
    @Override public BigDecimal applyMonthlyInterest() {
        BigDecimal interest = money(getBalance().multiply(monthlyInterestRate()));
        creditInterest(interest);
        return interest;
    }
}
