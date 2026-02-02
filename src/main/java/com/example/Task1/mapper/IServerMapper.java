package com.example.Task1.mapper;

import com.example.Task1.dto.*;
import com.example.Task1.model.Server;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IServerMapper {
    @Mapping(source = "datacenter.id", target = "datacenterId")
    ServerResponseDto toResponseDto(Server s);
    @Mapping(target = "datacenter", ignore=true)
    Server toServer(ServerRequestDto sDto);

    @Mapping(target = "datacenter", ignore=true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateServerFromDto(ServerUpdateRequestDto dto, @MappingTarget Server server);

}
