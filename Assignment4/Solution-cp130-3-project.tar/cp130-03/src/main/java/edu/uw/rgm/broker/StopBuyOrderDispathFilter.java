package edu.uw.rgm.broker;

import java.util.function.BiPredicate;
import edu.uw.ext.framework.order.StopBuyOrder;

public class StopBuyOrderDispathFilter implements BiPredicate<Integer, StopBuyOrder> {

	@Override
	public boolean test(Integer t, StopBuyOrder o) {
        return o.getPrice() <= t;
	}
}
