package com.example.eventsystem.repository;

import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.model.enums.EventStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.jspecify.annotations.NonNull;
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
}