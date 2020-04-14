/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.integration.domain.management;

import static org.hamcrest.CoreMatchers.is;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.AUTO_START;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.BLOCKING;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.COMPOSITE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.START_SERVERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.STEPS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.STOP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.STOP_SERVERS;
import static org.jboss.as.test.integration.domain.management.util.DomainTestSupport.validateResponse;
import static org.jboss.as.test.integration.domain.management.util.DomainTestUtils.waitUntilState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.test.integration.domain.management.util.DomainLifecycleUtil;
import org.jboss.as.test.integration.domain.management.util.DomainTestSupport;
import org.jboss.dmr.ModelNode;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Emanuel Muckenhuber
 */
public class ServerAutoStartTestCase {

    private static final String STOPPED_EXT = ".stopped";
    private static final String STARTED_EXT = ".started";

    private static DomainTestSupport testSupport;
    private static DomainLifecycleUtil domainMasterLifecycleUtil;
    private static DomainLifecycleUtil domainSlaveLifecycleUtil;
    private static final ModelNode hostMaster = new ModelNode();
    private static final ModelNode hostSlave = new ModelNode();
    private static final ModelNode mainOne = new ModelNode();
    private static final ModelNode mainTwo = new ModelNode();
    private static final ModelNode mainThree = new ModelNode();
    private static final ModelNode mainFour = new ModelNode();
    private static final ModelNode otherOne = new ModelNode();
    private static final ModelNode otherTwo = new ModelNode();
    private static final ModelNode otherThree = new ModelNode();
    private static final ModelNode otherFour = new ModelNode();
    private static final Path autoStartMasterDataDir
            = DomainTestSupport.getHostDir(ServerAutoStartTestCase.class.getSimpleName(), "master").toPath()
            .resolve("data")
            .resolve("auto-start");
    private static final Path autoStartSlaveDataDir
            = DomainTestSupport.getHostDir(ServerAutoStartTestCase.class.getSimpleName(), "slave").toPath()
            .resolve("data")
            .resolve("auto-start");

    static {
        // (host=master)
        hostMaster.add("host", "master");
        // (host=master),(server-config=main-one)
        mainOne.add("host", "master");
        mainOne.add("server-config", "main-one");
        // (host=master),(server-config=main-two)
        mainTwo.add("host", "master");
        mainTwo.add("server-config", "main-two");
        // (host=master),(server-config=other-one)
        otherOne.add("host", "master");
        otherOne.add("server-config", "other-one");
        // (host=master),(server-config=other-two)
        otherTwo.add("host", "master");
        otherTwo.add("server-config", "other-two");

        // (host=slave)
        hostSlave.add("host", "slave");
        // (host=slave),(server-config=main-three)
        mainThree.add("host", "slave");
        mainThree.add("server-config", "main-three");
        // (host=slave),(server-config=main-four)
        mainFour.add("host", "slave");
        mainFour.add("server-config", "main-four");
        // (host=slave),(server-config=other-three)
        otherThree.add("host", "slave");
        otherThree.add("server-config", "other-three");
        // (host=slave),(server-config=other-four)
        otherFour.add("host", "slave");
        otherFour.add("server-config", "other-four");

    }

    @BeforeClass
    public static void setupDomain() throws Exception {
        testSupport = DomainTestSupport.createAndStartSupport(DomainTestSupport.Configuration.create(ServerAutoStartTestCase.class.getSimpleName(),
                "domain-configs/domain-minimal.xml", "host-configs/host-master-auto-start.xml", "host-configs/host-slave-auto-start.xml"));
        domainMasterLifecycleUtil = testSupport.getDomainMasterLifecycleUtil();
        domainSlaveLifecycleUtil = testSupport.getDomainSlaveLifecycleUtil();
    }

    @AfterClass
    public static void tearDownDomain() throws Exception {
        testSupport.close();
        testSupport = null;
        domainMasterLifecycleUtil = null;
        domainSlaveLifecycleUtil = null;
    }

    @Test
    public void testDomainLifecycleMethods() throws Throwable {

        DomainClient client = domainMasterLifecycleUtil.getDomainClient();
        executeLifecycleOperation(client, START_SERVERS);
        waitUntilState(client, "master", "main-one", "STARTED");
        waitUntilState(client, "master", "main-two", "STARTED");
        waitUntilState(client, "master", "other-one", "STARTED");
        waitUntilState(client, "master", "other-two", "STARTED");
        waitUntilState(client, "slave", "main-three", "STARTED");
        waitUntilState(client, "slave", "main-four", "STARTED");
        waitUntilState(client, "slave", "other-three", "STARTED");
        waitUntilState(client, "slave", "other-four", "STARTED");
        assertAutoStartStatus(client, mainOne, true);
        assertAutoStartStatus(client, mainTwo, true);
        assertAutoStartStatus(client, otherOne, true);
        assertAutoStartStatus(client, otherTwo, false);
        assertAutoStartStatus(client, mainThree, true);
        assertAutoStartStatus(client, mainFour, true);
        assertAutoStartStatus(client, otherThree, true);
        assertAutoStartStatus(client, otherFour, false);
        assertAutoStartNotUpdated(autoStartMasterDataDir, "main-one");
        assertAutoStartUpdated(autoStartMasterDataDir, "main-two", true);
        assertAutoStartUpdated(autoStartMasterDataDir, "other-one", true);
        assertAutoStartNotUpdated(autoStartMasterDataDir, "other-two");
        assertAutoStartNotUpdated(autoStartSlaveDataDir, "main-three");
        assertAutoStartUpdated(autoStartSlaveDataDir, "main-four", true);
        assertAutoStartUpdated(autoStartSlaveDataDir, "other-three", true);
        assertAutoStartNotUpdated(autoStartSlaveDataDir, "other-four");

        executeLifecycleOperation(client, STOP_SERVERS);
        //When stopped auto-start=true -> STOPPED, auto-start=false -> DISABLED
        waitUntilState(client, "master", "main-one", "STOPPED");
        waitUntilState(client, "master", "main-two", "DISABLED");
        waitUntilState(client, "master", "other-one", "DISABLED");
        waitUntilState(client, "master", "other-two", "DISABLED");
        waitUntilState(client, "slave", "main-three", "STOPPED");
        waitUntilState(client, "slave", "main-four", "DISABLED");
        waitUntilState(client, "slave", "other-three", "DISABLED");
        waitUntilState(client, "slave", "other-four", "DISABLED");
        assertAutoStartStatus(client, mainOne, true);
        assertAutoStartStatus(client, mainTwo, false);
        assertAutoStartStatus(client, otherOne, false);
        assertAutoStartStatus(client, otherTwo, false);
        assertAutoStartStatus(client, mainThree, true);
        assertAutoStartStatus(client, mainFour, false);
        assertAutoStartStatus(client, otherThree, false);
        assertAutoStartStatus(client, otherFour, false);
        assertAutoStartNotUpdated(autoStartMasterDataDir, "main-one");
        assertAutoStartUpdated(autoStartMasterDataDir, "main-two", false);
        assertAutoStartUpdated(autoStartMasterDataDir, "other-one", false);
        assertAutoStartNotUpdated(autoStartMasterDataDir, "other-two");
        assertAutoStartNotUpdated(autoStartSlaveDataDir, "main-three");
        assertAutoStartUpdated(autoStartSlaveDataDir, "main-four", false);
        assertAutoStartUpdated(autoStartSlaveDataDir, "other-three", false);
        assertAutoStartNotUpdated(autoStartSlaveDataDir, "other-four");

        executeLifecycleOperation(client, START_SERVERS);
        waitUntilState(client, "master", "main-one", "STARTED");
        waitUntilState(client, "master", "main-two", "STARTED");
        waitUntilState(client, "master", "other-one", "STARTED");
        waitUntilState(client, "master", "other-two", "STARTED");
        waitUntilState(client, "slave", "main-three", "STARTED");
        waitUntilState(client, "slave", "main-four", "STARTED");
        waitUntilState(client, "slave", "other-three", "STARTED");
        waitUntilState(client, "slave", "other-four", "STARTED");
        assertAutoStartStatus(client, mainOne, true);
        assertAutoStartStatus(client, mainTwo, true);
        assertAutoStartStatus(client, otherOne, true);
        assertAutoStartStatus(client, otherTwo, false);
        assertAutoStartNotUpdated(autoStartMasterDataDir, "main-one");
        assertAutoStartUpdated(autoStartMasterDataDir, "main-two", true);
        assertAutoStartUpdated(autoStartMasterDataDir, "other-one", true);
        assertAutoStartNotUpdated(autoStartMasterDataDir, "other-two");
        assertAutoStartNotUpdated(autoStartSlaveDataDir, "main-three");
        assertAutoStartUpdated(autoStartSlaveDataDir, "main-four", true);
        assertAutoStartUpdated(autoStartSlaveDataDir, "other-three", true);
        assertAutoStartNotUpdated(autoStartSlaveDataDir, "other-four");

        domainMasterLifecycleUtil.stop();
        assertAutoStartNotUpdated(autoStartMasterDataDir, "main-one");
        assertAutoStartUpdated(autoStartMasterDataDir, "main-two", true);
        assertAutoStartUpdated(autoStartMasterDataDir, "other-one", true);
        assertAutoStartNotUpdated(autoStartMasterDataDir, "other-two");

        domainMasterLifecycleUtil.start();
        client = domainMasterLifecycleUtil.getDomainClient();
        waitUntilState(client, "master", "main-one", "STARTED");
        waitUntilState(client, "master", "main-two", "STARTED");
        waitUntilState(client, "master", "other-one", "STARTED");
        //WFCORE-905
        executeFailingBatch(client);
    }

    private void assertAutoStartStatus(final ModelControllerClient client, ModelNode address, boolean autostart) throws IOException {
        final ModelNode operation = Operations.createReadAttributeOperation(address, AUTO_START);
        ModelNode result = validateResponse(client.execute(operation));
        Assert.assertThat(result.asBoolean(), is(autostart));
    }

    private void assertAutoStartNotUpdated(Path dir, String serverName) throws IOException {
        Path startedFile = dir.resolve(serverName + STARTED_EXT);
        Path stoppedFile = dir.resolve(serverName + STOPPED_EXT);
        Assert.assertThat(Files.exists(startedFile), is(false));
        Assert.assertThat(Files.exists(stoppedFile), is(false));
    }

    private void assertAutoStartUpdated(Path dir, String serverName, boolean autostart) throws IOException {
        Path startedFile = dir.resolve(serverName + STARTED_EXT);
        Path stoppedFile = dir.resolve(serverName + STOPPED_EXT);
        if (autostart) {
            Assert.assertThat(Files.exists(startedFile), is(true));
            Assert.assertThat(Files.exists(stoppedFile), is(false));
        } else {
            Assert.assertThat(Files.exists(startedFile), is(false));
            Assert.assertThat(Files.exists(stoppedFile), is(true));
        }
    }

    private void executeLifecycleOperation(final ModelControllerClient client, String opName) throws IOException {
        executeLifecycleOperation(client, null, opName);
    }

    private void executeLifecycleOperation(final ModelControllerClient client, String groupName, String opName) throws IOException {
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(opName);
        if (groupName == null) {
            operation.get(OP_ADDR).setEmptyList();
        } else {
            operation.get(OP_ADDR).add(SERVER_GROUP, groupName);
        }
        validateResponse(client.execute(operation));
    }

    private void executeFailingBatch(final ModelControllerClient client) throws IOException {
        ModelNode composite = Util.createEmptyOperation(COMPOSITE, PathAddress.EMPTY_ADDRESS);
        ModelNode steps = composite.get(STEPS).setEmptyList();

        ModelNode stopServerOne = Util.createOperation(STOP, PathAddress.pathAddress(mainOne));
        stopServerOne.get(BLOCKING).set(true);
        steps.add(stopServerOne);

        ModelNode removeServerOne = Util.createRemoveOperation(PathAddress.pathAddress(mainOne));
        steps.add(removeServerOne);
        validateResponse(client.execute(composite));
    }
}
