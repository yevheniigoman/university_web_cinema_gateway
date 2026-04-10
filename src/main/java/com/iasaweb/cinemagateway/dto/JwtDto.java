package com.iasaweb.cinemagateway.dto;

import java.util.List;
import java.time.ZonedDateTime;

public record JwtDto(
    String username,
    List<String> roles,
    ZonedDateTime expiration
) {}
