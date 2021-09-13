/*
 * Copyright 2021. Netflix, Inc.
 * Copyright 2021. EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.netflix.genie.common.internal.spring.autoconfigure;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.storage.Storage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.gcp.autoconfigure.core.GcpContextAutoConfiguration;
import org.springframework.cloud.gcp.autoconfigure.storage.GcpStorageAutoConfiguration;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.cloud.gcp.storage.GoogleStorageProtocolResolver;
import org.springframework.context.annotation.Bean;

public class GcpStorageAutoConfigurationTest {
    private final ApplicationContextRunner applicationContextRunner = new ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                GcpContextAutoConfiguration.class,
                GcpStorageAutoConfiguration.class
            )
        ).withPropertyValues("spring.cloud.gcp.projectId=my-gcp-project-id")
        .withUserConfiguration(TestConfiguration.class);

    @Test
    void expectedBeansExist() {
        this.applicationContextRunner.run(
            context -> {
                Assertions.assertThat(context).hasSingleBean(CredentialsProvider.class);
                Assertions.assertThat(context).hasSingleBean(GcpProjectIdProvider.class);
                final GcpProjectIdProvider gcpProjectIdProvider = context.getBean(GcpProjectIdProvider.class);
                Assertions.assertThat(gcpProjectIdProvider.getProjectId()).isEqualTo("my-gcp-project-id");
                Assertions.assertThat(context).hasSingleBean(Storage.class);

                Assertions.assertThat(context).hasSingleBean(GoogleStorageProtocolResolver.class);
            }
        );
    }

    private static class TestConfiguration {
        @Bean
        CredentialsProvider googleCredentials() {
            return () -> Mockito.mock(com.google.auth.Credentials.class);
        }
    }
}
