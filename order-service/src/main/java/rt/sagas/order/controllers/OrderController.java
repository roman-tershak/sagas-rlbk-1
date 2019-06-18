package rt.sagas.order.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import rt.sagas.order.entities.Order;
import rt.sagas.order.services.OrderService;

import javax.transaction.Transactional;
import javax.validation.Valid;

@RestController("/orders")
public class OrderController {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private OrderService orderService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Transactional
    public Order create(@RequestBody @Valid Order order) throws Exception {

        LOGGER.info("Create Order called: {}", order);

        Order orderCreated = orderService.createOrder(order);

        return orderCreated;
    }

}
