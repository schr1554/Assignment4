package main.java.edu.uw.ajs.broker;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;

/**
 * A simple OrderQueue implementation backed by a TreeSet.
 *
 * @param <T>
 *            the dispatch threshold type
 * @param <E>
 *            the type of order contained in the queue
 *
 * @author Russ Moul
 */
public final class SimpleOrderQueue<T, E extends Order> implements OrderQueue<T, E>, Runnable {

	/** This class' logger */
	private static final Logger logger = LoggerFactory.getLogger(SimpleBroker.class);

	/** The queue data structure */
	private TreeSet<E> queue;

	// BiPredicate interface is used in place of DispatchFilter.
	/** The filter used to determine if an order is dispatchable */
	private BiPredicate<T, E> filter;

	/** Order processor used to process dispatchable orders */
	private Consumer<E> orderProcessor;

	/** The current threshold. */
	private T threshold;

	// Added these three objects
	private Thread orderdispatchThread;

	private final ReentrantLock queueLock = new ReentrantLock();

	private final ReentrantLock processorLock = new ReentrantLock();

	private final Condition dispatchCondition = queueLock.newCondition();

	private final String name;

	/**
	 * Constructor.
	 *
	 * @param threshold
	 *            the initial threshold
	 * @param filter
	 *            the dispatch filter used to control dispatching from this
	 *            queue
	 */
	public SimpleOrderQueue(final String name, final T threshold, final BiPredicate<T, E> filter) {
		queue = new TreeSet<>();
		this.threshold = threshold;
		this.filter = filter;
		this.name = name;
		startDispatchThread();

	}

	// start dispatch thread

	// new thread this, name _ "OrderDispatchThread

	// dispatchThread.setDaemon(true);

	// dispatchThread.start()

	private void startDispatchThread() {

		this.orderdispatchThread = new Thread(this);
		orderdispatchThread.setDaemon(true);
		orderdispatchThread.setName(name);
		orderdispatchThread.start();
		logger.info("Launching thread: " + orderdispatchThread.getName());

	}

	/**
	 * Constructor.
	 *
	 * @param threshold
	 *            the initial threshold
	 * @param filter
	 *            the dispatch filter used to control dispatching from this
	 *            queue
	 * @param cmp
	 *            Comparator to be used for ordering
	 */
	public SimpleOrderQueue(final String name, final T threshold, final BiPredicate<T, E> filter,
			final Comparator<E> cmp) {
		queue = new TreeSet<>(cmp);
		this.threshold = threshold;
		this.filter = filter;
		this.name = name;

		startDispatchThread();

	}

	/**
	 * Adds the specified order to the queue. Subsequent to adding the order
	 * dispatches any dispatchable orders.
	 *
	 * @param order
	 *            the order to be added to the queue
	 */
	@Override
	public void enqueue(final E order) {

		try {
			queueLock.lock();
			queue.add(order);
			dispatchOrders();

		} finally {
			queueLock.unlock();
		}

	}

	/**
	 * Removes the highest dispatchable order in the queue. If there are orders
	 * in the queue but they do not meet the dispatch threshold order will not
	 * be removed and null will be returned.
	 *
	 * @return the first dispatchable order in the queue, or null if there are
	 *         no dispatchable orders in the queue
	 */
	@Override
	public E dequeue() {
		E order = null;

		try {
			queueLock.lock();

			if (!queue.isEmpty()) {
				order = queue.first();

				if (filter.test(threshold, order)) {
					queue.remove(order);
				} else {
					order = null;
				}
			}
		} finally {
			queueLock.unlock();
		}

		return order;
	}

	/**
	 * Executes the callback for each dispatchable order. Each dispatchable
	 * order is in turn removed from the queue and passed to the callback. If no
	 * callback is registered the order is simply removed from the queue.
	 */
	@Override
	public void dispatchOrders() {

		try {
			queueLock.lock();
			dispatchCondition.signal();

			E order;

			while ((order = dequeue()) != null) {
				if (orderProcessor != null) {

					final E threadOrder = order;

					orderProcessor.accept(threadOrder);

				}
			}

		} finally {
			queueLock.unlock();
		}

		// unlock

	}

	/**
	 * Registers the callback to be used during order processing.
	 *
	 * @param proc
	 *            the callback to be registered
	 */
	@Override
	public void setOrderProcessor(final Consumer<E> proc) {

		processorLock.lock();
		orderProcessor = proc;
	}

	/**
	 * Adjusts the threshold and dispatches orders.
	 *
	 * @param threshold
	 *            - the new threshold
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

	@Override
	public void run() {

		// move the old dispatch order here:

		// We are going to lock the queue then go into an infinite while loop

		// E order

		E order;

		try {

			queueLock.lock();
			while ((order = dequeue()) == null) {

				try {
					dispatchCondition.await();
				} catch (final InterruptedException iex) {
				}
				logger.info("InterruptedException");
			}
		}

		finally

		{
			queueLock.unlock();
		}

		processorLock.lock();

		try {
			if (orderProcessor != null) {
				orderProcessor.accept(order);
			}
		} finally {
			processorLock.unlock();
		}
	}

}
