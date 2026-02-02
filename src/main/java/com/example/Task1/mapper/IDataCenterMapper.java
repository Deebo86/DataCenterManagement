package com.example.Task1.mapper;

import com.example.Task1.dto.*;
import com.example.Task1.model.DataCenter;
import com.example.Task1.model.Server;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface IDataCenterMapper {
    @Mapping(source = "servers", target = "serverIds")
    DataCenterResponseDto toDataCenterResponseDto(DataCenter dc);

    @Mapping(target = "servers", ignore = true)
    DataCenter toDataCenter(DataCenterRequestDto dcDto);

    @Mapping(target = "servers", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDataCenterFromDto(DataCenterUpdateRequestDto dto, @MappingTarget DataCenter dataCenter);

    List<DataCenter> toDataCenter (List<DataCenterResponseDto> dtoList);

    List<DataCenterResponseDto> toDataCenterResponseDto(List<DataCenter> dcList);

    default List<Integer> mapServersToServerIds(List<Server> servers) {
        if (servers == null) {
            return null;
        }
        return servers.stream()
                .map(Server::getId)
                .collect(Collectors.toList());
    }

}
