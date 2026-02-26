package com.tradeops;

import com.tradeops.model.entity.Category;
import com.tradeops.model.entity.InventoryItem;
import com.tradeops.model.entity.Product;
import com.tradeops.repo.CategoryRepo;
import com.tradeops.repo.InventoryItemRepo;
import com.tradeops.repo.ProductRepo;
import com.tradeops.service.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class ReservationConcurrencyTest {

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryItemRepo inventoryItemRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private CategoryRepo categoryRepo;

    @Test
    public void testConcurrentReservation_PreventsOversell() throws InterruptedException {
        Category category = new Category();
        category.setName("TestCat");
        category.setSlug("test-cat");
        category = categoryRepo.save(category);

        Product product = new Product();
        product.setSku("TEST-IPHONE");
        product.setName("Nothing Phone 4a");
        product.setBasePrice(new BigDecimal("599.00"));
        product.setCategory(category);
        product = productRepo.save(product);

        InventoryItem item = new InventoryItem();
        item.setProduct(product);
        item.setQtyOnHand(1);
        item.setQtyReserved(0);
        inventoryItemRepo.save(item);

        Long productId = product.getId();

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger successfulReservations = new AtomicInteger(0);
        AtomicInteger failedReservations = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    // Пытаемся забронировать 1 штуку
                    inventoryService.reserveStock(productId, 1, 1L);
                    successfulReservations.incrementAndGet();
                } catch (Exception e) {
                    // Ожидаем, что 9 потоков упадут с ошибкой OptimisticLockException или InsufficientStockException
                    failedReservations.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Assertions.assertEquals(1, successfulReservations.get(), "Only one reservation should succeed");
        Assertions.assertEquals(9, failedReservations.get(), "Nine reservations should fail");

        InventoryItem dbItem = inventoryItemRepo.findByProductId(productId).get();
        Assertions.assertEquals(1, dbItem.getQtyReserved(), "Reserved quantity should be exactly 1");
    }
}