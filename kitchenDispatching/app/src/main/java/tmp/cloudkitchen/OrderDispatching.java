package tmp.cloudkitchen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public abstract class OrderDispatching {


    public static final class Order {
        public String id;
        public String name;
        public long prepTime;

        public Courier pair;
        public Courier courier;

        public volatile long orderReceivedTime = -1L;
        public volatile long foodReadyTime = -1L;
        public volatile long foodWaitDuration = -1L;

        /**
         * difference between order prepare time in math and reality
         */
        public Long orderDelta = null;
        /**
         * difference between courier prepare time in math and reality
         */
        public Long courierDelta = null;

        @Override
        public String toString() {

            return "Order{" +
                    "orderDelta: " + orderDelta + ", courierDelta:" + courierDelta +
                    ">>id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", prepTime=" + prepTime +
                    ", pair=" + pair +
                    ", courier=" + courier +
                    ", orderReceivedTime=" + orderReceivedTime +
                    ", foodReadyTime=" + foodReadyTime +
                    ", foodWaitDuration=" + foodWaitDuration +
                    '}';
        }
    }

    public static final class Courier {
        public Courier(Order pair, long prepDuration) {
            this.pair = pair;
            this.pair.pair = this;
            this.prepDuration = prepDuration;
        }

        public Order pair;
        public long prepDuration;

        public Order order;
        public volatile long arriveTime = -1L;
        public volatile long waitDuration = -1L;
        public volatile long pickUpTime = -1L;


        public void pickUp(Order order, long pickUpTime) {
            this.order = order;
            this.pickUpTime = pickUpTime;
            this.waitDuration = this.pickUpTime - this.arriveTime;

            this.order.courier = this;
            this.order.foodWaitDuration = this.pickUpTime - this.order.foodReadyTime;

            this.order.orderDelta = this.order.foodReadyTime - this.order.orderReceivedTime - this.order.prepTime * 1000;
            this.order.courierDelta = this.arriveTime - this.pair.orderReceivedTime - this.prepDuration;
        }

        @Override
        public String toString() {
            return "Courier{" +
                    "pair=" + pair.id +
                    ", prepDuration=" + prepDuration +
                    (order == null ? "" : ", order=" + order.id) +
                    ", arriveTime=" + arriveTime +
                    ", waitDuration=" + waitDuration +
                    ", pickUpTime=" + pickUpTime +
                    '}';
        }
    }

    protected static Logger logger = LogManager.getLogger(OrderDispatching.class);

    abstract void dispatch(List<Order> l, List<Long> lstOfCourierPrepDuraion) throws InterruptedException;


    private long foodWaitAccum;
    private long courierWaitAccum;
    private int orderAccum;

    public long getFoodWaitAccum() {
        return foodWaitAccum;
    }

    public long getCourierWaitAccum() {
        return courierWaitAccum;
    }

    public int getOrderAccum() {
        return orderAccum;
    }

    protected synchronized void statsAccumulate(Order order) {
        orderAccum++;
        foodWaitAccum += order.foodWaitDuration;
        courierWaitAccum += order.courier.waitDuration;

        logger.info("{} orders: newOrder:{}", orderAccum, order);
    }

    public void printResult() {
        logger.info(" Result: {}.dispatching:avg food wait milliseconds:{}, avg courier wait milliseconds:{}", this, foodWaitAccum, courierWaitAccum);
    }

}
