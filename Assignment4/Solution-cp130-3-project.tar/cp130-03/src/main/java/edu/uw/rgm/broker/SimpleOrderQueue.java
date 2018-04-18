package edu.uw.rgm.broker;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;

/**
 * A simple OrderQueue implementation backed by a TreeSet.
 *
 * @param <T> the dispatch threshold type
 * @param <E> the type of order contained in the queue
 *
 * @author Russ Moul
 */
public final class SimpleOrderQueue<T, E extends Order>
                                         implements OrderQueue<T, E> {
    /** The queue data structure */
    private TreeSet<E> queue;

    //  BiPredicate interface is used in place of DispatchFilter.
    /** The filter used to determine if an order is dispatchable */
    private BiPredicate<T, E> filter;

    /** Order processor used to process dispatchable orders */
    private Consumer<E> orderProcessor;
    
    /** The current threshold. */
    private T threshold;

    /**
     * Constructor.
     *
     * @param threshold the initial threshold
     * @param filter the dispatch filter used to control dispatching from this
     *               queue
     */
    public SimpleOrderQueue(final T threshold, final BiPredicate<T, E> filter) {
        queue = new TreeSet<>();
        this.threshold = threshold;
        this.filter = filter;
    }

    /**
     * Constructor.
     *
     * @param threshold the initial threshold
     * @param filter the dispatch filter used to control dispatching from this
     *               queue
     * @param cmp Comparator to be used for ordering
     */
    public SimpleOrderQueue(final T threshold,
                            final BiPredicate<T, E> filter,
                            final Comparator<E> cmp) {
        queue = new TreeSet<>(cmp);
        this.threshold = threshold;
        this.filter = filter;
    }

    /**
     * Adds the specified order to the queue.  Subsequent to adding the order
     * dispatches any dispatchable orders.
     *
     * @param order the order to be added to the queue
     */
    @Override
    public void enqueue(final E order) {
        queue.add(order);
        dispatchOrders();
    }

    /**
     * Removes the highest dispatchable order in the queue. If there are orders
     * in the queue but they do not meet the dispatch threshold order will not
     * be removed and null will be returned.
     *
     * @return the first dispatchable order in the queue, or null if there are no
     *         dispatchable orders in the queue
     */
    @Override
    public E dequeue() {
        E order = null;

        if (!queue.isEmpty()) {
            order = queue.first();

            if (filter.test(threshold, order)) {
                queue.remove(order);
            } else {
                order = null;
            }
        }

        return order;
    }

    /**
     * Executes the callback for each dispatchable order.  Each dispatchable
     * order is in turn removed from the queue and passed to the callback.  If
     * no callback is registered the order is simply removed from the queue.
     */
    @Override
    public void dispatchOrders() {
        E order;

        while ((order = dequeue()) != null) {
            if (orderProcessor != null) {
                orderProcessor.accept(order);
            }
        }
    }

    /**
     * Registers the callback to be used during order processing.
     *
     * @param proc the callback to be registered
     */
    @Override
    public void setOrderProcessor(final Consumer<E> proc) {
        orderProcessor = proc;
    }
    
    /**
     * Adjusts the threshold and dispatches orders.
     *
     * @param threshold - the new threshold
     */
    @Override
    public final void setThreshold(final T threshold) {
        this.threshold = threshold;
        dispatchOrders();
    }
    
    /**
     * Obtains the current threshold value.
     *
     * @return the current threshold
     */
    @Override
   public final T getThreshold() {
        return threshold;
    }

}

