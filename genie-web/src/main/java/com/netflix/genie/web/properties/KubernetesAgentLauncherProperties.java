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

package com.netflix.genie.web.properties;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties related to launching Agent on k8s cluster.
 *
 * @author rmendybayev
 * @since 4.1.0
 */
@ConfigurationProperties(prefix = KubernetesAgentLauncherProperties.PROPERTY_PREFIX)
@Getter
@Setter
@Validated
public class KubernetesAgentLauncherProperties {
    /**
     * Prefix for all properties related to k8s agent launcher.
     */
    public static final String PROPERTY_PREFIX = "genie.agent.launcher.k8s";

    /**
     * Name of the property that enables/disables the launcher.
     */
    public static final String ENABLE_PROPERTY = PROPERTY_PREFIX + ".enabled";

    /**
     *  Name of the property that sets name of Persistent Volume Claim for Jobs output volume.
     */
    public static final String JOBS_OUTPUT_PVC = PROPERTY_PREFIX + ".pvc";

    /**
     * Name of the property that sets name of container image for Agent Application.
     */
    public static final String AGENT_APP_IMAGE = PROPERTY_PREFIX + ".image";

    /**
     * Name of the property that sets name of k8s Service Account to attach to the Pod.
     */
    public static final String AGENT_APP_SA = PROPERTY_PREFIX + ".sa";

    /**
     * Name of the property that sets GCP project to Genie Agent Launcher
     */
    public static final String GCP_PROJECT_ID = PROPERTY_PREFIX + "projectId";

    /**
     * Defaults to placeholder.
     */
    private String jobsOutputPvc = "<DEFAULT>";

    /**
     * Defaults to placeholder.
     */
    private String agentAppImage = "<DEFAULT>";

    /**
     * Defaults to Empty string.
     */
    private String agentAppSa = StringUtils.EMPTY;
}
