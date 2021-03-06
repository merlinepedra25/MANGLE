/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

/**
 * @author rpraveen
 *
 */

package com.vmware.mangle.test.plugin.helpers.systemresource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.utils.DockerCommandUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

public class CustomDockerSystemResourceFaultHelper implements CustomSystemResourceFaultHelper {

    private EndpointClientFactory endpointClientFactory;

    private CustomSystemResourceFaultUtils systemResourceFaultUtils;

    private DockerCommandUtils dockerCommandUtils;

    public CustomDockerSystemResourceFaultHelper(EndpointClientFactory endpointClientFactory,
            CustomSystemResourceFaultUtils systemResourceFaultUtils) {
        super();
        this.endpointClientFactory = endpointClientFactory;
        this.systemResourceFaultUtils = systemResourceFaultUtils;
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec faultSpec) throws MangleException {
        if (null == dockerCommandUtils) {
            return new DockerCommandUtils(faultSpec, endpointClientFactory);
        } else {
            return dockerCommandUtils;
        }

    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(CommandExecutionFaultSpec faultSpec) throws MangleException {
        CommandInfo injectFaultCommandInfo = CommandInfo
                .builder(systemResourceFaultUtils.buildInjectionCommand(faultSpec.getArgs(),
                        faultSpec.getInjectionHomeDir()))
                .ignoreExitValueCheck(false)
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest())
                .expectedCommandOutputList(Collections.emptyList()).build();
        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(injectFaultCommandInfo);
        return commandInfoList;
    }

    @Override
    public List<CommandInfo> getRemediationcommandInfoList(CommandExecutionFaultSpec faultSpec) throws MangleException {
        if (!systemResourceFaultUtils.isManualRemediationSupported(faultSpec.getFaultName())) {
            return Collections.emptyList();
        }
        String remediationCommand =
                systemResourceFaultUtils.buildRemediationCommand(faultSpec.getArgs(), faultSpec.getInjectionHomeDir());
        List<CommandInfo> commandInfoList = new ArrayList<>();
        if (!StringUtils.isEmpty(remediationCommand)) {
            CommandInfo commandInfo = CommandInfo.builder(remediationCommand).ignoreExitValueCheck(false)
                    .expectedCommandOutputList(Collections.emptyList())
                    .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultRemediationRequest())
                    .build();
            commandInfoList.add(commandInfo);
        }
        return commandInfoList;
    }

    @Override
    public List<SupportScriptInfo> getFaultInjectionScripts(CommandExecutionFaultSpec faultSpec) {
        return systemResourceFaultUtils.getAgentFaultScripts(faultSpec);
    }

    @Override
    public void checkTaskSpecificPrerequisites(CommandExecutionFaultSpec spec) throws MangleException {
        //No Specific Requirements
    }
}
