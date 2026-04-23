package com.iasaweb.cinemagateway.dto;

import java.time.ZonedDateTime;

public record JwtDto(
    String username,
    String roles,
    ZonedDateTime expiration
) {}
