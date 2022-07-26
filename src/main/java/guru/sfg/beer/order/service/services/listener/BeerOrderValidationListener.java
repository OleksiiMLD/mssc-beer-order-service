package guru.sfg.beer.order.service.services.listener;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.ValidateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Created by okostetskyi on 22.07.2022
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderValidationListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESULT_QUEUE)
    public void listenBeerOrderValidationResult(@Payload ValidateBeerOrderResult validateBeerOrderResult,
                                                @Headers MessageHeaders headers) {
        log.info("Order validation received: ");
        if (validateBeerOrderResult.isValid()) {
            log.info("\tOrder approved.");
            beerOrderManager.approveBeerOrder(validateBeerOrderResult.getOrderId());
        } else {
            log.info("\tOrder rejected.");
            beerOrderManager.rejectBeerOrder(validateBeerOrderResult.getOrderId());
        }
    }
}
