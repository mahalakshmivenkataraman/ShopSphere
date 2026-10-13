package ecommerce.repository;

import ecommerce.users.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository implements Repository<User, String> {
    private final Map<String, User> storage = new ConcurrentHashMap<>();

    @Override
    public User save(User item) {
        if (item == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        storage.put(item.getId(), item);
        return item;
    }

    public void saveAll(Collection<User> users) {
        if (users != null) {
            for (User u : users) {
                if (u != null) {
                    save(u);
                }
            }
        }
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(u -> username.equalsIgnoreCase(u.getUsername()))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
