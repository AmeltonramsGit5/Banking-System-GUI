import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class CustomerDAO {
    private final Database database;
    public CustomerDAO(Database database) { this.database = database; }

    public long create(Customer customer) throws SQLException { return database.inTransaction(c -> create(c, customer)); }
    public long create(Connection c, Customer customer) throws SQLException {
        String sql = "INSERT INTO customers(name,address,customer_type,employed,employer_name,employer_address) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getName()); ps.setString(2, customer.getAddress());
            ps.setString(3, customer.getType().name()); ps.setBoolean(4, customer.isEmployed());
            ps.setString(5, customer.getEmployerName()); ps.setString(6, customer.getEmployerAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Customer ID was not generated.");
                long id = keys.getLong(1); customer.setId(id); return id;
            }
        }
    }

    public Customer findById(long id) throws SQLException { return database.withConnection(c -> findById(c, id)); }
    public Customer findById(Connection c, long id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM customers WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public List<Customer> findAll() throws SQLException {
        return database.withConnection(c -> {
            List<Customer> rows = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM customers ORDER BY id"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(map(rs));
            }
            return rows;
        });
    }

    public void update(Customer customer) throws SQLException { database.withConnection(c -> { update(c, customer); return null; }); }
    public void update(Connection c, Customer customer) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE customers SET name=?,address=?,customer_type=?,employed=?,employer_name=?,employer_address=? WHERE id=?")) {
            ps.setString(1, customer.getName()); ps.setString(2, customer.getAddress());
            ps.setString(3, customer.getType().name()); ps.setBoolean(4, customer.isEmployed());
            ps.setString(5, customer.getEmployerName()); ps.setString(6, customer.getEmployerAddress());
            ps.setLong(7, customer.getId());
            if (ps.executeUpdate() != 1) throw new SQLException("Customer not found.");
        }
    }

    public void delete(long id) throws SQLException { database.withConnection(c -> { delete(c, id); return null; }); }
    public void delete(Connection c, long id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM customers WHERE id=?")) { ps.setLong(1, id); ps.executeUpdate(); }
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer customer = new Customer(rs.getString("name"), rs.getString("address"),
                CustomerType.valueOf(rs.getString("customer_type")), rs.getBoolean("employed"),
                rs.getString("employer_name"), rs.getString("employer_address"));
        customer.setId(rs.getLong("id"));
        return customer;
    }
}
