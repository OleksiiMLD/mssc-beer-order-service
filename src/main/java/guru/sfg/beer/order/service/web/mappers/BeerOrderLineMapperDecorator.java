package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.services.beer.BeerService;
import guru.sfg.beer.order.service.services.beer.model.BeerDto;
import guru.sfg.beer.order.service.web.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by okostetskyi on 13.07.2022
 */
public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {
    private BeerOrderLineMapper beerOrderLineMapper;
    private BeerService beerService;

    @Autowired
    public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper) {
        this.beerOrderLineMapper = beerOrderLineMapper;
    }

    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        final BeerOrderLineDto beerOrderLineDto = beerOrderLineMapper.beerOrderLineToDto(line);
        final Optional<BeerDto> beerByUpc = beerService.getBeerByUpc(line.getUpc());
        beerByUpc.ifPresent(beerDto -> {
                    beerOrderLineDto.setBeerName(beerDto.getBeerName());
                    if (beerOrderLineDto.getBeerId() == null) {
                        beerOrderLineDto.setBeerId(beerDto.getId());
                    }
                }
        );
        return beerOrderLineDto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        final BeerOrderLine beerOrderLine = beerOrderLineMapper.dtoToBeerOrderLine(dto);
        final Optional<BeerDto> beerByUpc = beerService.getBeerByUpc(dto.getUpc());
        beerByUpc.ifPresent(beerDto -> beerOrderLine.setBeerId(beerDto.getId()));
        return beerOrderLine;
    }
}
