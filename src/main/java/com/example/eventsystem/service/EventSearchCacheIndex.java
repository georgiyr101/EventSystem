package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.EventResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class EventSearchCacheIndex {

    private final Map<EventSearchCacheKey, Page<EventResponseDto>> cache = new HashMap<>();

    public synchronized Page<EventResponseDto> get(final EventSearchCacheKey key) {
        return cache.get(key);
    }

    public synchronized void put(final EventSearchCacheKey key, final Page<EventResponseDto> value) {
        cache.put(key, value);
    }

    public synchronized void clear() {
        System.out.println("index invalidation");
        cache.clear();
    }

    @AllArgsConstructor
    public static final class EventSearchCacheKey {
        private final String categoryName;
        private final Double minPrice;
        private final String organizerName;
        private final int pageNumber;
        private final int pageSize;
        private final String sort;
        private final boolean useNative;

        public static EventSearchCacheKey of(
                final String categoryName,
                final Double minPrice,
                final String organizerName,
                final Pageable pageable,
                final boolean useNative
        ) {
            return new EventSearchCacheKey(
                    categoryName,
                    minPrice,
                    organizerName,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    pageable.getSort().toString(),
                    useNative
            );
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            EventSearchCacheKey that = (EventSearchCacheKey) o;
            return pageNumber == that.pageNumber
                    && pageSize == that.pageSize && useNative == that.useNative
                    && Objects.equals(categoryName, that.categoryName) && Objects.equals(minPrice, that.minPrice)
                    && Objects.equals(organizerName, that.organizerName) && Objects.equals(sort, that.sort);
        }

        @Override
        public int hashCode() {
            return Objects.hash(categoryName, minPrice, organizerName, pageNumber, pageSize, sort, useNative);
        }
    }
}