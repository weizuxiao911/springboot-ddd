package io.github.weizuxiao911.springboot.ddd.infrastructure.pbac.autoconfigure;

import io.github.weizuxiao911.springboot.ddd.common.pbac.service.PBACService;
import io.github.weizuxiao911.springboot.ddd.infrastructure.pbac.impl.PBACServiceImpl;
import io.github.weizuxiao911.springboot.ddd.infrastructure.pbac.aspect.PBACAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ConditionalOnWebApplication
@EnableAspectJAutoProxy
public class PBACAutoConfiguration {

    @Bean
    public PBACService pbacService() {
        return new PBACServiceImpl();
    }

    @Bean
    public PBACAspect pbacAspect(PBACService pbacService) {
        return new PBACAspect(pbacService);
    }
}
