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

package org.jboss.as.controller.operations;

import org.jboss.as.controller.OperationContext;
import org.jboss.dmr.ModelNode;

/**
 * Class that is used to transform an operation before it is send to other servers in the domain.
 *
 * The main use case for this is replacing content addition parameters with a hash into the content
 * repository.
 *
 *
 * @author Stuart Douglas
 * @see OperationAttachments#SLAVE_SERVER_OPERATION_TRANSMUTERS
 * @see  CompositeOperationAwareTransformer
 *
 */
public interface DomainOperationTransmuter {

    ModelNode transmmute(final OperationContext context, final ModelNode operation);

}
