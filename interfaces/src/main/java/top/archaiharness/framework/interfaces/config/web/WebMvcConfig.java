package top.archaiharness.framework.interfaces.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import top.archaiharness.framework.interfaces.config.interceptor.AuthInterceptor;
import top.archaiharness.framework.interfaces.config.interceptor.RequestInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

        private final AuthInterceptor authInterceptor;
        private final RequestInterceptor requestInterceptor;

        public WebMvcConfig(AuthInterceptor authInterceptor, RequestInterceptor requestInterceptor) {
                this.authInterceptor = authInterceptor;
                this.requestInterceptor = requestInterceptor;
        }

        @SuppressWarnings("null")
        @Override
        public void addInterceptors(@NonNull InterceptorRegistry registry) {
                registry.addInterceptor(requestInterceptor)
                                .addPathPatterns("/**")
                                .excludePathPatterns(
                                                "/error",
                                                "/actuator/**",
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-resources/**",
                                                "/webjars/**");

                registry.addInterceptor(authInterceptor)
                                .addPathPatterns("/**")
                                .excludePathPatterns(
                                                "/error",
                                                "/actuator/**",
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-resources/**",
                                                "/webjars/**",
                                                "/health");
        }
}
