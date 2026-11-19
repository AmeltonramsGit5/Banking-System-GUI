import java.math.BigDecimal;

public final class ChequeAccount extends Account {
    public ChequeAccount(String number, long customerId, String branch, BigDecimal balance) {
        super(number, customerId, branch, balance);
    }

    @Override public AccountType getType() { return AccountType.CHEQUE; }
    @Override protected boolean allowsWithdrawals() { return true; }
}
