package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.Location;
import ru.practicum.model.LocationDb;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDb toEntity(Location location);
}
