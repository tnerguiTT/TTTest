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

package org.jboss.as.cli.parsing;

import org.jboss.as.cli.CommandFormatException;

/**
 * @author Alexey Loubyansky
 *
 */
public class ExpressionBaseState extends DefaultParsingState {

    private final boolean resolveSystemProperties;
    private final boolean exceptionIfNotResolved;

    private CharacterHandler resolvingEntranceHandler = new CharacterHandler() {
        @Override
        public void handle(ParsingContext ctx) throws CommandFormatException {
            ctx.resolveExpression(resolveSystemProperties, exceptionIfNotResolved);
            ExpressionBaseState.super.getEnterHandler().handle(ctx);
        }};

    public ExpressionBaseState(String id) {
        this(id, true);
    }

    public ExpressionBaseState(String id, boolean enterLeaveContent, CharacterHandlerMap enterStateHandlers) {
        this(id, enterLeaveContent, enterStateHandlers, true);
    }

    public ExpressionBaseState(String id, boolean enterLeaveContent, CharacterHandlerMap enterStateHandlers, boolean resolveSystemProperties) {
        super(id, enterLeaveContent, enterStateHandlers);
        this.resolveSystemProperties = resolveSystemProperties;
        this.exceptionIfNotResolved = true;
        putExpressionHandler();
    }

    public ExpressionBaseState(String id, boolean resolveSystemProperties) {
        this(id, resolveSystemProperties, true);
    }

    public ExpressionBaseState(String id, boolean resolveSystemProperties, boolean exceptionIfNotResolved) {
        super(id);
        this.resolveSystemProperties = resolveSystemProperties;
        this.exceptionIfNotResolved = exceptionIfNotResolved;
        putExpressionHandler();
    }

    public ExpressionBaseState(String id, boolean resolveSystemProperties, boolean exceptionIfNotResolved, boolean enterLeaveContent) {
        super(id, enterLeaveContent);
        this.resolveSystemProperties = resolveSystemProperties;
        this.exceptionIfNotResolved = exceptionIfNotResolved;
        putExpressionHandler();
    }

    protected void putExpressionHandler() {
        this.putHandler('$', new CharacterHandler(){
            @Override
            public void handle(ParsingContext ctx)
                    throws CommandFormatException {
                final int originalLength = ctx.getInput().length();
                ctx.resolveExpression(resolveSystemProperties, exceptionIfNotResolved);
                final char resolvedCh = ctx.getCharacter();
                if(resolvedCh == '$' && originalLength == ctx.getInput().length()) {
                    getDefaultHandler().handle(ctx);
                } else {
                    getHandler(resolvedCh).handle(ctx);
                }
            }});
    }

    @Override
    public CharacterHandler getEnterHandler() {
        return resolvingEntranceHandler;
    }
}
