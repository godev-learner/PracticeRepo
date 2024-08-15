package tmp.cloudkitchen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FIFOOrder extends OrderDispatching {

    protected static Logger logger = LogManager.getLogger(FIFOOrder.class);

    public void dispatch(List<Order> lstOfOrders, List<Long> lstOfCourierPrepDuraion) throws InterruptedException {
        LinkedBlockingQueue<Order> ordersQueue = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Courier> couriersQueue = new LinkedBlockingQueue<>();

        final int numOfOrders = lstOfOrders.size();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(16);

        AtomicInteger orderIdx = new AtomicInteger(0);
        ScheduledFuture<?> task = executor.scheduleAtFixedRate(() -> {
            int idx = orderIdx.getAndIncrement();
            Order order = lstOfOrders.get(idx);
            Courier courier = new Courier(order, lstOfCourierPrepDuraion.get(idx));

            order.orderReceivedTime = Instant.now().toEpochMilli();

            executor.schedule(() -> {
                order.foodReadyTime = Instant.now().toEpochMilli();
                ordersQueue.offer(order);
            }, order.prepTime * 1000, TimeUnit.MILLISECONDS);


            executor.schedule(() -> {
                courier.arriveTime = Instant.now().toEpochMilli();
                couriersQueue.offer(courier);
            }, courier.prepDuration, TimeUnit.MILLISECONDS);
        }, 1, 500, TimeUnit.MILLISECONDS);

        int counter = 0;
        while (true) {
            Order order = ordersQueue.poll(999, TimeUnit.DAYS);
            Courier courier = couriersQueue.poll(999, TimeUnit.DAYS);
            long pickUpTime = Instant.now().toEpochMilli();

            courier.pickUp(order, pickUpTime);
            statsAccumulate(order);
            counter++;
            logger.debug("count:{}/{}>>courier:{},pickup order:{}, isdone:{}", counter, numOfOrders, courier, order, task.isDone());
            if (counter == numOfOrders) {
                break;
            }
        }
        executor.shutdown();
    }

}
