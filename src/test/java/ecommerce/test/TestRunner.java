package ecommerce.test;

import ecommerce.concurrency.InventoryConcurrencyTestRunner;
import ecommerce.persistence.FileManagerTestRunner;
import ecommerce.repository.RepositoryTestRunner;

public class TestRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("        ShopSphere Integration Test Runner       ");
        System.out.println("=================================================");

        int passed = 0;
        int failed = 0;

        try {
            System.out.print("[TEST 1/6] Running RepositoryTest... ");
            RepositoryTestRunner.runTests();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace(System.out);
            failed++;
        }

        try {
            System.out.print("[TEST 2/6] Running FileManagerTest... ");
            FileManagerTestRunner.runTests();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace(System.out);
            failed++;
        }

        try {
            System.out.print("[TEST 3/6] Running InventoryConcurrencyTest... ");
            InventoryConcurrencyTestRunner.runTests();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace(System.out);
            failed++;
        }

        System.out.println("-------------------------------------------------");
        try {
            System.out.print("[TEST 4/6] Running ShopSphereCoherenceTest... ");
            ShopSphereCoherenceTestRunner.runTests();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace(System.out);
            failed++;
        }

        try {
            System.out.print("[TEST 5/6] Running SellerWorkflowTest... ");
            SellerWorkflowTestRunner.runTests();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace(System.out);
            failed++;
        }

        try {
            System.out.print("[TEST 6/6] Running SyllabusConceptTest... ");
            SyllabusConceptTestRunner.runTests();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace(System.out);
            failed++;
        }

        System.out.println("=================================================");
        System.out.println("Test Results: " + passed + " Passed, " + failed + " Failed.");
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
