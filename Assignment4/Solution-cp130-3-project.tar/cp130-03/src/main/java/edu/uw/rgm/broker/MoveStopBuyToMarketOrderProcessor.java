package edu.uw.rgm.broker;

import java.util.function.Consumer;

import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;
import edu.uw.ext.framework.order.StopBuyOrder;

public class MoveStopBuyToMarketOrderProcessor implements Consumer<StopBuyOrder> {
	private OrderQueue<Boolean, Order> marketQ;
	
	public MoveStopBuyToMarketOrderProcessor(OrderQueue<Boolean, Order> marketQ) {
		this.marketQ = marketQ;
	}
	
	@Override
	public void accept(StopBuyOrder order) {
		marketQ.enqueue(order);
	}

}
