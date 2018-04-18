package edu.uw.rgm.account;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountFactory;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.dao.AccountDao;


/**
 * A simple account manager that has no persistence, and accepts every login.
 *
 * @author Russ Moul
 */
public final class SimpleAccountManager implements AccountManager {
    /** This class' logger. */
    private static final Logger log =
                         LoggerFactory.getLogger(SimpleAccountManager.class);

    /** Character encoding to use when converting strings to/from bytes */
    private static final String ENCODING = "ISO-8859-1";

    /** The hashing algorithm */
    private static final String ALGORITHM = "SHA1";

    /** The account DAO to use */
    private AccountDao dao;

    /** The factory to use for creating accounts */
    private AccountFactory accountFactory;

    /**
     * Creates a new Simple account manager using the specified AccountDao for
     * persistence.
     *
     * @param dao the DAO to use for persistence
     */
    public SimpleAccountManager(final AccountDao dao) {
        this.dao = dao;
        try (ClassPathXmlApplicationContext 
            appContext = new ClassPathXmlApplicationContext("context.xml")) {
            accountFactory = appContext.getBean(AccountFactory.class);
        } catch (final BeansException ex) {
            log.error("Unable to create account manager.", ex);
        }
    }

    /**
     * Used to persist an account.
     *
     * @param account the account to persist
     *
     * @exception AccountException if operation fails
     */
    public synchronized void persist(final Account account) throws AccountException {
        dao.setAccount(account);
    }

    /**
     * Lookup an account based on username.
     *
     * @param accountName the name of the desired account
     *
     * @return the account if located otherwise null
     *
     * @exception AccountException if operation fails
     */
    public synchronized Account getAccount(final String accountName) throws AccountException {
        final Account acct = dao.getAccount(accountName);
        if (acct != null) {
            acct.registerAccountManager(this);
        }
        return acct;
    }

    /**
     * Remove the account.
     *
     * @param accountName the name of the account to remove
     *
     * @exception AccountException if operation fails
     */
    public synchronized void deleteAccount(final String accountName) throws AccountException {
        final Account acct = dao.getAccount(accountName);

        if (acct != null) {
            dao.deleteAccount(accountName);
        }
    }

    /**
     * Creates an account.
     *
     * @param accountName the name for account to add
     * @param password the password used to gain access to the account
     * @param balance the initial balance of the account
     *
     * @return the newly created account
     *
     * @exception AccountException if the account already exists, or account
     *                             creation fails for any reason
     */
    public synchronized Account createAccount(final String accountName,
                                              final String password,
                                              final int balance)
        throws AccountException {
        if (dao.getAccount(accountName) == null) {
            final byte[] passwordHash = hashPassword(password);

            final Account acct = accountFactory.newAccount(accountName,
                                 passwordHash, balance);
            acct.registerAccountManager(this);
            persist(acct);

            return acct;
        } else {
            throw new AccountException("Account name already in use.");
        }
    }

    /**
     * Check whether a login is valid. Username must exist and password must
     * match.
     *
     * @param accountName name of account the password is to be validated for
     * @param password password is to be validated
     *
     * @return true if password is valid for account identified by username
     *
     * @exception AccountException if error occurs accessing accounts
     */
    public synchronized boolean validateLogin(final String accountName,
                                 final String password)
        throws AccountException {
        boolean valid = false;
        final Account account = getAccount(accountName);

        if (account != null) {
            final byte[] passwordHash = hashPassword(password);
            valid = MessageDigest.isEqual(account.getPasswordHash(),
                                          passwordHash);
        }

        return valid;
    }

    /**
     * Hash the password.
     *
     * @param password the password string to be hashed
     *
     * @return the password hash
     *
     * @throws AccountException if the password hashing operation fails
     */
    private byte[] hashPassword(final String password) throws AccountException {
        try {
            final MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(password.getBytes(ENCODING));
            return md.digest();
        } catch (final NoSuchAlgorithmException e) {
            throw new AccountException("Unable to find hash algorithm", e);
        } catch (final UnsupportedEncodingException e) {
            throw new AccountException(String.format("Unable to find character encoding: %s", ENCODING), e);
        }
    }

    /**
     * Closes the account manager.
     *
     * @exception AccountException if the DAO can't be closed
     */
    public void close() throws AccountException {
        dao.close();
        dao = null;
    }
}

