package ecommerce.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T save(T item);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean deleteById(ID id);
    void clear();
}
