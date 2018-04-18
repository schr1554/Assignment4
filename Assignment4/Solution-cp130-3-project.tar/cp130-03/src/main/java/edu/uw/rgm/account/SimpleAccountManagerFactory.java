package edu.uw.rgm.account;

import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.account.AccountManagerFactory;
import edu.uw.ext.framework.dao.AccountDao;


/**
 * A simple implementation of the AccountManagerFactory that instantiates the
 * SimpleAccountManager.
 *
 * @author Russ Moul
 */
public final class SimpleAccountManagerFactory implements AccountManagerFactory
{
    /**
     * Instantiates a new SimpleAccountManager instance.
     *
     * @param dao the data access object to be used by the account manager
     *
     * @return a newly instantiated SimpleAccountManager
     */
    public AccountManager newAccountManager(final AccountDao dao) {
        return new SimpleAccountManager(dao);
    }
}

