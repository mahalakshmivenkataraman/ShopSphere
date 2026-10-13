package ecommerce.repository;

import ecommerce.products.Product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ProductRepository implements Repository<Product, Integer> {
    private final Map<Integer, Product> storage = new ConcurrentHashMap<>();

    @Override
    public Product save(Product item) {
        if (item == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        storage.put(item.getId(), item);
        return item;
    }

    public void saveAll(Collection<Product> products) {
        if (products != null) {
            for (Product p : products) {
                if (p != null) {
                    save(p);
                }
            }
        }
    }

    @Override
    public Optional<Product> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(Integer id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }

    @Override
    public void clear() {
        storage.clear();
    }

    public List<Product> findByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return findAll();
        }
        return storage.values().stream()
                .filter(p -> category.equalsIgnoreCase(p.getCategory()))
                .collect(Collectors.toList());
    }
}
