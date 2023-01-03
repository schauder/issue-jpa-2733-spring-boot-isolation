package io.github.jvanheesch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootTest
class ExampleIT {
	@Autowired
	private MyService myService;

	@Autowired
	private DataSource dataSource;

	@Test
	void readCommittedDoesNotSeeUncommittedUpdate() throws InterruptedException {
		myService.reset();
		new Thread(() -> myService.update()).start();

		myService.getUpdateDone().await();// wait for stmt.executeUpdate() to be executed (MyService)

		String committedDataBeforeCommit = readCommittedData();

		Assertions.assertEquals("INITIAL", committedDataBeforeCommit);

		myService.getReadyForCommit().countDown();
		myService.getCommitDone().await();
	}

	@Test
	void readCommittedSeesUncommittedUpdateWhenReadUncommittedFirst() throws InterruptedException {
		myService.reset();
		new Thread(() -> myService.update()).start();

		myService.getUpdateDone().await();// wait for stmt.executeUpdate() to be executed (MyService)

		String uncommittedDataBeforeCommit = readUncommittedData(); // introduce side effects
		Assertions.assertEquals("UPDATED", uncommittedDataBeforeCommit);
		String committedDataBeforeCommit = readCommittedData();

		Assertions.assertEquals("INITIAL", committedDataBeforeCommit);

		myService.getReadyForCommit().countDown();
		myService.getCommitDone().await();
	}

	@Test
	void pureJdbc() throws InterruptedException {
		myService.reset();
		new Thread(() -> myService.update()).start();

		myService.getUpdateDone().await();// wait for stmt.executeUpdate() to be executed (MyService)

		String uncommittedDataBeforeCommit = readUncommittedDataJdbc(); // introduce side effects

		Assertions.assertEquals("UPDATED", uncommittedDataBeforeCommit);

		String committedDataBeforeCommit = readCommittedDataJdbc();

		Assertions.assertEquals("INITIAL", committedDataBeforeCommit);

		myService.getReadyForCommit().countDown();
		myService.getCommitDone().await();
	}

	private String readCommittedDataJdbc() {
		return read(Connection.TRANSACTION_READ_COMMITTED);
	}

	private String readUncommittedDataJdbc() {
		return read(Connection.TRANSACTION_READ_UNCOMMITTED);
	}

	private String read(int transactionIsolation) {
		try {
			Connection con = dataSource.getConnection();
			con.setTransactionIsolation(transactionIsolation);
			Statement stmnt = con.createStatement();

			ResultSet resultSet = stmnt.executeQuery("select some_value from my_entity where id =1");

			boolean next = resultSet.next();
			Assertions.assertTrue(next, "No row returned.");

			return resultSet.getString(1);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


	String readUncommittedData() {
		return myService.readUncommittedData();
	}

	String readCommittedData() {
		return myService.readCommittedData();
	}
}
