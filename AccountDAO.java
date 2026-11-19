import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class AccountDAO {
    private final Database database;
    public AccountDAO(Database database) { this.database = database; }

    public void create(Account account) throws SQLException { database.inTransaction(c -> { create(c, account); return null; }); }
    public void create(Connection c, Account account) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO accounts(account_number,customer_id,account_type,branch,balance) VALUES(?,?,?,?,?)")) {
            ps.setString(1, account.getAccountNumber()); ps.setLong(2, account.getCustomerId());
            ps.setString(3, account.getType().name()); ps.setString(4, account.getBranch()); ps.setBigDecimal(5, account.getBalance());
            ps.executeUpdate();
        }
    }

    public Account findByNumber(String accountNumber) throws SQLException { return database.withConnection(c -> findByNumber(c, accountNumber)); }
    public Account findByNumber(Connection c, String accountNumber) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM accounts WHERE account_number=?")) {
            ps.setString(1, accountNumber.trim());
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public List<Account> findByCustomer(long customerId) throws SQLException { return database.withConnection(c -> findByCustomer(c, customerId)); }
    public List<Account> findByCustomer(Connection c, long customerId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM accounts WHERE customer_id=? ORDER BY account_number")) {
            ps.setLong(1, customerId); try (ResultSet rs = ps.executeQuery()) { return readRows(rs); }
        }
    }

    public List<Account> findAll() throws SQLException { return database.withConnection(c -> findAll(c)); }
    public List<Account> findAll(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM accounts ORDER BY account_number"); ResultSet rs = ps.executeQuery()) { return readRows(rs); }
    }

    public void update(Account account) throws SQLException { database.withConnection(c -> { update(c, account); return null; }); }
    public void update(Connection c, Account account) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE accounts SET branch=?,balance=? WHERE account_number=?")) {
            ps.setString(1, account.getBranch()); ps.setBigDecimal(2, account.getBalance()); ps.setString(3, account.getAccountNumber());
            if (ps.executeUpdate() != 1) throw new SQLException("Account not found.");
        }
    }

    public void delete(String accountNumber) throws SQLException { database.withConnection(c -> { delete(c, accountNumber); return null; }); }
    public void delete(Connection c, String accountNumber) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM accounts WHERE account_number=?")) { ps.setString(1, accountNumber); ps.executeUpdate(); }
    }

    private List<Account> readRows(ResultSet rs) throws SQLException {
        List<Account> rows = new ArrayList<>(); while (rs.next()) rows.add(map(rs)); return rows;
    }
    private Account map(ResultSet rs) throws SQLException {
        return AccountFactory.create(AccountType.valueOf(rs.getString("account_type")), rs.getString("account_number"),
                rs.getLong("customer_id"), rs.getString("branch"), rs.getBigDecimal("balance"));
    }
}
