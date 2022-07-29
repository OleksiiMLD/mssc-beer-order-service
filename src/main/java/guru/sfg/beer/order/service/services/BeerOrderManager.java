package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.brewery.model.BeerOrderDto;

import java.util.UUID;

/**
 * Created by jt on 11/29/19.
 */
public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);

    void processValidationResult(UUID beerOrderId, Boolean isValid);

    void processAllocationResult(BeerOrderDto beerOrderDto, Boolean isPendingInventory);

    void processAllocationFailure(UUID beerOrderId);
}
