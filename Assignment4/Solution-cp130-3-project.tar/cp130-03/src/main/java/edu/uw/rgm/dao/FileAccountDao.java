package edu.uw.rgm.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;
import edu.uw.ext.framework.dao.AccountDao;

/**
 * An AccountDao that persists the account information in a file.
 *
 * @author Russ Moul
 */
public final class FileAccountDao implements AccountDao {

    /** The logger for this class */
    private static final Logger logger = LoggerFactory.getLogger(FileAccountDao.class);

    /** The name of the file holding the account data */
    private static final String ACCOUNT_FILENAME = "account.dat";

    /** The name of the file holding the address data */
    private static final String ADDRESS_FILENAME = "address.properties";

    /** The name of the file holding the credit card data */
    private static final String CREDITCARD_FILENAME = "creditcard.txt";
    
    /** The accounts directory. */
    private final File accountsDir = new File("target", "accounts");

    /**
     * Creates an instance of this class and loads the account data.
     *
     * @throws AccountException if an error occurs during the load operation
     */
    public FileAccountDao() throws AccountException {
    }

    /**
     * Lookup an account in the HashMap based on username.
     *
     * @param accountName the name of the desired account
     *
     * @return the account if located otherwise null
     */
    public Account getAccount(final String accountName) {
        Account account = null;
        FileInputStream in = null;
        final File accountDir = new File(accountsDir, accountName);

        if (accountDir.exists() && accountDir.isDirectory()) {
            try {

                File inFile = new File(accountDir, ACCOUNT_FILENAME);
                in = new FileInputStream(inFile);
                account = AccountSer.read(in);
                in.close();

                inFile = new File(accountDir, ADDRESS_FILENAME);
                if (inFile.exists()) {
                    in = new FileInputStream(inFile);
                    final Address address = AddressSer.read(in);
                    in.close();
                    account.setAddress(address);
                }

                inFile = new File(accountDir, CREDITCARD_FILENAME);
                if (inFile.exists()) {
                    in = new FileInputStream(inFile);
                    final CreditCard creditCard = CreditCardSer.read(in);
                    in.close();
                    account.setCreditCard(creditCard);
                }
            } catch (final IOException ex) {
                logger.warn(String.format("Unable to access or read account data, '%s'", accountName),
                            ex);
            } catch (final AccountException ex) {
                logger.warn(String.format("Unable to process account files for account, '%s'", accountName),
                            ex);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        logger.warn("Attempt to close stream failed.", e);
                    }
                }

            }
        }
        return account;
    }

    /**
     * Adds or updates an account.
     *
     * @param account the account to add/update
     *
     * @exception AccountException if operation fails
     */
    public void setAccount(final Account account) throws AccountException {
        FileOutputStream out = null;
        try {
            final File accountDir = new File(accountsDir, account.getName());

            final Address address = account.getAddress();
            final CreditCard card = account.getCreditCard();

            deleteFile(accountDir);
            if (!accountDir.exists()) {
                final boolean success = accountDir.mkdirs();
                if (!success) {
                    throw new AccountException(
                            String.format("Unable to create account diretory, %s", accountDir.getAbsolutePath()));
                }
            }

            File outFile = new File(accountDir, ACCOUNT_FILENAME);
            out = new FileOutputStream(outFile);
            AccountSer.write(out, account);
            out.close();

            if (address != null) {
                outFile = new File(accountDir, ADDRESS_FILENAME);
                out = new FileOutputStream(outFile);
                AddressSer.write(out, address);
                out.close();
            }

            if (card != null) {
                outFile = new File(accountDir, CREDITCARD_FILENAME);
                out = new FileOutputStream(outFile);
                CreditCardSer.write(out, card);
                out.close();
            }
        } catch (final IOException ex) {
            throw new AccountException("Unable to store account(s).", ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.warn("Attempt to close stream failed.", e);
                }
            }
        }
    }

    /**
     * Remove the account.
     *
     * @param accountName the name of the account to remove
     *
     * @exception AccountException if operation fails
     */
    public void deleteAccount(final String accountName)
        throws AccountException {
        deleteFile(new File(accountsDir, accountName));
    }

    /**
     * Remove all accounts.  This is primarily available to facilitate testing.
     *
     * @exception AccountException if operation fails
     */
    public void reset() throws AccountException {
        deleteFile(accountsDir);
    }

    /**
     * Utility method to delete a file or directory.  Recursively delete
     * directory contents and then the directory itself.
     *
     * @param file the account file or directory to delete
     */
    private void deleteFile(final File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                final File[] files = file.listFiles();
                for (File currFile : files) {
                    deleteFile(currFile);
                }
            }

            if (!file.delete()) {
                logger.warn(String.format("File deletion failed, %s", file.getAbsolutePath()));
           }
        }
    }


    /**
     * Close the DAO.
     */
    public void close() {
        // no-op
    }
}

