package rt.sagas.events.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rt.sagas.events.entities.EventEntity;

import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<EventEntity, Long> {

    @Modifying
    @Query("delete EventEntity where id in (:ids)")
    void deleteByIds(@Param("ids") List<Long> ids);
}
