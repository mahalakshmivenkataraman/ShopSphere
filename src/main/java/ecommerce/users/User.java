package ecommerce.users;

import java.io.Serializable;
import java.util.Objects;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String password;
    private UserRole role;

    public User() {
    }

    public User(String id, String username, String password, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Overloaded constructor accepting role as String for backward compatibility
    public User(String id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        if (role != null) {
            try {
                this.role = UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.role = UserRole.CUSTOMER;
            }
        } else {
            this.role = UserRole.CUSTOMER;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Aliases for Person 1 & 2 GUI compatibility
    public String getUserId() {
        return id;
    }

    public void setUserId(String userId) {
        this.id = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public String getRoleString() {
        return role != null ? role.name() : "CUSTOMER";
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }
}
