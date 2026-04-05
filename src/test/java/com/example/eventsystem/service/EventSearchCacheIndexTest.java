package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.EventResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EventSearchCacheIndexTest {

    private final EventSearchCacheIndex cacheIndex = new EventSearchCacheIndex();

    @Test
    void equals_specialCases() {
        Pageable pageable = PageRequest.of(1, 20);
        EventSearchCacheIndex.EventSearchCacheKey key = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Music", 5.0, "Org", pageable, false
        );

        org.junit.jupiter.api.Assertions.assertNotEquals(null, key);

        org.junit.jupiter.api.Assertions.assertNotEquals("some string", key);

        assertEquals(key, key);
    }

    @Test
    void equals_differentFields() {
        Pageable p1 = PageRequest.of(1, 20);
        Pageable p2 = PageRequest.of(2, 20);
        Pageable p3 = PageRequest.of(1, 30);

        EventSearchCacheIndex.EventSearchCacheKey base = EventSearchCacheIndex.EventSearchCacheKey.of("A", 1.0, "O", p1, true);

        org.junit.jupiter.api.Assertions.assertNotEquals(base, EventSearchCacheIndex.EventSearchCacheKey.of("B", 1.0, "O", p1, true));
        org.junit.jupiter.api.Assertions.assertNotEquals(base, EventSearchCacheIndex.EventSearchCacheKey.of("A", 2.0, "O", p1, true));
        org.junit.jupiter.api.Assertions.assertNotEquals(base, EventSearchCacheIndex.EventSearchCacheKey.of("A", 1.0, "Other", p1, true));
        org.junit.jupiter.api.Assertions.assertNotEquals(base, EventSearchCacheIndex.EventSearchCacheKey.of("A", 1.0, "O", p2, true));
        org.junit.jupiter.api.Assertions.assertNotEquals(base, EventSearchCacheIndex.EventSearchCacheKey.of("A", 1.0, "O", p3, true));
        org.junit.jupiter.api.Assertions.assertNotEquals(base, EventSearchCacheIndex.EventSearchCacheKey.of("A", 1.0, "O", p1, false));
    }

    @Test
    void putGetAndClear_shouldWork() {
        Pageable pageable = PageRequest.of(0, 10);
        EventSearchCacheIndex.EventSearchCacheKey key = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Tech", 10.0, "Org", pageable, true
        );
        Page<EventResponseDto> page = new PageImpl<>(List.of(EventResponseDto.builder().id(1L).build()));

        cacheIndex.put(key, page);
        assertEquals(1, cacheIndex.get(key).getTotalElements());

        cacheIndex.clear();

        assertNull(cacheIndex.get(key));
    }

    @Test
    void keyOf_shouldProduceEqualKeysForSameInput() {
        Pageable pageable = PageRequest.of(1, 20);

        EventSearchCacheIndex.EventSearchCacheKey key1 = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Music", 5.0, "Org", pageable, false
        );
        EventSearchCacheIndex.EventSearchCacheKey key2 = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Music", 5.0, "Org", pageable, false
        );

        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }
}
