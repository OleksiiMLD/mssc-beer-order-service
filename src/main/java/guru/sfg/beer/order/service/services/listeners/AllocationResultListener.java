package guru.sfg.beer.order.service.services.listeners;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Created by okostetskyi on 29.07.2022
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESULT_QUEUE)
    public void listen(AllocateOrderResult result) {
        if (Boolean.TRUE.equals(result.isAllocationError())) {
            beerOrderManager.processAllocationFailure(result.getBeerOrderDto().getId());
        } else {
            beerOrderManager.processAllocationResult(result.getBeerOrderDto(), result.isPendingInventory());
        }
    }
}
