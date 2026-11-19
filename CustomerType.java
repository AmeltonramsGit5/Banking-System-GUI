public enum CustomerType {
    INDIVIDUAL, COMPANY;

    public static CustomerType fromLabel(String value) {
        if (value == null) throw new IllegalArgumentException("Choose a customer type.");
        String normalized = value.trim().replace(' ', '_').toUpperCase();
        return CustomerType.valueOf(normalized);
    }
}
