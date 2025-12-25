package de.jobst.resulter.application.auth;

import de.jobst.resulter.adapter.driver.web.dto.BffUserInfoDto;
import java.util.Optional;
import org.springframework.security.core.Authentication;

public interface BffUserInfoService {
    Optional<BffUserInfoDto> extractUserInfo(Authentication authentication);
}
