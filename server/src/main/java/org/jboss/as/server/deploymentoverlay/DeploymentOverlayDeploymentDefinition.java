/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.as.server.deploymentoverlay;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT_OVERLAY;
import static org.jboss.as.server.deploymentoverlay.DeploymentOverlayModel.DEPLOYMENT_OVERRIDE_DEPLOYMENT_PATH;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.common.ControllerResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * Links a deployment overlay to a deployment
 *
 * @author Stuart Douglas
 */
public class DeploymentOverlayDeploymentDefinition extends SimpleResourceDefinition {

    private static final AttributeDefinition[] ATTRIBUTES = {};

    public static AttributeDefinition[] attributes() {
        return ATTRIBUTES.clone();
    }

    public DeploymentOverlayDeploymentDefinition() {
        super(new Parameters(DEPLOYMENT_OVERRIDE_DEPLOYMENT_PATH, ControllerResolver.getResolver(DEPLOYMENT_OVERLAY + '.' + DEPLOYMENT))
                .setAddHandler(new DeploymentOverlayDeploymentAdd()));
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        for (AttributeDefinition attr : ATTRIBUTES) {
            resourceRegistration.registerReadOnlyAttribute(attr, null);
        }
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(DeploymentOverlayDeploymentRemoveHandler.REMOVE_DEFINITION, DeploymentOverlayDeploymentRemoveHandler.INSTANCE);
    }
}
