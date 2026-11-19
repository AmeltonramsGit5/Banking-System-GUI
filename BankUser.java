public final class BankUser {
    private final String username;
    private String passwordHash;
    private String role;
    private final Long customerId;

    public BankUser(String username, String passwordHash, String role, Long customerId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.customerId = customerId;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getCustomerId() { return customerId; }
}
