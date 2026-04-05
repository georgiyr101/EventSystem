package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.EventResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void keyEquals_shouldCoverNegativeBranches() {
        EventSearchCacheIndex.EventSearchCacheKey base = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Music", 5.0, "Org", PageRequest.of(1, 20), false
        );
        EventSearchCacheIndex.EventSearchCacheKey diffCategory = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Tech", 5.0, "Org", PageRequest.of(1, 20), false
        );
        EventSearchCacheIndex.EventSearchCacheKey diffPrice = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Music", 6.0, "Org", PageRequest.of(1, 20), false
        );
        EventSearchCacheIndex.EventSearchCacheKey diffOrganizer = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Music", 5.0, "Other", PageRequest.of(1, 20), false
        );
        EventSearchCacheIndex.EventSearchCacheKey diffPageNumber = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Music", 5.0, "Org", PageRequest.of(2, 20), false
        );
        EventSearchCacheIndex.EventSearchCacheKey diffPageSize = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Music", 5.0, "Org", PageRequest.of(1, 21), false
        );
        EventSearchCacheIndex.EventSearchCacheKey diffSort = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Music", 5.0, "Org", PageRequest.of(1, 20, Sort.by("name")), false
        );
        EventSearchCacheIndex.EventSearchCacheKey diffUseNative = EventSearchCacheIndex.EventSearchCacheKey.of(
                "Music", 5.0, "Org", PageRequest.of(1, 20), true
        );

        assertTrue(base.equals(base));
        assertNotEquals(base, null);
        assertNotEquals(base, "not-a-key");
        assertNotEquals(base, diffCategory);
        assertNotEquals(base, diffPrice);
        assertNotEquals(base, diffOrganizer);
        assertNotEquals(base, diffPageNumber);
        assertNotEquals(base, diffPageSize);
        assertNotEquals(base, diffSort);
        assertNotEquals(base, diffUseNative);
    }
}
