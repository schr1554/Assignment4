package edu.uw.rgm.broker;

import java.util.HashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.Broker;
import edu.uw.ext.framework.broker.BrokerException;
import edu.uw.ext.framework.broker.OrderManager;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.exchange.ExchangeEvent;
import edu.uw.ext.framework.exchange.ExchangeListener;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.exchange.StockQuote;
import edu.uw.ext.framework.order.MarketBuyOrder;
import edu.uw.ext.framework.order.MarketSellOrder;
import edu.uw.ext.framework.order.Order;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;

/**
 * An implementation of the Broker interface, provides a full
 * implementation less the creation of the order manager and market queue.
 *
 * @author Russ Moul
 */
public class SimpleBroker implements Broker, ExchangeListener {
    /** This class' logger */
    private static final Logger logger =
                         LoggerFactory.getLogger(SimpleBroker.class);

    /** This broker's name */
    private String name;

    /** The brokers account manager */
    private AccountManager accountManager;

    /** The exchange used by this broker */
    private StockExchange stockExchange;

    /** The set of order managers used by the broker */
    private HashMap<String, OrderManager> orderManagerMap;

    /** The market order queue. */
    protected OrderQueue<Boolean, Order> marketOrders;

    /**
     * Constructor for sub classes
     *
     * @param brokerName name of the broker
     * @param exchg the stock exchange to be used by the broker
     * @param acctMgr the account manager to be used by the broker
     */
    protected SimpleBroker(final String brokerName,
                           final StockExchange exchg,
                           final AccountManager acctMgr) {
        name = brokerName;
        accountManager = acctMgr;
        stockExchange = exchg;
    }

    /**
     *  Constructor.
     *
     * @param brokerName name of the broker
     * @param acctMgr the account manager to be used by the broker
     * @param exchg the stock exchange to be used by the broker
     */
    public SimpleBroker(final String brokerName, final AccountManager acctMgr,
                        final StockExchange exchg) {
        this(brokerName, exchg, acctMgr);

        // Create the market order queue, & order processor
        marketOrders = new SimpleOrderQueue<>(exchg.isOpen(), (t, o)->t);
        Consumer<Order> stockTrader = (order) -> {
            logger.info(String.format("Executing - %s", order));
            final int sharePrice = stockExchange.executeTrade(order);
            try {
                final Account acct = accountManager.getAccount(order.getAccountId());
                acct.reflectOrder(order, sharePrice);
                logger.info(String.format("New balance - %d", acct.getBalance()));

            } catch (final AccountException ex) {
                logger.error(String.format("Unable to update account, %s", order.getAccountId()), ex);
            }
        };

        marketOrders.setOrderProcessor(stockTrader);

        // Create the order managers
        initializeOrderManagers();

        exchg.addExchangeListener(this);
    }
    
    /**
     * Fetch the stock list from the exchange and initialize an order manager
     * for each stock.  Only to be used during construction.
     */
    protected final void initializeOrderManagers() {
        orderManagerMap = new HashMap<>();
        final Consumer<StopBuyOrder> moveBuy2MarketProc = (StopBuyOrder order) -> marketOrders.enqueue(order);
        final Consumer<StopSellOrder> moveSell2MarketProc = (StopSellOrder order) -> marketOrders.enqueue(order);
        for (String ticker : stockExchange.getTickers()) {
            final int currPrice = stockExchange.getQuote(ticker).getPrice();
            final OrderManager orderMgr = createOrderManager(ticker, currPrice);
            orderMgr.setBuyOrderProcessor(moveBuy2MarketProc);
            orderMgr.setSellOrderProcessor(moveSell2MarketProc);
            orderManagerMap.put(ticker, orderMgr);
            if (logger.isInfoEnabled()) {
                logger.info(String.format("Initialized order manager for '%s' @ %d",
                                          ticker, currPrice));
            }
        }
    }
    
    /**
     * Create an appropriate order manager for this broker.  Only to be used during construction.
     *
     * @param ticker the ticker symbol of the stock
     * @param initialPrice current price of the stock
     *
     * @return a new OrderManager for the specified stock
     */
    protected OrderManager createOrderManager(final String ticker, final int initialPrice) {
        return new SimpleOrderManager(ticker, initialPrice);
    }

   /**
    * Upon the exchange opening sets the market dispatch filter threshold
    * and processes any available orders.
    *
    * @param event the price change event
    */
    public synchronized final void priceChanged(final ExchangeEvent event) {
        checkInvariants();
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Processing price change [%s:%d]",
                                      event.getTicker(), event.getPrice()));
        }
        OrderManager orderMgr;
        orderMgr = orderManagerMap.get(event.getTicker());
        if (orderMgr != null) {
            orderMgr.adjustPrice(event.getPrice());
        }
    }

    /**
     * Upon the exchange opening sets the market dispatch filter threshold
     * and processes any available orders.
     *
     * @param event the exchange (open) event
     */
    public synchronized final void exchangeOpened(final ExchangeEvent event) {
        checkInvariants();
        logger.info("### MARKET OPENED ###");
        marketOrders.setThreshold(Boolean.TRUE);
    }

    /**
     * Upon the exchange opening sets the market dispatch filter threshold.
     *
     * @param event the exchange (closed) event
     */
    public synchronized final void exchangeClosed(final ExchangeEvent event) {
        checkInvariants();
        marketOrders.setThreshold(Boolean.FALSE);
        logger.info("### MARKET CLOSED ###");
    }

    /**
     * Get the name of the broker.
     *
     * @return the name of the broker
     */
    public synchronized final String getName() {
        checkInvariants();
        return name;
    }

    /**
     * Create an account with the broker.
     *
     * @param username the user or account name for the account
     * @param password the password for the new account
     * @param balance the initial account balance in cents
     *
     * @return the new account
     *
     * @exception BrokerException if unable to create account
     */
    public synchronized final Account createAccount(final String username,
                                       final String password, final int balance)
        throws BrokerException {
        checkInvariants();
        try {
            return accountManager.createAccount(username, password, balance);
        } catch (final AccountException ae) {
            throw new BrokerException("Unable to create account.", ae);
        }
    }

    /**
     * Delete an account with the broker.
     *
     * @param username the user or account name for the account
     *
     * @exception BrokerException if unable to delete account
     */
    public synchronized final void deleteAccount(final String username)
        throws BrokerException {
        checkInvariants();
        try {
            accountManager.deleteAccount(username);
        } catch (final AccountException ae) {
            throw new BrokerException("Unable to delete account.", ae);
        }
    }

    /**
     * Locate an account with the broker.  The username and password are first
     * verified and the account is returned.
     *
     * @param username the user or account name for the account
     * @param password the password for the new account
     *
     * @return the account
     *
     * @exception BrokerException username and password are invalid
     */
    public synchronized final Account getAccount(final String username,
                                                 final String password)
        throws BrokerException {
        checkInvariants();
        try {
            // First we check the password
            if (accountManager.validateLogin(username, password)) {
                // It's valid, so we'll return the account
                return accountManager.getAccount(username);
            } else {
                throw new BrokerException("Invalid username/password.");
            }
        } catch (final AccountException ae) {
            throw new BrokerException("Unable to access account.", ae);
        }
    }

    /**
     * Place an order with the broker.
     *
     * @param order the order being placed with the broker
     */
    public synchronized final void placeOrder(final MarketBuyOrder order) {
        checkInvariants();
        marketOrders.enqueue(order);
    }

    /**
     * Place an order with the broker.
     *
     * @param order the order being placed with the broker
     */
    public synchronized final void placeOrder(final MarketSellOrder order) {
        checkInvariants();
        marketOrders.enqueue(order);
    }

    /**
     * Lookup the order manager for this stock.
     *
     * @param ticker the stocks ticker symbol
     *
     * @return the order manager for the stock
     *
     * @exception BrokerException if unable to obtain the order manager
     */
    private synchronized OrderManager orderManagerLookup(final String ticker)
        throws BrokerException {
        final OrderManager orderMgr = orderManagerMap.get(ticker);

        if (orderMgr == null) {
            throw new BrokerException(String.format("Requested stock, '%s' does not exist", ticker));
        }

        return orderMgr;
    }

    /**
     * Place an order with the broker.
     *
     * @param order the order being placed with the broker
     *
     * @exception BrokerException if unable to place order
     */
    public synchronized final void placeOrder(final StopBuyOrder order)
        throws BrokerException {
        checkInvariants();
        orderManagerLookup(order.getStockTicker()).queueOrder(order);
    }

    /**
     * Place an order with the broker.
     *
     * @param order the order being placed with the broker
     *
     * @exception BrokerException if unable to place order
     */
    public synchronized final void placeOrder(final StopSellOrder order)
        throws BrokerException {
        checkInvariants();
        orderManagerLookup(order.getStockTicker()).queueOrder(order);
    }

    /**
     * Get a price quote for a stock.
     *
     * @param symbol the stocks ticker symbol
     *
     * @return the quote
     *
     * @exception BrokerException if unable to obtain quote
     */
    public synchronized final StockQuote requestQuote(final String symbol)
        throws BrokerException {
        checkInvariants();
        final StockQuote quote = stockExchange.getQuote(symbol);

        if (quote == null) {
            throw new BrokerException(String.format("Quote not available for '%s'.",symbol));
        }

        return quote;
    }

    /**
     * Release broker resources.
     *
     * @exception BrokerException if the operation fails
     */
    public synchronized void close() throws BrokerException {
        try {
            stockExchange.removeExchangeListener(this);
            accountManager.close();
            orderManagerMap = null;
        } catch (final AccountException ex) {
            throw new BrokerException(
                      "Attempt to close the broker failed.", ex);
        }
    }
    
    /**
     * Every public operation invokes this method to insure the broker is 
     * capable of performing operations.  If the broker is not in a valid
     * state an IllegalStateException is thrown.
     * 
     * @throws IllegalStateException if broker is n an invalid state
     */
    private void checkInvariants() {
        if (name == null ||
            accountManager == null ||
            stockExchange == null ||
            orderManagerMap == null ||
            marketOrders == null) {
            throw new IllegalStateException("Broker is not properly initialized, or has been closed.");
        }
    }
}


