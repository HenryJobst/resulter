package de.jobst.resulter.application.auth;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;

public interface BffUserInfoService {
    Optional<BffUserInfo> extractUserInfo(@Nullable Authentication authentication);
}
