package com.ticketflow.eventservice.repos;

import com.ticketflow.eventservice.entities.Event;
import com.ticketflow.eventservice.entities.EventStatus;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepo extends JpaRepository<Event, UUID> {
    java.util.List<Event> findByVenueId(UUID venueId);
    @Query("""
            select e from Event e where e.status = :status
            and (:query is null or lower(e.title) like lower(concat('%', :query, '%'))
                 or lower(e.summary) like lower(concat('%', :query, '%')))
            and (:city is null or lower(e.venue.city) = lower(:city))
            and (:categoryId is null or e.category.id = :categoryId)
            and (:fromDate is null or e.startsAt >= :fromDate)
            and (:toDate is null or e.startsAt <= :toDate)
            and ((:minPrice is null and :maxPrice is null) or exists
                 (select t from TicketTier t where t.event = e
                  and (:minPrice is null or t.price >= :minPrice)
                  and (:maxPrice is null or t.price <= :maxPrice)))
            """)
    Page<Event> search(@Param("status") EventStatus status, @Param("query") String query,
                       @Param("city") String city, @Param("categoryId") UUID categoryId,
                       @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate,
                       @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
                       Pageable pageable);
}
