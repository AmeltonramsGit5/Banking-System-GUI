import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;

public final class InterestRunDAO {
    private final Database database;
    public InterestRunDAO(Database database) { this.database = database; }

    public boolean exists(Connection c, YearMonth month) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM interest_runs WHERE month_key=?")) {
            ps.setString(1, month.toString()); try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void markApplied(Connection c, YearMonth month) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO interest_runs(month_key,completed_at) VALUES(?,?)")) {
            ps.setString(1, month.toString()); ps.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now())); ps.executeUpdate();
        }
    }
}
