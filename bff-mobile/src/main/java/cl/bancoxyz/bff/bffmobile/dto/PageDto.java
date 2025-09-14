package cl.bancoxyz.bff.bffmobile.dto;

import java.util.List;

public record PageDto<T>(List<T> items, int page, int size, int total) {}

