public final class Customer {
    private long id;
    private final String name;
    private final String address;
    private final CustomerType type;
    private final boolean employed;
    private final String employerName;
    private final String employerAddress;

    public Customer(String name, String address, CustomerType type, boolean employed,
                    String employerName, String employerAddress) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Enter a customer name.");
        if (address == null || address.trim().isEmpty()) throw new IllegalArgumentException("Enter an address.");
        this.name = name.trim();
        this.address = address.trim();
        this.type = type;
        this.employed = employed;
        this.employerName = employerName == null ? "" : employerName.trim();
        this.employerAddress = employerAddress == null ? "" : employerAddress.trim();
        if (employed && type == CustomerType.INDIVIDUAL &&
                (this.employerName.isEmpty() || this.employerAddress.isEmpty())) {
            throw new IllegalArgumentException("Enter the employer name and address for an employed customer.");
        }
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public CustomerType getType() { return type; }
    public boolean isEmployed() { return employed; }
    public String getEmployerName() { return employerName; }
    public String getEmployerAddress() { return employerAddress; }
}
