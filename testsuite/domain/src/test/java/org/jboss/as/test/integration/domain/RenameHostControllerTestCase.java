/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.test.integration.domain;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.test.integration.domain.management.util.DomainLifecycleUtil;
import org.jboss.as.test.integration.domain.management.util.DomainTestSupport;
import org.jboss.as.test.integration.domain.management.util.DomainTestUtils;
import org.jboss.as.test.integration.domain.suites.DomainTestSuite;
import org.jboss.dmr.ModelNode;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Verifies we are able to rename a Host Controller and, after reloading it, it gets registered in the domain with the new name.
 *
 * @author Yeray Borges
 */
public class RenameHostControllerTestCase {
    private static final String RENAMED_SLAVE = "renamed-slave";
    private static final PathAddress SLAVE_ADDR = PathAddress.pathAddress(HOST, "slave");
    private static final PathAddress MASTER_ADDR = PathAddress.pathAddress(HOST, "master");
    private static final PathAddress RENAMED_SLAVE_ADDR = PathAddress.pathAddress(HOST, RENAMED_SLAVE);

    private static DomainTestSupport testSupport;
    private static DomainLifecycleUtil domainMasterLifecycleUtil;
    private static DomainLifecycleUtil domainSlaveLifecycleUtil;

    @BeforeClass
    public static void setupDomain() {
        testSupport = DomainTestSuite.createSupport(RenameHostControllerTestCase.class.getSimpleName());
        domainMasterLifecycleUtil = testSupport.getDomainMasterLifecycleUtil();
        domainSlaveLifecycleUtil = testSupport.getDomainSlaveLifecycleUtil();

    }

    @AfterClass
    public static void tearDownDomain() {
        testSupport = null;
        domainMasterLifecycleUtil = null;
        domainSlaveLifecycleUtil = null;
        DomainTestSuite.stopSupport();
    }

    @Test
    public void renameSlave() throws Exception {
        DomainClient masterClient = domainMasterLifecycleUtil.getDomainClient();

        ModelNode operation = Util.getWriteAttributeOperation(SLAVE_ADDR, "name", RENAMED_SLAVE);
        DomainTestUtils.executeForResult(operation, masterClient);

        DomainClient slaveClient = reloadHost(domainSlaveLifecycleUtil, "slave");

        String result = DomainTestUtils.executeForResult(
                Util.getReadAttributeOperation(PathAddress.EMPTY_ADDRESS, "local-host-name"), slaveClient).asString();

        Assert.assertEquals(RENAMED_SLAVE, result);

        // verify all is running, it also verifies the slave is registered in the domain with the new name
        result = DomainTestUtils.executeForResult(
                Util.getReadAttributeOperation(RENAMED_SLAVE_ADDR, "host-state"), masterClient).asString();

        Assert.assertEquals("running", result);

        result = DomainTestUtils.executeForResult(
                Util.getReadAttributeOperation(MASTER_ADDR, "host-state"), masterClient).asString();

        Assert.assertEquals("running", result);
    }

    private DomainClient reloadHost(DomainLifecycleUtil util, String host) throws Exception {
        ModelNode reload = Util.createEmptyOperation("reload", getRootAddress(host));
        util.executeAwaitConnectionClosed(reload);
        util.connect();
        util.getConfiguration().setHostName(RENAMED_SLAVE);
        util.awaitHostController(System.currentTimeMillis());
        return util.createDomainClient();
    }

    private PathAddress getRootAddress(String host) {
        return host == null ? PathAddress.EMPTY_ADDRESS : PathAddress.pathAddress(HOST, host);
    }
}
