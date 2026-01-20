package com.example.demo.application.oauth;

import com.example.demo.domain.Provider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TokenExchangerFactory {
    private final Map<Provider, TokenExchanger> map;

    public TokenExchangerFactory(List<TokenExchanger> exchangers) {
        this.map = exchangers.stream()
                .collect(Collectors.toUnmodifiableMap(TokenExchanger::provider, Function.identity()));
    }

    public TokenExchanger get(Provider provider) {
        TokenExchanger exchanger = map.get(provider);
        if (exchanger == null) throw new IllegalArgumentException("지원하지 않는 provider 입니다: " + provider);
        return exchanger;
    }
}
