package com.fcv.citas.application.auth;

import java.util.List;

public record RegisteredUserResponse(Long id, String email, List<String> roles) { }
