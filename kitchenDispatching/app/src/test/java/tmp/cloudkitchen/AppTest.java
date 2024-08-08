package tmp.cloudkitchen;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


class AppTest {
    protected static Logger logger = LogManager.getLogger(AppTest.class);

    @Test
    void appHasAGreeting() throws IOException, InterruptedException {
        ObjectMapper objectMapper = new JsonMapper();
        TypeReference<List<OrderDispatching.Order>> type = new TypeReference<>() {
        };

        tmp.cloudkitchen.OrderDispatching dispatching = new tmp.cloudkitchen.FIFOOrder();
        logger.info("courierForAnyOrder>>>>>>>>>>>>>>>");
        List<tmp.cloudkitchen.OrderDispatching.Order> orders = objectMapper.readValue(App.class.getClassLoader().getResourceAsStream("data.json"), type);
        List<Long> courierPrepTimeLst = orders.stream().map(i -> 3000L + new Random().nextInt(12000)).collect(Collectors.toList());

        dispatching.dispatch(orders, courierPrepTimeLst);

        dispatching.printResult();

        orders.stream().forEach(o -> {
            Assertions.assertTrue(o.courierDelta < 10, "courier delta should be less than 10");
            Assertions.assertTrue(o.orderDelta < 10, "order delta should be less than 10");

            Assertions.assertEquals(o.foodWaitDuration, o.courier.pickUpTime - o.foodReadyTime, "Food wait duration calculation wrong");
            Assertions.assertEquals(o.courier.waitDuration, o.courier.pickUpTime - o.courier.arriveTime, "Courier wait duration calculation wrong");
            Assertions.assertNotNull(o.pair, "every order should have a paired courier");
            Assertions.assertEquals(o.pair.pair, o, "every order is its paired courier's pair");
            Assertions.assertEquals(o.courier.pair.getClass(), OrderDispatching.Order.class, "every order's courier should have a paired order");

        });


        dispatching = new MatchedOrder();
        orders = objectMapper.readValue(App.class.getClassLoader().getResourceAsStream("data.json"), type);

        dispatching.dispatch(orders, courierPrepTimeLst);
        dispatching.printResult();


        orders.stream().forEach(o -> {
            Assertions.assertTrue(o.courierDelta < 10, "courier delta should be less than 10");
            Assertions.assertTrue(o.orderDelta < 10, "order delta should be less than 10");

            Assertions.assertEquals(o.foodWaitDuration, o.courier.pickUpTime - o.foodReadyTime, "Food wait duration calculation wrong");
            Assertions.assertEquals(o.courier.waitDuration, o.courier.pickUpTime - o.courier.arriveTime, "Courier wait duration calculation wrong");
            Assertions.assertEquals(o.pair.pair, o, "every order is its paired courier's pair");
            Assertions.assertEquals(o.pair, o.courier, "every order pair is specific to pickup self");
            Assertions.assertEquals(o.courier.order, o, "order's courier's order should pick self");

        });
    }
}