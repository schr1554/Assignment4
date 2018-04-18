package edu.uw.rgm.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;
import edu.uw.ext.framework.order.Order;


/**
 * Implementation of the Account interface as a JavaBean.
 *
 * @author Russ Moul
 */
public final class SimpleAccount implements Account {

    /** Version id */
    private static final long serialVersionUID = 9028406835809215441L;

    /** The logger to be used by this class */
    private static final Logger logger = LoggerFactory.getLogger(SimpleAccount.class);

    /** The minimum allowed account length */
    private static final int MIN_ACCT_LEN = 8;

    /** The minimum allowed initial account balance */
    private static final int MIN_ACCT_BALANCE = 100_000;

    /** The account name */
    private String name;

    /** The hashed account password */
    private byte[] passwordHash;

    /** The account balance, in cents. */
    private int balance = Integer.MIN_VALUE;

    /** The account holders full name */
    private String fullName;

    /** The account holders address */
    private Address address;

    /** The account holders phone number */
    private String phone;

    /** The account holders email address */
    private String email;

    /** The account holders credit card */
    private CreditCard creditCard;

    /** Account manager responsible for managing this account */
    private transient AccountManager acctMngr;

    /**
     * No parameter constructor, required by JavaBeans.
     */
    public SimpleAccount() {
    }

    /**
     * Constructor, validates length of account name and the initial balance.
     *
     * @param acctName the account name
     * @param passwordHash the password hash
     * @param balance the balance
     *
     * @throws AccountException if the account name is too short or balance
     *                          too low
     */
    public SimpleAccount(final String acctName, final byte[] passwordHash,
                         final int balance)
        throws AccountException {

        if (balance < MIN_ACCT_BALANCE) {
            final String msg = String.format("Account creation failed for , account '%s' minimum balance not met, %d",
                                             acctName, balance);
            logger.warn(msg);
            throw new AccountException(msg);
        }

        setName(acctName);
        setPasswordHash(passwordHash);
        this.balance = balance;
    }

    /**
     * Get the account name.
     *
     * @return the name of the account
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the account name.  The name will be checked for minimum length based
     * on the edu.uw.ext.account.SimpleAccount.minAccountLength
     * preference entry.
     *
     * This operation is not generally used but is provided for JavaBean
     * conformance.
     *
     * @param acctName the value to be set for the account name
     *
     * @throws AccountException if the account name is to short
     */
    public void setName(final String acctName) throws AccountException {
        if (acctName == null || acctName.length() < MIN_ACCT_LEN) {
            final String msg = String.format("Account name '%s' is unacceptable.", acctName);
            logger.warn(msg);
            throw new AccountException(msg);
        }

        name = acctName;
    }

    /**
     * Gets the hashed password.
     *
     * @return the hashed password
     */
    public byte[] getPasswordHash() {
        byte[] copy = null;
        if (passwordHash != null) {
            copy = new byte[passwordHash.length];
            System.arraycopy(passwordHash, 0, copy, 0, passwordHash.length);
        }
        return copy;
    }

    /**
     * Sets the hashed password.
     *
     * @param passwordHash the value to be set for the password hash
     */
    public void setPasswordHash(final byte[] passwordHash) {
        byte[] copy = null;
        if (passwordHash != null) {
            copy = new byte[passwordHash.length];
            System.arraycopy(passwordHash, 0, copy, 0, passwordHash.length);
        }
        this.passwordHash = copy;
    }

    /**
     * Gets the account balance.
     *
     * @return the current balance of the account
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Sets the account balance.
     *
     * @param balance the value to set the balance to
     */
    public void setBalance(final int balance) {
        this.balance = balance;
    }

    /**
     * Gets the full name of the account holder.
     *
     * @return the account holders full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name of the account holder.
     *
     * @param fullName the account holders full name
     */
    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the account address.
     *
     *  @return the accounts address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the account address.
     *
     *  @param address the address for the account
     */
    public void setAddress(final Address address) {
        this.address = address;
    }

    /**
     * Gets the phone number.
     *
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the account phone number.
     *
     * @param phone value for the account phone number
     */
    public void setPhone(final String phone) {
        this.phone = phone;
    }

    /**
     * Gets the email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the account email address.
     *
     * @param email the email address
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Gets the account credit card.
     *
     * @return the credit card
     */
    public CreditCard getCreditCard() {
        return creditCard;
    }

    /**
     * Sets the account credit card.
     *
     * @param card the value to be set for the credit card
     */
    public void setCreditCard(final CreditCard card) {
        creditCard = card;
    }

    /**
     * Sets the account manager responsible for persisting/managing this
     * account. This may be invoked exactly once on any given account, any
     * subsequent invocations should be ignored.
     *
     * @param m the account manager
     */
    public void registerAccountManager(final AccountManager m) {
        if (acctMngr == null) {
            acctMngr = m;
        } else {
            logger.info("Attempting to set the account manager, after it has been initialized.");
        }
    }

    /**
     * Incorporates the effect of an order in the balance.  Increments or
     * decrements the account balance by the execution price * number of shares
     * in the order and then persists the account, using the account manager.
     *
     * @param order the order to be reflected in the account
     * @param executionPrice the price the order was executed at
     */
    public void reflectOrder(final Order order, final int executionPrice) {
        try {
            balance += order.valueOfOrder(executionPrice);
            if (acctMngr != null) {
                acctMngr.persist(this);
            } else {
                logger.error("Account manager has not been initialized.",
                             new Exception());
            }
        } catch (final AccountException ex) {
            logger.error(String.format("Failed to persist account %s after adjusting for order.", name),
                         ex);
        }
    }
}

