package edu.uw.rgm.dao;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;
import edu.uw.ext.framework.dao.AccountDao;
import edu.uw.rgm.account.SimpleAccount;
import edu.uw.rgm.account.SimpleAddress;
import edu.uw.rgm.account.SimpleCreditCard;

import static java.lang.String.format;

/**
 * An AccountDao that persists the account information using JSON.
 *
 * @author Russ Moul
 */
public final class JsonAccountDao implements AccountDao {
    /** This class' logger. */
    private static final Logger log = 
                         LoggerFactory.getLogger(JsonAccountDao.class);

    /** The name of the file holding the account data */
    private static final String ACCOUNT_FILENAME_PAT = "%s.json";

    /** JSON serializer. */
    private final ObjectMapper mapper;
    
    /** The accounts directory. */
    private final File accountsDir = new File("target", "accounts");

    /**
     * Creates an instance of this class and loads the account data.
     *
     * @throws AccountException if an error occurs during the load operation
     */
    public JsonAccountDao() throws AccountException {
        // Map interfaces to implementation classes
        final SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(Account.class, SimpleAccount.class);
        module.addAbstractTypeMapping(Address.class, SimpleAddress.class);
        module.addAbstractTypeMapping(CreditCard.class, SimpleCreditCard.class);
        mapper = new ObjectMapper();
        mapper.registerModule(module);

        // An alternative approach that encodes class names in the output
        //mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    /**
     * Lookup an account based on username.
     *
     * @param accountName the name of the desired account
     *
     * @return the account if located otherwise null
     */
    @Override
    public Account getAccount(final String accountName) {
        Account account = null;
        String acctFileName = format(ACCOUNT_FILENAME_PAT, accountName);

        if (accountsDir.exists() && accountsDir.isDirectory()) {
            try {
                final File inFile = new File(accountsDir, acctFileName);
                account = mapper.readValue(inFile, Account.class);
            } catch (final IOException ex) {
                log.warn("Unable to access or read account data, '" + accountName + "'", ex);
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
    @Override
    public void setAccount(final Account account) throws AccountException {
        try {
            String acctFileName = format(ACCOUNT_FILENAME_PAT, account.getName());
            final File outFile = new File(accountsDir, acctFileName);
            if (!accountsDir.exists()) {
                final boolean success = accountsDir.mkdirs();
                if (!success) {
                    throw new AccountException("Unable to create account diretory, "
                                             + accountsDir.getAbsolutePath());
                }
            }
            
            if (outFile.exists()) {
                boolean deleted = outFile.delete();
                if (!deleted) {
                    log.warn(format("Unable to delete account file, %s, overwriting.",
                    accountsDir.getAbsolutePath()));
                }

            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(outFile, account);

        } catch (final IOException ex) {
            throw new AccountException("Unable to store account(s).", ex);
        }
    }

    /**
     * Remove the account.
     *
     * @param accountName the name of the account to remove
     *
     * @exception AccountException if operation fails
     */
    @Override
    public void deleteAccount(final String accountName)
        throws AccountException {
        String acctFileName = format(ACCOUNT_FILENAME_PAT, accountName);
        File acctFile = new File(accountsDir, acctFileName);
        if (acctFile.exists() && !acctFile.delete()) {
            log.warn("File deletion failed, " + acctFile.getAbsolutePath());
        }
    }

    /**
     * Remove all accounts.  This is primarily available to facilitate testing.
     *
     * @exception AccountException if operation fails
     */
    @Override
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
                for (final File currFile : files) {
                    deleteFile(currFile);
                }
            }

            if (!file.delete()) {
                log.warn("File deletion failed, " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Close the DAO.
     */
    @Override
    public void close() {
        // no-op
    }
}

