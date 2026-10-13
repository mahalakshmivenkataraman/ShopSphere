package ecommerce.persistence;

import ecommerce.exceptions.PersistenceException;
import ecommerce.orders.Order;
import ecommerce.products.Product;
import ecommerce.repository.OrderRepository;
import ecommerce.repository.ProductRepository;
import ecommerce.repository.UserRepository;
import ecommerce.users.User;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class FileManager {
    private final String dataDir;
    private static final ReentrantLock FILE_LOCK = new ReentrantLock();

    public FileManager() {
        this("data");
    }

    public FileManager(String dataDir) {
        this.dataDir = dataDir;
        ensureDataDirectoryExists();
    }

    private void ensureDataDirectoryExists() {
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String getDataDir() {
        return dataDir;
    }

    // Generic save using temp file + atomic move
    public <T> void saveListToFile(String filename, Collection<T> items) {
        FILE_LOCK.lock();
        try {
            ensureDataDirectoryExists();
            Path targetPath = Paths.get(dataDir, filename);
            Path tempPath = Paths.get(dataDir, filename + ".tmp");

            List<T> listToSave = new ArrayList<>(items);

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(tempPath.toFile())))) {
                oos.writeObject(listToSave);
                oos.flush();
            } catch (IOException e) {
                throw new PersistenceException("Failed to write temporary file: " + tempPath, e);
            }

            try {
                try {
                    Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException atomicEx) {
                    // Fallback to standard replace if ATOMIC_MOVE fails on Windows
                    Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new PersistenceException("Failed to atomically replace file: " + targetPath, e);
            }
        } finally {
            FILE_LOCK.unlock();
        }
    }

    // Generic load
    @SuppressWarnings("unchecked")
    public <T> List<T> loadListFromFile(String filename) {
        FILE_LOCK.lock();
        try {
            Path targetPath = Paths.get(dataDir, filename);
            File file = targetPath.toFile();

            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }

            try (ObjectInputStream ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(file)))) {
                Object obj = ois.readObject();
                if (obj instanceof List<?>) {
                    return (List<T>) obj;
                }
                return new ArrayList<>();
            } catch (IOException | ClassNotFoundException e) {
                throw new PersistenceException("Failed to load data from file: " + targetPath, e);
            }
        } finally {
            FILE_LOCK.unlock();
        }
    }

    // Entity specific methods
    public void saveProducts(Collection<Product> products) {
        saveListToFile("products.dat", products);
    }

    public List<Product> loadProducts() {
        return loadListFromFile("products.dat");
    }

    public void saveUsers(Collection<User> users) {
        saveListToFile("users.dat", users);
    }

    public List<User> loadUsers() {
        return loadListFromFile("users.dat");
    }

    public void saveOrders(Collection<Order> orders) {
        saveListToFile("orders.dat", orders);
    }

    public List<Order> loadOrders() {
        return loadListFromFile("orders.dat");
    }

    public void saveAll(ProductRepository productRepo, UserRepository userRepo, OrderRepository orderRepo) {
        if (productRepo != null) saveProducts(productRepo.findAll());
        if (userRepo != null) saveUsers(userRepo.findAll());
        if (orderRepo != null) saveOrders(orderRepo.findAll());
    }

    public void loadAll(ProductRepository productRepo, UserRepository userRepo, OrderRepository orderRepo) {
        if (productRepo != null) {
            productRepo.clear();
            productRepo.saveAll(loadProducts());
        }
        if (userRepo != null) {
            userRepo.clear();
            userRepo.saveAll(loadUsers());
        }
        if (orderRepo != null) {
            orderRepo.clear();
            orderRepo.saveAll(loadOrders());
        }
    }
}
