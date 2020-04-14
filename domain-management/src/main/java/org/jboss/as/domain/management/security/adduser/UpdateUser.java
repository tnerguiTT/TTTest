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

package org.jboss.as.domain.management.security.adduser;

import org.jboss.as.domain.management.logging.DomainManagementLogger;

/**
 * Describe the purpose
 *
 * @author <a href="mailto:flemming.harms@gmail.com">Flemming Harms</a>
 */
public class UpdateUser extends UpdatePropertiesHandler implements State {

    private final StateValues stateValues;
    private final ConsoleWrapper theConsole;

    public UpdateUser(ConsoleWrapper theConsole,final StateValues stateValues) {
        super(theConsole);
        this.theConsole = theConsole;
        this.stateValues = stateValues;
    }

    @Override
    public State execute() {
        State nextState = update(stateValues);

        /*
         * If this is interactive mode, the password is not null (enable/disable mode) and no error occurred
         * offer to display the Base64 password of the user - otherwise the util can end.
         */
        if (null == nextState && null != stateValues.getPassword() && (stateValues.isInteractive() || !stateValues.isSilent() && stateValues.isDisplaySecret())) {
            nextState = (stateValues.isDisplaySecret()) ?
                    new DisplaySecret(theConsole, stateValues):
                    new ConfirmationChoice(theConsole, DomainManagementLogger.ROOT_LOGGER.serverUser(), DomainManagementLogger.ROOT_LOGGER.yesNo(), new DisplaySecret(theConsole, stateValues), null);

        }
        return nextState;
    }

    @Override
    String consoleUserMessage(String fileName) {
        return DomainManagementLogger.ROOT_LOGGER.updateUser(stateValues.getUserName(), fileName);
    }

    @Override
    String consoleGroupsMessage(String fileName) {
        return DomainManagementLogger.ROOT_LOGGER.updatedGroups(stateValues.getUserName(), stateValues.getGroups(), fileName);
    }

    @Override
    String errorMessage(String fileName, Throwable e) {
        return DomainManagementLogger.ROOT_LOGGER.unableToUpdateUser(fileName, e.getMessage());
    }
}
