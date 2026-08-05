package io.github.weizuxiao911.springboot.ddd.infrastructure.service;

import io.github.weizuxiao911.springboot.ddd.domain.user.repository.UserRepository;
import io.github.weizuxiao911.springboot.ddd.domain.user.service.UserDomainService;
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