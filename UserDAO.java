import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class UserDAO {
    private final Database database;
    public UserDAO(Database database) { this.database = database; }

    public void create(BankUser user) throws SQLException { database.inTransaction(c -> { create(c, user); return null; }); }
    public void create(Connection c, BankUser user) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO users(username,password_hash,role,customer_id) VALUES(?,?,?,?)")) {
            ps.setString(1, user.getUsername()); ps.setString(2, user.getPasswordHash()); ps.setString(3, user.getRole());
            if (user.getCustomerId() == null) ps.setNull(4, java.sql.Types.BIGINT); else ps.setLong(4, user.getCustomerId());
            ps.executeUpdate();
        }
    }

    public BankUser findByUsername(String username) throws SQLException {
        return database.withConnection(c -> findByUsername(c, username));
    }
    public BankUser findByUsername(Connection c, String username) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT username,password_hash,role,customer_id FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                long customerId = rs.getLong("customer_id");
                Long nullableId = rs.wasNull() ? null : customerId;
                return new BankUser(rs.getString("username"), rs.getString("password_hash"), rs.getString("role"), nullableId);
            }
        }
    }

    public void update(BankUser user) throws SQLException { database.withConnection(c -> { update(c, user); return null; }); }
    public void update(Connection c, BankUser user) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash=?,role=? WHERE username=?")) {
            ps.setString(1, user.getPasswordHash()); ps.setString(2, user.getRole()); ps.setString(3, user.getUsername());
            if (ps.executeUpdate() != 1) throw new SQLException("User not found.");
        }
    }

    public void delete(String username) throws SQLException { database.withConnection(c -> { delete(c, username); return null; }); }
    public void delete(Connection c, String username) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE username=?")) { ps.setString(1, username); ps.executeUpdate(); }
    }
}
