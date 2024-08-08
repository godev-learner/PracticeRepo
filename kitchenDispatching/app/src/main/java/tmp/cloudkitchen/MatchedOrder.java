package tmp.cloudkitchen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchedOrder extends OrderDispatching {

    protected static Logger logger = LogManager.getLogger(MatchedOrder.class);

    public void dispatch(List<Order> lstOfOrders, List<Long> lstOfCourierPrepDuraion) throws InterruptedException {

        final int totalSize = lstOfOrders.size();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(16);

        CountDownLatch latch = new CountDownLatch(totalSize);

        AtomicInteger orderIdx = new AtomicInteger(0);
        executor.scheduleAtFixedRate(() -> {
            int idx = orderIdx.getAndIncrement();

            Order order = lstOfOrders.get(idx);
            Courier courier = new Courier(order, lstOfCourierPrepDuraion.get(idx));

            order.orderReceivedTime = Instant.now().toEpochMilli();

            executor.schedule(() -> {
                synchronized (order) {
                    order.foodReadyTime = Instant.now().toEpochMilli();
                    if (order.pair.arriveTime > 0) {
                        pairMatch(order, latch);
                    }
                }
            }, order.prepTime * 1000, TimeUnit.MILLISECONDS);


            executor.schedule(() -> {
                synchronized (courier) {
                    courier.arriveTime = Instant.now().toEpochMilli();
                    if (courier.pair.foodReadyTime > 0) {
                        pairMatch(courier.pair, latch);
                    }
                }
            }, courier.prepDuration, TimeUnit.MILLISECONDS);
        }, 1, 500, TimeUnit.MILLISECONDS);

        latch.await();
        executor.shutdown();
    }

    private void pairMatch(Order order, CountDownLatch latch) {
        long pickUpTime = Instant.now().toEpochMilli();

        Courier courier = order.pair;
        courier.pickUp(order, pickUpTime);
        statsAccumulate( order );
        latch.countDown();
        logger.debug("count:{}>>courier:{},pickup order:{}", latch.getCount(), courier, order);
    }

}
