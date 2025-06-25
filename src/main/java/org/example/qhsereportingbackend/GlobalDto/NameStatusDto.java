package org.example.qhsereportingbackend.GlobalDto;

import java.util.Map;

public record NameStatusDto(
        String name,
        Map<String, Integer> status
) {
}
