package com.example.demo.application.oauth;

import com.example.demo.domain.Provider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OauthServiceFactory {
    private final Map<Provider, OauthService> map;

    public OauthServiceFactory(List<OauthService> services) {
        this.map = services.stream()
                .collect(Collectors.toUnmodifiableMap(OauthService::provider, Function.identity()));
    }

    public OauthService get(Provider provider) {
        OauthService service = map.get(provider);
        if (service == null) throw new IllegalArgumentException("지원하지 않는 provider 입니다: " + provider);
        return service;
    }
}