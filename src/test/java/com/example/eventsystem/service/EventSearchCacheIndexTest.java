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
