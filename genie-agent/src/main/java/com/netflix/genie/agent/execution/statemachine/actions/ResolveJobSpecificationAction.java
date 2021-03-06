/*
 *
 *  Copyright 2018 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.netflix.genie.agent.execution.statemachine.actions;

import com.netflix.genie.agent.cli.ArgumentDelegates;
import com.netflix.genie.agent.cli.JobRequestConverter;
import com.netflix.genie.agent.execution.ExecutionContext;
import com.netflix.genie.agent.execution.exceptions.JobSpecificationResolutionException;
import com.netflix.genie.agent.execution.services.AgentJobSpecificationService;
import com.netflix.genie.agent.execution.statemachine.Events;
import com.netflix.genie.common.dto.v4.AgentJobRequest;
import com.netflix.genie.common.dto.v4.JobSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Action performed when in state RESOLVE_JOB_SPECIFICATION.
 * @author mprimi
 * @since 4.0.0
 */
@Slf4j
@Component
@Lazy
class ResolveJobSpecificationAction extends BaseStateAction implements StateAction.ResolveJobSpecification {

    private final ArgumentDelegates.JobRequestArguments jobRequestArguments;
    private final AgentJobSpecificationService agentJobSpecificationService;
    private final JobRequestConverter jobRequestConverter;

    ResolveJobSpecificationAction(
        final ExecutionContext executionContext,
        final ArgumentDelegates.JobRequestArguments jobRequestArguments,
        final AgentJobSpecificationService agentJobSpecificationService,
        final JobRequestConverter jobRequestConverter
    ) {
        super(executionContext);
        this.jobRequestArguments = jobRequestArguments;
        this.agentJobSpecificationService = agentJobSpecificationService;
        this.jobRequestConverter = jobRequestConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Events executeStateAction(final ExecutionContext executionContext) {
        log.info("Resolving job specification...");

        // Compose a job request from argument
        final AgentJobRequest agentJobRequest;
        try {
            agentJobRequest = jobRequestConverter.agentJobRequestArgsToDTO(jobRequestArguments);
        } catch (final JobRequestConverter.ConversionException e) {
            throw new RuntimeException("Failed to construct job request from arguments", e);
        }

        // Resolve via service
        final JobSpecification jobSpecification;
        try {
            jobSpecification = agentJobSpecificationService.resolveJobSpecification(agentJobRequest);
        } catch (final JobSpecificationResolutionException e) {
            throw new RuntimeException("Failed to resolve job specification", e);
        }

        executionContext.setJobSpecification(jobSpecification);
        return Events.RESOLVE_JOB_SPECIFICATION_COMPLETE;
    }
}
