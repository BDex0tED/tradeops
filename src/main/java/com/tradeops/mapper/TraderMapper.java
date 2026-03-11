package com.tradeops.mapper;

import com.tradeops.model.entity.Trader;
import com.tradeops.model.response.TraderResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TraderMapper {
    TraderResponse toTraderResponse(Trader trader);
}
