/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.as.domain.management.security.realms;

import static org.jboss.as.domain.management.security.realms.LdapTestSuite.HOST_NAME;
import static org.jboss.as.domain.management.security.realms.LdapTestSuite.MASTER_LDAP_PORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.sasl.RealmCallback;

import org.jboss.as.core.security.RealmGroup;
import org.jboss.as.core.security.SimplePrincipal;
import org.jboss.as.core.security.SubjectUserInfo;
import org.jboss.as.domain.management.AuthMechanism;
import org.jboss.as.domain.management.AuthorizingCallbackHandler;
import org.jboss.as.domain.management.SecurityRealm;
import org.jboss.as.domain.management.connections.ldap.LdapConnectionManager;
import org.jboss.as.domain.management.connections.ldap.LdapConnectionManagerService;
import org.jboss.as.domain.management.security.operations.OutboundConnectionAddBuilder;
import org.jboss.dmr.ModelNode;
import org.wildfly.security.auth.callback.EvidenceVerifyCallback;
import org.wildfly.security.evidence.PasswordGuessEvidence;

/**
 * A base class for all LDAP test to allow the server to be initialised if
 * being executed outside of the suite.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public abstract class BaseLdapSuiteTest extends SecurityRealmTestBase {

    protected static final String MASTER_CONNECTION_NAME = "MasterConnection";

    @Override
    protected void addBootOperations(List<ModelNode> bootOperations) throws Exception {
        addAddOutboundConnectionOperations(bootOperations);

        super.addBootOperations(bootOperations);
    }

    protected void addAddOutboundConnectionOperations(List<ModelNode> bootOperations) throws Exception {
        bootOperations.add(OutboundConnectionAddBuilder.builder(MASTER_CONNECTION_NAME)
                .setUrl("ldap://" + HOST_NAME + ":" + MASTER_LDAP_PORT)
                .setSearchDn("uid=wildfly,dc=simple,dc=wildfly,dc=org")
                .setSearchCredential("wildfly_password")
                .build());
    }

    private AuthorizingCallbackHandler getAuthorizingCallbackHandler(final String realmName) {
        SecurityRealm realm;
        if (TEST_REALM.equals(realmName)) {
            realm = securityRealm;
        } else {
            realm = SecurityRealmHelper.getSecurityRealm(getContainer(), SecurityRealm.ServiceUtil.createServiceName(realmName));
        }

        return realm.getAuthorizingCallbackHandler(AuthMechanism.PLAIN);
    }

    private Set<RealmGroup> getUsersGroups(final String realmName, final String userName, final String password) throws Exception {
        AuthorizingCallbackHandler cbh = getAuthorizingCallbackHandler(realmName);

        NameCallback ncb = new NameCallback("Username", userName);
        RealmCallback rcb = new RealmCallback("Realm", TEST_REALM);
        EvidenceVerifyCallback ecb = new EvidenceVerifyCallback(new PasswordGuessEvidence(password.toCharArray()));

        cbh.handle(new Callback[] { ncb, rcb, ecb });

        assertTrue("Password verified", ecb.isVerified());

        Principal user = new SimplePrincipal(userName);
        Collection<Principal> principals = Collections.singleton(user);
        SubjectUserInfo userInfo = cbh.createSubjectUserInfo(principals);

        return userInfo.getSubject().getPrincipals(RealmGroup.class);
    }

    protected LdapConnectionManager getConnectionManager(final String name) {
        return (LdapConnectionManager) getContainer().getService(LdapConnectionManagerService.ServiceUtil.createServiceName(name)).getValue();
    }

    protected void verifyGroupMembership(final String realmName, final String userName, final String password, final String... groups) throws Exception {
        Set<RealmGroup> groupPrincipals = getUsersGroups(realmName, userName, password);
        assertEquals("Number of groups", groups.length, groupPrincipals.size());
        Collection<String> expectedGroups = new HashSet<String>(Arrays.asList(groups));
        for (RealmGroup current : groupPrincipals) {
            assertTrue(String.format("User not expected to be in group '%s'", current.getName()),
                    expectedGroups.remove(current.getName()));
        }
        assertTrue(String.format("User not in expected groups '%s'", expectedGroups.toString()), expectedGroups.isEmpty());
    }

}
