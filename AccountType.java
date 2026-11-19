public enum AccountType {
    SAVINGS("Savings"), INVESTMENT("Investment"), CHEQUE("Cheque");

    private final String label;
    AccountType(String label) { this.label = label; }
    public String getLabel() { return label; }

    public static AccountType fromLabel(String value) {
        if (value == null) throw new IllegalArgumentException("Choose an account type.");
        for (AccountType type : values()) {
            if (type.label.equalsIgnoreCase(value.trim()) || type.name().equalsIgnoreCase(value.trim())) return type;
        }
        throw new IllegalArgumentException("Unknown account type: " + value);
    }

    @Override public String toString() { return label; }
}
