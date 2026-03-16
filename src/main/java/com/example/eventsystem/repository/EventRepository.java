package com.example.eventsystem.repository;

import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"organizer", "categories"})
    List<Event> findAll();

    @EntityGraph(attributePaths = {"organizer", "categories"})
    List<Event> findAllByStatus(EventStatus status);

    @Query(value = """
            SELECT DISTINCT e.id FROM Event e
            JOIN e.categories c
            JOIN e.organizer o
            WHERE (CAST(:categoryName AS string) IS NULL OR c.name = :categoryName)
            AND (:minPrice IS NULL OR e.ticketPrice >= :minPrice)
            AND (CAST(:organizerName AS string) IS NULL 
                 OR LOWER(o.name) LIKE LOWER(CONCAT('%', CAST(:organizerName AS string), '%')))
            """,
                countQuery = """
            SELECT COUNT(DISTINCT e.id) FROM Event e
            JOIN e.categories c
            JOIN e.organizer o
            WHERE (CAST(:categoryName AS string) IS NULL OR c.name = :categoryName)
            AND (:minPrice IS NULL OR e.ticketPrice >= :minPrice)
            AND (CAST(:organizerName AS string) IS NULL 
            OR LOWER(o.name) LIKE LOWER(CONCAT('%', CAST(:organizerName AS string), '%')))
            """)
    Page<Long> findIdsByFilterJpql(
            @Param("categoryName") String categoryName,
            @Param("minPrice") Double minPrice,
            @Param("organizerName") String organizerName,
            Pageable pageable);

    // Пункт 2 + 3: Native Query с фильтрацией и пагинацией (PostgreSQL)
    @Query(value = """
            SELECT DISTINCT e.id FROM events e
            JOIN event_categories ec ON e.id = ec.event_id
            JOIN categories c ON c.id = ec.category_id
            JOIN organizers o ON o.id = e.organizer_id
            WHERE (:categoryName IS NULL OR c.name = :categoryName)
            AND (:minPrice IS NULL OR e.ticket_price >= :minPrice)
            AND (:organizerName IS NULL OR o.name ILIKE CONCAT('%', :organizerName, '%'))
            """,
            countQuery = """
            SELECT COUNT(DISTINCT e.id) FROM events e
            JOIN event_categories ec ON e.id = ec.event_id
            JOIN categories c ON c.id = ec.category_id
            JOIN organizers o ON o.id = e.organizer_id
            WHERE (:categoryName IS NULL OR c.name = :categoryName)
            AND (:minPrice IS NULL OR e.ticket_price >= :minPrice)
            AND (:organizerName IS NULL OR o.name ILIKE CONCAT('%', :organizerName, '%'))
            """,
            nativeQuery = true)
    Page<Long> findIdsByFilterNative(
            @Param("categoryName") String categoryName,
            @Param("minPrice") Double minPrice,
            @Param("organizerName") String organizerName,
            Pageable pageable);

    // Вспомогательный метод для загрузки полных данных (решение N+1)
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.organizer LEFT JOIN FETCH e.categories WHERE e.id IN :ids")
    List<Event> findAllByIdsWithDependencies(@Param("ids") List<Long> ids);
}