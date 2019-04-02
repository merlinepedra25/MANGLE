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

package com.vmware.mangle.unittest.faults.plugin.helpers.byteman;

import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.AGENT_NAME;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.EXTRACT_AGENT_COMMAND;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.FI_ADD_INFO_FAULTID;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.GET_FAULT_COMMAND_WITH_PORT;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.LOAD_ARG;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.PID_AGENT_COMMAND_WITH_PORT;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.PID_ATTACH_MXBEANS_COMMAND_WITH_PORT;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.PORT_9091;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.REMEDIATION_COMMAND_WITH_PORT;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.SUBMIT_COMMAND_WITH_PORT;

import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.byteman.LinuxBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Test Class for LinuxBytemanFaultHelper
 *
 * @author jayasankarr
 *
 */
@Log4j2
public class LinuxBytemanFaultHelperTest {

    @Mock
    EndpointClientFactory endpointClientFactory;
    @Mock
    SSHUtils sshUtils;

    private LinuxBytemanFaultHelper linuxBytemanFaultHelper;
    private FaultsMockData faultsMockData = new FaultsMockData();

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        linuxBytemanFaultHelper = new LinuxBytemanFaultHelper(endpointClientFactory);
    }

    @Test
    public void testGetExecutor() {
        ICommandExecutor executor = null;
        try {
            CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(),
                    cpuFaultSpec.getEndpoint())).thenReturn(sshUtils);
            executor = linuxBytemanFaultHelper.getExecutor(cpuFaultSpec);
        } catch (MangleException e) {
            log.error("testGetExecutor failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertEquals(executor, sshUtils);
    }

    @Test
    public void testGetJVMAgentInjectionCommandInfoList() {
        try {
            CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(),
                    cpuFaultSpec.getEndpoint())).thenReturn(sshUtils);
            List<CommandInfo> injectionCommands = linuxBytemanFaultHelper.getInjectionCommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertTrue(injectionCommands.size() > 0);
            Assert.assertEquals(String.format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9091, null),
                    injectionCommands.get(2).getCommand());
            Assert.assertEquals(
                    String.format(PID_AGENT_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9091, LOAD_ARG + " 80"),
                    injectionCommands.get(3).getCommand());
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForCPUJVMAgentFault failed with Exception: ", e);
            Assert.assertTrue(false);


        }
    }

    @Test
    public void testGetJVMAgentRemediationCommandInfoList() {
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
        Mockito.when(endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(),
                cpuFaultSpec.getEndpoint())).thenReturn(sshUtils);
        List<CommandInfo> remediationCommands = linuxBytemanFaultHelper.getRemediationCommandInfoList(cpuFaultSpec);
        log.info(RestTemplateWrapper.objectToJson(remediationCommands));
        Assert.assertEquals(
                String.format(REMEDIATION_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9091, FI_ADD_INFO_FAULTID),
                remediationCommands.get(0).getCommand());
        Assert.assertEquals(
                String.format(GET_FAULT_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9091, FI_ADD_INFO_FAULTID),
                remediationCommands.get(1).getCommand());

    }


    @Test
    public void testGetJVMCodeLevelInjectionCommandInfoList() {
        try {
            JVMCodeLevelFaultSpec springServiceExceptionFaultSpec =
                    faultsMockData.getLinuxJvmCodelevelFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(
                    springServiceExceptionFaultSpec.getCredentials(), springServiceExceptionFaultSpec.getEndpoint()))
                    .thenReturn(sshUtils);
            List<CommandInfo> injectionCommands =
                    linuxBytemanFaultHelper.getInjectionCommandInfoList(springServiceExceptionFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertTrue(injectionCommands.size() > 0);
            Assert.assertEquals(
                    String.format(EXTRACT_AGENT_COMMAND, DEFAULT_TEMP_DIR, "byteman-download-3.0.10-full.tar.gz"),
                    injectionCommands.get(0).getCommand());
            Assert.assertEquals(injectionCommands.get(1).getCommand(), "chmod -R 777 " + DEFAULT_TEMP_DIR + "/"
                    + AGENT_NAME + ";chmod -R 777 " + DEFAULT_TEMP_DIR + "/" + AGENT_NAME + "/*");
            Assert.assertTrue(injectionCommands.get(2).getCommand()
                    .contains(String.format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9091, null)));
            Assert.assertTrue(injectionCommands.get(4).getCommand().toString().contains(String
                    .format(SUBMIT_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9091, DEFAULT_TEMP_DIR + "/123456;.btm")));
        } catch (MangleException e) {
            log.error("testGetRemediationCommandInfoListForSpringServiceFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetJVMCodeLevelRemediationCommandInfoList() {

        JVMCodeLevelFaultSpec springServiceExceptionFaultSpec = faultsMockData.getLinuxJvmCodelevelFaultSpec();
        Mockito.when(endpointClientFactory.getEndPointClient(springServiceExceptionFaultSpec.getCredentials(),
                springServiceExceptionFaultSpec.getEndpoint())).thenReturn(sshUtils);
        List<CommandInfo> remediationCommands =
                linuxBytemanFaultHelper.getRemediationCommandInfoList(springServiceExceptionFaultSpec);
        log.info(RestTemplateWrapper.objectToJson(remediationCommands));
        Assert.assertTrue(remediationCommands.get(0).getCommand().contains(String.format(SUBMIT_COMMAND_WITH_PORT,
                DEFAULT_TEMP_DIR, PORT_9091, "-u " + DEFAULT_TEMP_DIR + "/123456;.btm")));
    }

    @Test
    void testGetAgentFaultInjectionScripts() {
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
        List<SupportScriptInfo> supportScripts = linuxBytemanFaultHelper.getAgentFaultInjectionScripts(cpuFaultSpec);
        Assert.assertEquals("byteman-download-3.0.10-full.tar.gz", supportScripts.get(0).getScriptFileName());
        Assert.assertEquals(DEFAULT_TEMP_DIR, supportScripts.get(0).getTargetDirectoryPath());
    }

}
