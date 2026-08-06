package com.campus.eventmanagement.repository.projection;

/**
 * Spring Data JPA interface-based projection for the "most popular events"
 * report — lets the aggregate JPQL query below return exactly these three
 * columns without loading (or needing) full Event/Registration entities.
 */
public interface EventPopularityProjection {
    Long getEventId();
    String getEventTitle();
    Long getRegistrationCount();
}
