/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

package org.jboss.as.test.integration.management.cli.extensions;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.ModelOnlyAddStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.NonResolvingResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;


/**
 *
 * @author Alexey Loubyansky
 */
public class CliExtCommandsSubsystemResourceDescription extends SimpleResourceDefinition {


    public static final PathElement PATH = PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM, CliExtCommandsExtension.SUBSYSTEM_NAME);

    public CliExtCommandsSubsystemResourceDescription() {
        super(PATH, new NonResolvingResourceDescriptionResolver(), new ModelOnlyAddStepHandler(), new AbstractRemoveStepHandler(){});
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
    }
}
