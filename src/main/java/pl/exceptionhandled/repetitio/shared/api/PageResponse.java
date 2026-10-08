package pl.exceptionhandled.repetitio.shared.api;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int size, boolean hasNext) {
    public static <T> PageResponse<T> of(List<T> items, int page, int size) {
        return new PageResponse<>(List.copyOf(items.subList(0, Math.min(size, items.size()))),
                page, size, items.size() > size);
    }
}

