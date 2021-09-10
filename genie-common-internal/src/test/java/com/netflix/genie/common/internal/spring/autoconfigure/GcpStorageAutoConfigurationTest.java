package com.netflix.genie.common.internal.spring.autoconfigure;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.storage.Storage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.gcp.autoconfigure.storage.GcpStorageAutoConfiguration;
import org.springframework.cloud.gcp.core.Credentials;
import org.springframework.cloud.gcp.core.DefaultCredentialsProvider;
import org.springframework.cloud.gcp.core.DefaultGcpProjectIdProvider;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.cloud.gcp.storage.GoogleStorageProtocolResolver;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

public class GcpStorageAutoConfigurationTest {
    private final ApplicationContextRunner applicationContextRunner = new ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                GcpStorageAutoConfiguration.class
            )
        ).withPropertyValues(
            "spring.cloud.gcp.storage.enabled=true",
            "spring.cloud.gcp.credentials.location=file:/usr/local/key.json",
            "spring.cloud.gcp.project-id=my-gcp-project-id"
        ).withUserConfiguration(ExternalBeans.class);

    @Test
    void expectedBeansExist() {
        this.applicationContextRunner.run(
            context -> {
                Assertions.assertThat(context).hasSingleBean(CredentialsProvider.class);
                Assertions.assertThat(context).hasSingleBean(GcpProjectIdProvider.class);
                Assertions.assertThat(context).hasSingleBean(Storage.class);

                Assertions.assertThat(context).hasSingleBean(GoogleStorageProtocolResolver.class);
            }
        );
    }

    private static class ExternalBeans {
        @Bean
        GcpProjectIdProvider gcpProjectIdProvider() { return new DefaultGcpProjectIdProvider();
        }

        @Bean
        CredentialsProvider credentialsProvider() {
            try {
                return new DefaultCredentialsProvider(Credentials::new);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
