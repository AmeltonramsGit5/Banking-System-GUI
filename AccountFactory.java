import java.math.BigDecimal;

public final class AccountFactory {
    private AccountFactory() { }

    public static Account create(AccountType type, String number, long customerId, String branch, BigDecimal balance) {
        switch (type) {
            case SAVINGS: return new SavingsAccount(number, customerId, branch, balance);
            case INVESTMENT: return new InvestmentAccount(number, customerId, branch, balance);
            case CHEQUE: return new ChequeAccount(number, customerId, branch, balance);
            default: throw new IllegalArgumentException("Unsupported account type.");
        }
    }
}
