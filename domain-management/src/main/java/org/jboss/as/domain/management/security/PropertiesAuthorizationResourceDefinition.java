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

package org.jboss.as.domain.management.security;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.common.ControllerResolver;
import org.jboss.as.controller.registry.OperationEntry;

/**
 * {@link org.jboss.as.controller.ResourceDefinition} for a management security realm's properties-file-based authorization resource.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class PropertiesAuthorizationResourceDefinition extends PropertiesFileResourceDefinition {

    public static final AttributeDefinition[] ATTRIBUTE_DEFINITIONS = { PATH, RELATIVE_TO };

    public PropertiesAuthorizationResourceDefinition() {
        super(new Parameters(PathElement.pathElement(ModelDescriptionConstants.AUTHORIZATION, ModelDescriptionConstants.PROPERTIES),
                ControllerResolver.getDeprecatedResolver(SecurityRealmResourceDefinition.DEPRECATED_PARENT_CATEGORY,
                          "core.management.security-realm.authorization.properties"))
                .setAddHandler(new SecurityRealmChildAddHandler(false, true, ATTRIBUTE_DEFINITIONS))
                .setAddRestartLevel(OperationEntry.Flag.RESTART_ALL_SERVICES)
                .setRemoveHandler(new SecurityRealmChildRemoveHandler(false))
                .setRemoveRestartLevel(OperationEntry.Flag.RESTART_ALL_SERVICES)
                .setDeprecatedSince(ModelVersion.create(1, 7)));
    }

    @Override
    protected AttributeDefinition[] getAttributeDefinitions() {
        return ATTRIBUTE_DEFINITIONS;
    }

}
