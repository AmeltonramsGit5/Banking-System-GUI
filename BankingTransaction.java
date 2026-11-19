import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class BankingTransaction {
    private long id;
    private final String accountNumber;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final LocalDateTime occurredAt;
    private final String description;

    public BankingTransaction(String accountNumber, TransactionType type, BigDecimal amount,
                              BigDecimal balanceAfter, LocalDateTime occurredAt, String description) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
        this.balanceAfter = balanceAfter.setScale(2, java.math.RoundingMode.HALF_UP);
        this.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
        this.description = description == null ? "" : description;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public String getDescription() { return description; }
}
