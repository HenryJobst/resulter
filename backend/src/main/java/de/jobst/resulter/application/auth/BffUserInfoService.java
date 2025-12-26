package de.jobst.resulter.application.auth;

import de.jobst.resulter.adapter.BffUserInfoDto;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;

public interface BffUserInfoService {
    Optional<BffUserInfoDto> extractUserInfo(@Nullable Authentication authentication);
}
