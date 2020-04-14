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
package org.jboss.as.domain.management.audit;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.JSON_FORMATTER;
import static org.jboss.as.domain.management.audit.AuditLogHandlerResourceDefinition.HANDLER_TYPES;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.domain.management.logging.DomainManagementLogger;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 */
public class HandlerUtil {

    static void checkNoOtherHandlerWithTheSameName(OperationContext context) throws OperationFailedException {
        final PathAddress address = context.getCurrentAddress();
        final PathAddress parentAddress = address.subAddress(0, address.size() - 1);
        final Resource resource = context.readResourceFromRoot(parentAddress);

        final PathElement element = address.getLastElement();
        final String handlerType = element.getKey();
        final String handlerName = element.getValue();

        for (String otherHandler: HANDLER_TYPES) {
            if (handlerType.equals(otherHandler)) {
                // we need to check other handler types for the same name
                continue;
            }
            final PathElement check = PathElement.pathElement(otherHandler, handlerName);
            if (resource.hasChild(check)) {
                throw DomainManagementLogger.ROOT_LOGGER.handlerAlreadyExists(check.getValue(), parentAddress.append(check));
            }
        }
    }

    static boolean lookForHandler(final OperationContext context, final PathAddress addr, final String name) {
        final PathAddress subAddress = addr.subAddress(0, addr.size() - 2);
        final Resource root = context.readResourceFromRoot(PathAddress.EMPTY_ADDRESS);

        for (String handlerType: HANDLER_TYPES) {
            final PathAddress referenceAddress = subAddress.append(handlerType, name);
            if (lookForResource(root, referenceAddress)) {
                return true;
            }
        }
        return false;
    }

    static boolean lookForFormatter(OperationContext context, PathAddress addr, String name) {
        PathAddress referenceAddress = addr.subAddress(0, addr.size() - 1).append(JSON_FORMATTER, name);
        final Resource root = context.readResourceFromRoot(PathAddress.EMPTY_ADDRESS);
        return lookForResource(root, referenceAddress);
    }

    private static boolean lookForResource(final Resource root, final PathAddress pathAddress) {
        Resource current = root;
        for (PathElement element : pathAddress) {
            current = current.getChild(element);
            if (current == null) {
                return false;
            }
        }
        return true;
    }
}
