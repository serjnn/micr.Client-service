package com.serjnn.ClientService.integration;

import com.serjnn.ClientService.models.Client;
import com.serjnn.ClientService.repo.ClientRepository;
import com.serjnn.ClientService.services.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BalanceConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void shouldHandle100ConcurrentDeductionsSafely() throws InterruptedException {
        // 1. Setup: Create a client with 1000 balance
        String email = "concurrency" + UUID.randomUUID() + "@test.com";
        clientRepository.save(new Client(email, "pass"));
        Client client = clientRepository.findByMail(email).orElseThrow();
        Long clientId = client.id();
        
        clientService.addBalance(clientId, new BigDecimal("1000.00"));

        // 2. Prepare 100 concurrent requests to deduct 10.00 each
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads); // Wait for all to be ready
        CountDownLatch startLatch = new CountDownLatch(1);              // The "starting gun"
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);  // Wait for all to finish
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadNum = i;
            executorService.execute(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    String startTime = java.time.LocalTime.now().toString();
                    System.out.println("Request #" + threadNum + " [Thread " + Thread.currentThread().getId() + "] hitting at: " + startTime);
                    
                    clientService.deductMoney(clientId, new BigDecimal("10.00"));
                    
                    String endTime = java.time.LocalTime.now().toString();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Request #" + threadNum + " failed: " + e.getMessage());
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        // 3. Ensure every single thread is created and waiting at startLatch.await()
        readyLatch.await();
        // 4. Fire!
        startLatch.countDown();
        
        // 5. Wait for completion
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        // 5. Verification
        Client finalClient = clientRepository.findById(clientId).orElseThrow();
        
        System.out.println("Successful deductions: " + successCount.get());
        System.out.println("Failed deductions: " + failureCount.get());
        System.out.println("Final balance: " + finalClient.balance());

        // Total deduction should be 100 * 10 = 1000. Final balance must be 0.
        // If there was a race condition, the balance would likely be > 0 (lost updates).
        assertThat(successCount.get()).isEqualTo(100);
        assertThat(finalClient.balance()).isEqualByComparingTo("0.00");
        
        executorService.shutdown();
    }
}
