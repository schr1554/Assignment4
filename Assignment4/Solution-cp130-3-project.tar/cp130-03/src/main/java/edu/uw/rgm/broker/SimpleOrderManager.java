package edu.uw.rgm.broker;

import java.util.Comparator;
import java.util.function.Consumer;

import edu.uw.ext.framework.broker.OrderManager;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;

/**
 * Maintains queues to different types of orders and requests the execution of
 * orders when price conditions allow their execution.
 *
 * @author Russ Moul
 */
public class SimpleOrderManager implements OrderManager {
    /** The symbol of the stock this order manager is for */
    private String stockTickerSymbol;

    /** Queue for stop buy orders */
    protected OrderQueue<Integer, StopBuyOrder> stopBuyOrderQueue;

    /** Queue for stop sell orders */
    protected OrderQueue<Integer, StopSellOrder> stopSellOrderQueue;


    /**
     * Constructor.  Constructor to be used by sub classes to finish initialization.
     * 
     * @param stockTickerSymbol the ticker symbol of the stock this instance is
     *                          manage orders for
     */
    protected SimpleOrderManager(final String stockTickerSymbol) {
        this.stockTickerSymbol = stockTickerSymbol;
    }

    
    /**
     * Constructor.
     *
     * @param stockTickerSymbol the ticker symbol of the stock this instance is
     *                          manage orders for
     * @param price the current price of stock to be managed
     */
    public SimpleOrderManager(final String stockTickerSymbol, final int price) {
        this(stockTickerSymbol);
        // Create the stop buy order queue and associated pieces
        stopBuyOrderQueue = 
                new SimpleOrderQueue<>(price,
                                       (t, o) -> o.getPrice() <= t,
                                       Comparator.comparing(StopBuyOrder::getPrice)
                                                 .thenComparing(StopBuyOrder::compareTo));
        // Create the stop sell order queue ...
        stopSellOrderQueue =
            new SimpleOrderQueue<>(price,
                                   (t, o) -> o.getPrice() >= t,
                                   Comparator.comparing(StopSellOrder::getPrice)
                                             .reversed()
                                             .thenComparing(StopSellOrder::compareTo));
    }

    /**
     * Gets the stock ticker symbol for the stock managed by this stock manager.
     *
     * @return the stock ticker symbol
     */
    public final String getSymbol() {
        return stockTickerSymbol;
    }

    /**
     * Respond to a stock price adjustment by setting threshold on dispatch
     * filters.
     *
     * @param price the new price
     */
    public final void adjustPrice(final int price) {
        stopBuyOrderQueue.setThreshold(price);
        stopSellOrderQueue.setThreshold(price);
    }

    /**
     * Queue a stop buy order.
     *
     * @param order the order to be queued
     */
    public final void queueOrder(final StopBuyOrder order) {
        stopBuyOrderQueue.enqueue(order);
    }

    /**
     * Queue a stop sell order.
     *
     * @param order the order to be queued
     */
    public final void queueOrder(final StopSellOrder order) {
        stopSellOrderQueue.enqueue(order);
    }

    /**
     * Registers the processor to be used during buy order processing.  This will be
     * passed on to the order queues as the dispatch callback.
     *
     * @param processor the callback to be registered
     */
    public final void setBuyOrderProcessor(final Consumer<StopBuyOrder> processor) {
        stopBuyOrderQueue.setOrderProcessor(processor);
    }

    /**
     * Registers the processor to be used during sell order processing.  This will be
     * passed on to the order queues as the dispatch callback.
     *
     * @param processor the callback to be registered
     */
    public final void setSellOrderProcessor(final Consumer<StopSellOrder> processor) {
        stopSellOrderQueue.setOrderProcessor(processor);
    }
}

