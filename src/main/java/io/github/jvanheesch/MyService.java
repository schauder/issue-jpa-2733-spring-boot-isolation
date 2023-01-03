package io.github.jvanheesch;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

import static java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;

@Service
public class MyService {
    private final MyEntityRepository repository;
    private final String dbUrl;
    private final String user;
    private final String password;

    private CountDownLatch updateDone;
    private CountDownLatch readyForCommit;
    private CountDownLatch commitDone;


    public CountDownLatch getUpdateDone() {
        return updateDone;
    }
    public CountDownLatch getReadyForCommit() {
        return readyForCommit;
    }

    public CountDownLatch getCommitDone() {
        return commitDone;
    }


    public MyService(MyEntityRepository repository, DataSourceProperties dataSourceProperties) {
        this.repository = repository;
        this.dbUrl = dataSourceProperties.determineUrl();
        this.user = dataSourceProperties.determineUsername();
        this.password = dataSourceProperties.determinePassword();
    }

    void reset() {
        updateDone = new CountDownLatch(1);
        readyForCommit = new CountDownLatch(1);
        commitDone = new CountDownLatch(1);
    }

    // https://www.tutorialspoint.com/jdbc/commit-rollback.htm
    void update() {



        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(dbUrl, user, password);

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(TRANSACTION_READ_UNCOMMITTED);

            stmt = conn.createStatement();

            stmt.executeUpdate(
                    "UPDATE MY_ENTITY " +
                            "SET SOME_VALUE = 'UPDATED' " +
                            "WHERE ID = 1"
            );

            updateDone.countDown();
            readyForCommit.await();

            conn.commit();

            commitDone.countDown();
        } catch (SQLException se) {
            se.printStackTrace();
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    String readUncommittedData() {
        return repository.findById(1L).orElseThrow().getSomeValue();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String readCommittedData() {
        return repository.findById(1L).orElseThrow().getSomeValue();
    }
}
