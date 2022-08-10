package guru.sfg.beer.order.service.services.testcomponets;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by okostetskyi on 10.08.2022
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAlocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message message) {
        System.out.println("======= Order Allocation listener =======");
//        AllocateOrderRequest request = (AllocateOrderRequest) message.getPayload();
//        final AllocateOrderResult.AllocateOrderResultBuilder builder = AllocateOrderResult.builder();
//        BeerOrderDto beerOrderDto = request.getBeerOrderDto();

        AllocateOrderRequest request = (AllocateOrderRequest) message.getPayload();

        request.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto -> {
            beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
        });


        AllocateOrderResult result = AllocateOrderResult.builder()
                .beerOrderDto(request.getBeerOrderDto())
                .allocationError(false)
                .pendingInventory(false)
                .build();

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE, result);
    }
}
