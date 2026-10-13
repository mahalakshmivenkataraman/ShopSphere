package ecommerce.users;

public class Customer extends User {
    private static final long serialVersionUID = 1L;

    private String address;

    public Customer() {
        super();
        setRole(UserRole.CUSTOMER);
    }

    public Customer(String id, String username, String password, String address) {
        super(id, username, password, UserRole.CUSTOMER);
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
