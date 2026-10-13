package ecommerce.users;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin() {
        super();
        setRole(UserRole.ADMIN);
    }

    public Admin(String id, String username, String password) {
        super(id, username, password, UserRole.ADMIN);
    }
}
