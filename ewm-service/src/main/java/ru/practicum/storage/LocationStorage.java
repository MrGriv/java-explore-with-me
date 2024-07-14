package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.LocationDb;

public interface LocationStorage extends JpaRepository<LocationDb, Long>  {
}
