package guru.sfg.beer.order.service.sm.action;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.beer.BeerService;
import guru.sfg.beer.order.service.sm.StateMachineConstants;
import guru.sfg.beer.order.service.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by okostetskyi on 21.07.2022
 */
@Slf4j
@RequiredArgsConstructor
public class BeerOrderValidationAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerService beerService;
    private final BeerOrderRepository beerOrderRepository;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        log.info("Validating beer order...");
        UUID orderId = (UUID) stateContext.getMessageHeader(StateMachineConstants.ORDER_ID_HEADER);
        final BeerOrder beerOrder = beerOrderRepository.findOneById(orderId);

        boolean validated = beerOrder.getBeerOrderLines().stream().allMatch(line -> {
                    Optional<BeerDto> beerByUpc = beerService.getBeerByUpc(line.getUpc());
                    if (beerByUpc.isPresent()) {
                        BeerDto beerDto = beerByUpc.get();
                        return beerDto.getQuantityOnHand() >= line.getOrderQuantity();
                    } else {
                        return false;
                    }
                }
        );
        log.info("Order is {}", validated ? "validated" : "not validated");
        final BeerOrderEventEnum payload = validated ? BeerOrderEventEnum.VALIDATION_PASSED
                : BeerOrderEventEnum.VALIDATION_FAILED;
        stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(payload)
                .setHeader(StateMachineConstants.ORDER_ID_HEADER, stateContext.getMessageHeader(StateMachineConstants.ORDER_ID_HEADER))
                .build());
    }
}
