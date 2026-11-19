import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class TransactionDAO {
    private final Database database;
    public TransactionDAO(Database database) { this.database = database; }

    public void create(BankingTransaction transaction) throws SQLException { database.inTransaction(c -> { create(c, transaction); return null; }); }
    public void create(Connection c, BankingTransaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions(account_number,transaction_type,amount,balance_after,occurred_at,description) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, transaction.getAccountNumber()); ps.setString(2, transaction.getType().name());
            ps.setBigDecimal(3, transaction.getAmount()); ps.setBigDecimal(4, transaction.getBalanceAfter());
            ps.setTimestamp(5, Timestamp.valueOf(transaction.getOccurredAt())); ps.setString(6, transaction.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) transaction.setId(keys.getLong(1)); }
        }
    }

    public List<BankingTransaction> findByAccount(String accountNumber) throws SQLException {
        return database.withConnection(c -> {
            List<BankingTransaction> rows = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM transactions WHERE account_number=? ORDER BY occurred_at,id")) {
                ps.setString(1, accountNumber); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) rows.add(map(rs)); }
            }
            return rows;
        });
    }

    public List<BankingTransaction> findAll() throws SQLException {
        return database.withConnection(c -> {
            List<BankingTransaction> rows = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM transactions ORDER BY occurred_at,id"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(map(rs));
            }
            return rows;
        });
    }

    public void update(BankingTransaction transaction) throws SQLException {
        database.withConnection(c -> {
            String sql = "UPDATE transactions SET account_number=?,transaction_type=?,amount=?,balance_after=?,occurred_at=?,description=? WHERE id=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, transaction.getAccountNumber()); ps.setString(2, transaction.getType().name());
                ps.setBigDecimal(3, transaction.getAmount()); ps.setBigDecimal(4, transaction.getBalanceAfter());
                ps.setTimestamp(5, Timestamp.valueOf(transaction.getOccurredAt())); ps.setString(6, transaction.getDescription()); ps.setLong(7, transaction.getId());
                if (ps.executeUpdate() != 1) throw new SQLException("Transaction not found.");
            }
            return null;
        });
    }

    public void delete(long id) throws SQLException {
        database.withConnection(c -> { try (PreparedStatement ps = c.prepareStatement("DELETE FROM transactions WHERE id=?")) { ps.setLong(1, id); ps.executeUpdate(); } return null; });
    }

    private BankingTransaction map(ResultSet rs) throws SQLException {
        BankingTransaction transaction = new BankingTransaction(rs.getString("account_number"),
                TransactionType.valueOf(rs.getString("transaction_type")), rs.getBigDecimal("amount"),
                rs.getBigDecimal("balance_after"), rs.getTimestamp("occurred_at").toLocalDateTime(), rs.getString("description"));
        transaction.setId(rs.getLong("id")); return transaction;
    }
}
