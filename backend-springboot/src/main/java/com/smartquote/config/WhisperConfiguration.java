package com.smartquote.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Registers Whisper HTTP client with connect/read timeouts from configuration.
 */
@Configuration
@EnableConfigurationProperties(WhisperProperties.class)
public class WhisperConfiguration {

    @Bean
    public RestClient whisperRestClient(WhisperProperties whisperProperties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(whisperProperties.getConnectTimeoutMs()))
                .withReadTimeout(Duration.ofMillis(whisperProperties.getReadTimeoutMs()));

        return RestClient.builder()
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .build();
    }
}
