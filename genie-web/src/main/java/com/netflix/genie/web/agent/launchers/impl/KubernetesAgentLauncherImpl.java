/*
 * Copyright 2021.  EPAM Systems, Inc. (https://www.epam.com/)
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

package com.netflix.genie.web.agent.launchers.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Lists;
import com.netflix.genie.web.agent.launchers.AgentLauncher;
import com.netflix.genie.web.dtos.ResolvedJob;
import com.netflix.genie.web.exceptions.checked.AgentLaunchException;
import com.netflix.genie.web.introspection.GenieWebHostInfo;
import com.netflix.genie.web.introspection.GenieWebRpcInfo;
import com.netflix.genie.web.properties.KubernetesAgentLauncherProperties;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1ObjectMetaBuilder;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.openapi.models.V1VolumeBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Implementation of {@link AgentLauncher} which launched Agent instances on the k8s cluster.
 *
 * @author rmendybayev
 * @since 4.1.0
 */
@Slf4j
public class KubernetesAgentLauncherImpl implements AgentLauncher {
    private static final String THIS_CLASS = KubernetesAgentLauncherImpl.class.getCanonicalName();
    private final JsonNode launcherExt;
    private final String hostname;
    private final int rpcPort;
    private final KubernetesAgentLauncherProperties kubernetesAgentLauncherProperties;
    private final Environment environment;

    /**
     * Constructor.
     * @param hostInfo                          The {@link GenieWebRpcInfo} instance
     * @param rpcInfo                           The {@link GenieWebRpcInfo} instance
     * @param kubernetesAgentLauncherProperties The properties from the configuration that control agent behavior
     * @param environment                       The application environment to pull dynamic properties from
     */
    public KubernetesAgentLauncherImpl(
        final GenieWebHostInfo hostInfo,
        final GenieWebRpcInfo rpcInfo,
        final KubernetesAgentLauncherProperties kubernetesAgentLauncherProperties,
        final Environment environment
    ) {
        this.hostname = hostInfo.getHostname();
        this.rpcPort = rpcInfo.getRpcPort();
        this.kubernetesAgentLauncherProperties = kubernetesAgentLauncherProperties;
        this.environment = environment;

        this.launcherExt = JsonNodeFactory.instance.objectNode()
            .put(LAUNCHER_CLASS_EXT_FIELD, THIS_CLASS)
            .put(SOURCE_HOST_EXT_FIELD, this.hostname);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<JsonNode> launchAgent(
        @Valid final ResolvedJob resolvedJob,
        @Nullable final JsonNode requestedLauncherExt)
        throws AgentLaunchException {
        final String templateFileName = this.environment.getProperty(
            KubernetesAgentLauncherProperties.AGENT_APP_JOB_TEMPLATE,
            String.class,
            this.kubernetesAgentLauncherProperties.getAgentAppJobTemplate()
        );


        try {
            final String jobId = resolvedJob.getJobSpecification().getJob().getId();
            final ApiClient client = Config.defaultClient();
            final BatchV1Api batchV1Api = new BatchV1Api(client);

            final InputStream jobTemplate = new ClassPathResource(templateFileName).getInputStream();
            final InputStreamReader reader = new InputStreamReader(jobTemplate, StandardCharsets.UTF_8);
            final V1Job job = (V1Job) Yaml.load(reader);
            reader.close();

            job.getSpec().getTemplate().setMetadata(new V1ObjectMetaBuilder().addToLabels("jobId", jobId).build());
            job.getSpec().getTemplate().getSpec().addVolumesItem(new V1VolumeBuilder()
                .withName("jobs-pv-storage")
                .withPersistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName(
                        this.environment.getProperty(
                            KubernetesAgentLauncherProperties.JOBS_OUTPUT_PVC,
                            String.class,
                            this.kubernetesAgentLauncherProperties.getJobsOutputPvc()
                        )
                    )
                )
                .build());
            job.getSpec().getTemplate().getSpec().serviceAccountName(
                this.environment.getProperty(
                KubernetesAgentLauncherProperties.AGENT_APP_SA,
                String.class,
                this.kubernetesAgentLauncherProperties.getAgentAppSa()
            ));
            final V1Container agentLauncherContainer = job.getSpec().getTemplate().getSpec().getContainers().get(0);
            agentLauncherContainer.setImage(
                this.environment.getProperty(
                KubernetesAgentLauncherProperties.AGENT_APP_IMAGE,
                String.class,
                this.kubernetesAgentLauncherProperties.getAgentAppImage()
            ));
            agentLauncherContainer.setImagePullPolicy(
                this.environment.getProperty(
                KubernetesAgentLauncherProperties.AGENT_APP_IMAGE_PULL_POLICY,
                String.class,
                this.kubernetesAgentLauncherProperties.getAgentAppImagePullPolicy()
            ));
            agentLauncherContainer.env(Lists.newArrayList(
                new V1EnvVar().name("spring.cloud.gcp.core.enabled").value("true"),
                new V1EnvVar().name("spring.cloud.gcp.project-id").value(
                    this.environment.getProperty(
                        KubernetesAgentLauncherProperties.GCP_PROJECT_ID,
                        String.class,
                        this.kubernetesAgentLauncherProperties.getGcpProjectId()
                    )
                )
            ));
            agentLauncherContainer
                .addCommandItem("/cnb/lifecycle/launcher")
                .addArgsItem("java")
                .addArgsItem("org.springframework.boot.loader.JarLauncher")
                .addArgsItem("exec")
                .addArgsItem("--server-host")
                .addArgsItem("genie-grpc")
                .addArgsItem("--server-port")
                .addArgsItem(Integer.toString(this.rpcPort))
                .addArgsItem("--api-job")
                .addArgsItem("--job-id")
                .addArgsItem(jobId);
            log.info("Launching container for jobID: " + jobId);

            batchV1Api.createNamespacedJob(
                "default",
                job,
                null,
                null, null
            );
        } catch (IOException e) {
            throw new AgentLaunchException(
                String.format("Unable to launch agent using command: %s", e.getMessage()), e
            );
        } catch (ApiException e) {
            log.error("ApiException was thrown. Here is some details");
            log.error("ApiException message {}", e.getMessage());
            log.error("ApiException code {}", e.getCode());
            log.error("ApiException respBody {}", e.getResponseBody());
            log.error("ApiException headers {}", e.getResponseHeaders());
            throw new AgentLaunchException(
                "Unable to launch k8s agent", e
            );
        }
        return Optional.of(this.launcherExt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Health health() {
        return null;
    }
}
