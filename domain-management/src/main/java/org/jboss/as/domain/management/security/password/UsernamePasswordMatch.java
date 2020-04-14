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
package org.jboss.as.domain.management.security.password;

import org.jboss.as.domain.management.logging.DomainManagementLogger;

/**
 * A {@link PasswordRestriction} to verify that the username and password and not equal.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class UsernamePasswordMatch implements PasswordRestriction {

    private final boolean must;

    public UsernamePasswordMatch(final boolean must) {
        this.must = must;
    }

    @Override
    public String getRequirementMessage() {
        if (must) {
            return DomainManagementLogger.ROOT_LOGGER.passwordUsernameMustMatchInfo();
        }
        return DomainManagementLogger.ROOT_LOGGER.passwordUsernameShouldMatchInfo();
    }

    @Override
    public void validate(String userName, String password) throws PasswordValidationException {
        if (userName.equals(password)) {
            throw must ? DomainManagementLogger.ROOT_LOGGER.passwordUsernameMatchError() : DomainManagementLogger.ROOT_LOGGER.passwordUsernameShouldNotMatch();
        }
    }

}
