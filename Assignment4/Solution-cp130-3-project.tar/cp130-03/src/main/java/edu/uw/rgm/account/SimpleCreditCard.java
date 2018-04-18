package edu.uw.rgm.account;

import edu.uw.ext.framework.account.CreditCard;


/**
 * A straight forward implementation of the CreditCard interface as a JavaBean.
 *
 * @author Russ Moul
 */
public final class SimpleCreditCard implements CreditCard {
    /** Version id. */
    private static final long serialVersionUID = 774801588677947281L;

    /** The credit card issuer */
    private String issuer;

    /** The type of credit card */
    private String type;

    /** The credit card holders name */
    private String holder;

    /** The credit card account number */
    private String accountNumber;

    /** The credit card expiration date */
    private String expireDate;

    /**
     * No parameter constructor, required by JavaBeans.
     */
    public SimpleCreditCard() {
        super();
    }

    /**
     * Gets the card account number.
     *
     * @return the account number
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the card account number.
     *
     * @param accountNumber the account number
     */
    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Gets the card issuer.
     *
     * @return the card issuer
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Sets the card issuer.
     *
     * @param issuer the card issuer
     */
    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    /**
     * Gets the card type.
     *
     * @return the card type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the card type.
     *
     * @param type the card type
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Gets the card holder's name.
     *
     * @return the card holders name
     */
    public String getHolder() {
        return holder;
    }

    /**
     * Sets the card holder's name.
     *
     * @param name the card holders name
     */
    public void setHolder(final String name) {
        holder = name;
    }

    /**
     * Gets the card expiration date.
     *
     * @return the expiration date
     */
    public String getExpirationDate() {
        return expireDate;
    }

    /**
     * Sets the card expiration date.
     *
     * @param expDate the expiration date
     */
    public void setExpirationDate(final String expDate) {
        expireDate = expDate;
    }
}

