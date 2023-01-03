package io.github.jvanheesch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExampleIT {
    @Autowired
    private MyService myService;

    @Test
    void example1() throws InterruptedException {
        myService.reset();
        new Thread(() -> myService.update()).start();

        myService.getUpdateDone().await();// wait for stmt.executeUpdate() to be executed (MyService)

        String committedDataBeforeCommit = readCommittedData();

        Assertions.assertEquals("INITIAL", committedDataBeforeCommit);

        myService.getReadyForCommit().countDown();
        myService.getCommitDone().await();
    }

    @Test
    void example2() throws InterruptedException {
        myService.reset();
        new Thread(() -> myService.update()).start();

        myService.getUpdateDone().await();// wait for stmt.executeUpdate() to be executed (MyService)

        String uncommittedDataBeforeCommit = readUncommittedData(); // introduce side effects
        String committedDataBeforeCommit = readCommittedData();

        Assertions.assertEquals("INITIAL", committedDataBeforeCommit);

        myService.getReadyForCommit().countDown();
        myService.getCommitDone().await();
    }

    String readUncommittedData() {
        return myService.readUncommittedData();
    }

    String readCommittedData() {
        return myService.readCommittedData();
    }
}
