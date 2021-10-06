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

package com.netflix.genie.web.launchers.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.netflix.genie.common.external.dtos.v4.JobMetadata;
import com.netflix.genie.common.external.dtos.v4.JobSpecification;
import com.netflix.genie.web.agent.launchers.impl.KubernetesAgentLauncherImpl;
import com.netflix.genie.web.dtos.ResolvedJob;
import com.netflix.genie.web.exceptions.checked.AgentLaunchException;
import com.netflix.genie.web.introspection.GenieWebHostInfo;
import com.netflix.genie.web.introspection.GenieWebRpcInfo;
import com.netflix.genie.web.properties.KubernetesAgentLauncherProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.util.Random;
import java.util.UUID;

public class KubernetesAgentLauncherImplTest {
    private static final String JOB_ID = UUID.randomUUID().toString();
    private static final int RPC_PORT = new Random().nextInt();

    private String hostName;
    private GenieWebHostInfo hostInfo;
    private GenieWebRpcInfo rpcInfo;
    private KubernetesAgentLauncherImpl launcher;
    private ResolvedJob resolvedJob;
    private JobMetadata jobMetadata;
    private JobSpecification jobSpec;
    private JobSpecification.ExecutionResource job;
    private JsonNode requestedLauncherExt;
    private KubernetesAgentLauncherProperties launcherProperties;
    private Environment environment;

    @BeforeEach
    void setup() {
        this.hostName = "genie.epam.com";
        this.resolvedJob = Mockito.mock(ResolvedJob.class);
        this.jobMetadata = Mockito.mock(JobMetadata.class);
        this.jobSpec = Mockito.mock(JobSpecification.class);
        this.job = Mockito.mock(JobSpecification.ExecutionResource.class);
        this.requestedLauncherExt = null;
        this.hostInfo = Mockito.mock(GenieWebHostInfo.class);
        this.rpcInfo = Mockito.mock(GenieWebRpcInfo.class);
        this.launcherProperties = new KubernetesAgentLauncherProperties();
        this.launcherProperties.setAgentAppJobTemplate("test-agent-launcher-job.yaml");
        this.launcherProperties.setAppPodName("test-pod-0");
        this.environment = new MockEnvironment();
        Mockito
            .when(
                this.hostInfo.getHostname()
            )
            .thenReturn(this.hostName);
        Mockito
            .when(this.rpcInfo.getRpcPort()).thenReturn(RPC_PORT);
    }

    @Disabled
    @Test
    void launchAgent() throws AgentLaunchException {
        this.launcher = new KubernetesAgentLauncherImpl(hostInfo, rpcInfo, launcherProperties, environment);
        Mockito.when(this.resolvedJob.getJobSpecification()).thenReturn(this.jobSpec);
        Mockito.when(this.resolvedJob.getJobSpecification().getJob()).thenReturn(this.job);
        Mockito
            .when(
                this.resolvedJob.getJobSpecification().getJob().getId())
            .thenReturn(
                JOB_ID);
        Mockito
            .when(
                this.resolvedJob.getJobMetadata())
            .thenReturn(
                this.jobMetadata);
        launcher.launchAgent(resolvedJob, requestedLauncherExt);
    }
}
