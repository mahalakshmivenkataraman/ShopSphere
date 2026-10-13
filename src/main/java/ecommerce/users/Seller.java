package ecommerce.users;

public class Seller extends User {
    private static final long serialVersionUID = 1L;

    private String storeName;

    public Seller() {
        super();
        setRole(UserRole.SELLER);
    }

    public Seller(String id, String username, String password, String storeName) {
        super(id, username, password, UserRole.SELLER);
        this.storeName = storeName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
