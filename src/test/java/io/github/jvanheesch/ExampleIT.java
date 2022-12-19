package io.github.jvanheesch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Testcontainers
class ExampleIT {
    @Container
    private final GenericContainer<?> APP_CONTAINER = new GenericContainer<>("docker.io/library/spring-boot-isolation:1.0-SNAPSHOT")
            .withExposedPorts(8080)
            .waitingFor(new LogMessageWaitStrategy()
                    .withRegEx(".*Started Application.*"));

    private String host;
    private Integer port;

    @BeforeEach
    void setup() {
        host = APP_CONTAINER.getHost();
        port = APP_CONTAINER.getMappedPort(8080);
    }

    @Test
    void example1() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = WebClient.builder().build()
                .post()
                .uri(String.format("http://%s:%s/update", host, port))
                .retrieve()
                .bodyToMono(Void.class)
                .toFuture();

        Thread.sleep(1000); // wait for stmt.executeUpdate() to be executed (MyService)

        String committedDataBeforeCommit = readCommittedData();

        future.get();

        Assertions.assertEquals("INITIAL", committedDataBeforeCommit);
    }

    @Test
    void example2() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = WebClient.builder().build()
                .post()
                .uri(String.format("http://%s:%s/update", host, port))
                .retrieve()
                .bodyToMono(Void.class)
                .toFuture();

        Thread.sleep(1000); // wait for stmt.executeUpdate() to be executed (MyService)

        String uncommittedDataBeforeCommit = readUncommittedData(); // introduce side effects
        String committedDataBeforeCommit = readCommittedData();

        future.get();

        Assertions.assertEquals("INITIAL", committedDataBeforeCommit);
    }

    String readUncommittedData() {
        String url = String.format("http://%s:%s/read_uncommitted_data", host, port);

        WebClient webclient = WebClient.builder().build();

        return webclient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    String readCommittedData() {
        String url = String.format("http://%s:%s/read_committed_data", host, port);

        WebClient webclient = WebClient.builder().build();

        return webclient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
