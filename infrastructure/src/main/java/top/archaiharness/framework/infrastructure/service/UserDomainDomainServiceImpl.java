package top.archaiharness.framework.infrastructure.service;

import top.archaiharness.framework.domain.user.repository.UserRepository;
import top.archaiharness.framework.domain.user.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDomainDomainServiceImpl implements UserDomainService {

    private final UserRepository userRepository;

    @Override
    public UserRepository getUserRepository() {
        return userRepository;
    }
}