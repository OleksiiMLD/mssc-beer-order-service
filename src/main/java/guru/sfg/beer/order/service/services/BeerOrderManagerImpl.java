package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.sm.BeerOrderStateChangeInterceptor;
import guru.sfg.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by jt on 11/29/19.
 */
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);

        if(isValid){
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

            BeerOrder validatedOrder = beerOrderRepository.findOneById(beerOrderId);

            sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);

        } else {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
        }
    }

    @Override
    public void processAllocationFailure(UUID beerId) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerId);
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
    }

    @Transactional
    @Override
    public void processAllocationResult(BeerOrderDto beerOrderDto, Boolean isPendingInventory) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());

        BeerOrderEventEnum event = Boolean.TRUE.equals(isPendingInventory) ?
                BeerOrderEventEnum.ALLOCATION_NO_INVENTORY : BeerOrderEventEnum.ALLOCATION_SUCCESS;
        sendBeerOrderEvent(beerOrder, event);
        updateAllocatedQuantity(beerOrderDto);
    }

    private void updateAllocatedQuantity(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());

        beerOrder.getBeerOrderLines().forEach(
                line -> line.setQuantityAllocated(beerOrderDto.getBeerOrderLines().stream()
                        .filter(dtoLine -> dtoLine.getId().equals(line.getId()))
                        .findAny().orElseThrow(() -> new IllegalArgumentException("BeerOrderLine with id: " + line.getId() +
                                " for Order with id: " + beerOrder.getId() + " not processed."))
                        .getOrderQuantity())
        );

        beerOrderRepository.saveAndFlush(beerOrder);
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum){
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);

        Message msg = MessageBuilder.withPayload(eventEnum)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString())
                .build();

        sm.sendEvent(msg);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder){
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
                });

        sm.start();

        return sm;
    }
}
