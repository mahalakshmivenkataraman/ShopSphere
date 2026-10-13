package ecommerce.repository;

import ecommerce.orders.Order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OrderRepository implements Repository<Order, String> {
    private final Map<String, Order> storage = new ConcurrentHashMap<>();

    @Override
    public Order save(Order item) {
        if (item == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        storage.put(item.getId(), item);
        return item;
    }

    public void saveAll(Collection<Order> orders) {
        if (orders != null) {
            for (Order o : orders) {
                if (o != null) {
                    save(o);
                }
            }
        }
    }

    @Override
    public Optional<Order> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    public List<Order> findByCustomerId(String customerId) {
        if (customerId == null) {
            return new ArrayList<>();
        }
        return storage.values().stream()
                .filter(o -> customerId.equalsIgnoreCase(o.getCustomerId()) || customerId.equalsIgnoreCase(o.getCustomerName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAll() {
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
