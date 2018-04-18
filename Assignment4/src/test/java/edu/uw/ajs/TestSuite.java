package test.java.edu.uw.ajs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.AccountTest;
import test.BrokerTest;
import test.AccountManagerTest;
import test.DaoTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AccountTest.class, AccountManagerTest.class, DaoTest.class, BrokerTest.class })
public class TestSuite {
}
