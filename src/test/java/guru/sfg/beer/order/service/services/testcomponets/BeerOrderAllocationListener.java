package guru.sfg.beer.order.service.services.testcomponets;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManagerImplIT;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 2/16/20.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    public static final String INVENTORY_PENDING = "inventory-pending";
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message msg){
        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();
        boolean isFailed = BeerOrderManagerImplIT.FAIL_ALLOCATION.equals(request.getBeerOrderDto().getCustomerRef());

        request.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto -> {
            if (BeerOrderManagerImplIT.IN_DEMAND_UPC.equals(beerOrderLineDto.getUpc())) {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - 1);
                request.getBeerOrderDto().setOrderStatusCallbackUrl(INVENTORY_PENDING);
            } else {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
            }
        });

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                AllocateOrderResult.builder()
                .beerOrderDto(request.getBeerOrderDto())
                .pendingInventory(INVENTORY_PENDING.equals(request.getBeerOrderDto().getOrderStatusCallbackUrl()))
                .allocationError(isFailed)
                .build());
    }
}
