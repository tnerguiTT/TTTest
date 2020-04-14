/*
 *
 *  JBoss, Home of Professional Open Source.
 *  Copyright 2014, Red Hat, Inc., and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 * /
 */

package org.jboss.as.controller.test;

import static org.jboss.as.controller.test.TestUtils.createAttribute;
import static org.jboss.as.controller.test.TestUtils.createMetric;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ModelOnlyWriteAttributeHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PrimitiveListAttributeDefinition;
import org.jboss.as.controller.ResourceBuilder;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.NonResolvingResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.test.AbstractGlobalOperationsTestCase.TestMetricHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author Tomaz Cerar (c) 2014 Red Hat Inc.
 */
public class Subsystem1RootResource extends SimpleResourceDefinition {

    public Subsystem1RootResource() {
        super(PathElement.pathElement("subsystem", "subsystem1"), new NonResolvingResourceDescriptionResolver());
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration profileSub1Reg) {
        super.registerAttributes(profileSub1Reg);

        profileSub1Reg.registerReadOnlyAttribute(new PrimitiveListAttributeDefinition.Builder("attr1", ModelType.INT).setRequired(true).build(), null);

        profileSub1Reg.registerReadOnlyAttribute(createAttribute("read-only", ModelType.INT, null, false, false, true), null);
        final AttributeDefinition attribute = createAttribute("read-write", ModelType.INT, null, false, false, true);
        profileSub1Reg.registerReadWriteAttribute(attribute, null, new ModelOnlyWriteAttributeHandler(attribute));
        profileSub1Reg.registerMetric(createMetric("metric1", ModelType.INT), TestMetricHandler.INSTANCE);
        profileSub1Reg.registerMetric(createMetric("metric2", ModelType.INT), TestMetricHandler.INSTANCE);


    }

    @Override
    public void registerChildren(ManagementResourceRegistration profileSub1Reg) {
        super.registerChildren(profileSub1Reg);
        ResourceDefinition profileSub1RegType1Def = ResourceBuilder.Factory.create(PathElement.pathElement("type1", "*"),
                new NonResolvingResourceDescriptionResolver())
                .addReadOnlyAttribute(createAttribute("name", ModelType.STRING))
                .addReadOnlyAttribute(createAttribute("value", ModelType.INT))
                .build();
        profileSub1Reg.registerSubModel(profileSub1RegType1Def);

        ResourceDefinition profileSub1RegType2Def = ResourceBuilder.Factory.create(PathElement.pathElement("type2", "other"),
                new NonResolvingResourceDescriptionResolver())
                .addReadOnlyAttribute(createAttribute("name", ModelType.STRING))
                .addReadOnlyAttribute(SimpleAttributeDefinitionBuilder.create("default", ModelType.STRING).setRequired(false).setDefaultValue(new ModelNode("Default string")).build())
                .build();
        profileSub1Reg.registerSubModel(profileSub1RegType2Def);
    }
}
