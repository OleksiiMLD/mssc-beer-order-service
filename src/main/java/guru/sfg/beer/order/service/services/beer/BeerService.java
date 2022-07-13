package guru.sfg.beer.order.service.services.beer;

import guru.sfg.beer.order.service.services.beer.model.BeerDto;

import java.util.Optional;

/**
 * Created by okostetskyi on 13.07.2022
 */
public interface BeerService {
    Optional<BeerDto> getBeerByUpc(String upc);
}
