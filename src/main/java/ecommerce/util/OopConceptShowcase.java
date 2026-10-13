package ecommerce.util;

import ecommerce.products.Product;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

/**
 * Small executable examples of syllabus concepts used by ShopSphere.
 * The application itself uses these concepts throughout the domain, repository,
 * persistence, GUI and concurrency layers; this class keeps the concepts easy to demonstrate.
 */
public final class OopConceptShowcase {
    private static int runCount;
    private final List<String> supportedCollections = new ArrayList<>();

    static { runCount = 0; }                 // static initialization block
    { supportedCollections.add("List"); }   // instance initialization block

    public OopConceptShowcase() { runCount++; }

    public static void runDemo() {
        OopConceptShowcase demo = new OopConceptShowcase();
        demo.supportedCollections.addAll(Arrays.asList("Set", "Queue", "Map", "Tree"));

        // Generic method + bounded generic type + var-args.
        List<Product> products = List.of(
                new Product(1, "Demo Laptop", "Demo", "Electronics", BigDecimal.valueOf(50000), 2, true),
                new Product(2, "Demo Mouse", "Demo", "Accessories", BigDecimal.valueOf(1500), 5, true));
        long expensive = demo.countMatching(products, p -> p.getPriceAsDouble() > 10000);

        Set<String> categories = new TreeSet<>(Arrays.asList("Electronics", "Books", "Electronics"));
        Queue<String> checkoutQueue = new LinkedList<>();
        checkoutQueue.offer("Customer-A");
        Map<Integer, Product> productMap = new HashMap<>();
        for (Product p : products) productMap.put(p.getId(), p);

        Iterator<String> iterator = categories.iterator();
        while (iterator.hasNext()) iterator.next();
        String joined = join("OOP", "Generics", "Collections", "Threads", "JUnit");
        System.out.println("[OOP Demo] matching=" + expensive + ", queueSize=" + checkoutQueue.size()
                + ", mapSize=" + productMap.size() + ", topics=" + joined);
    }

    public <T> long countMatching(Collection<T> values, Predicate<T> predicate) {
        return values.stream().filter(predicate).count();
    }

    public static String join(String... values) { return String.join(" -> ", values); }
    public static int getRunCount() { return runCount; }
}
