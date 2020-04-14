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

package org.jboss.as.controller.operations.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jboss.as.controller.ExpressionResolver;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.logging.ControllerLogger;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * {@link org.jboss.as.controller.operations.validation.ParameterValidator} that validates the value is a string matching
 * one of the {@link java.lang.Enum} types.
 *
 * @author Jason T. Greene
 * @author Brian Stansberry
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class EnumValidator<E extends Enum<E>> extends ModelTypeValidator implements AllowedValuesValidator {

    private final EnumSet<E> allowedValues;
    private final Class<E> enumType;
    private final Map<String, E> toStringMap = new HashMap<String, E>();

    /**
     * Creates a validator where the specified enum values are allowed
     * @param enumType the type of the enum
     * @param allowed  the allowed values. Cannot be {@code null}
     */
    @SafeVarargs
    public EnumValidator(final Class<E> enumType, final E... allowed) {
        this(enumType, setOf(enumType, allowed), true);
    }

    /**
     * Creates a validator where the specified enum values are allowed
     * @param enumType the type of the enum
     * @param allowed  the allowed values. Cannot be {@code null}
     */
    public EnumValidator(final Class<E> enumType, final EnumSet<E> allowed) {
        this(enumType, allowed, false);
    }

    /**
     * Creates a validator where the specified enum values are allowed
     * @param enumType the type of the enum
     * @param allowed  the allowed values. Cannot be {@code null} or empty
     * @param safe {@code true} if the inputs have been validated and the set is an internally created one
     */
    private EnumValidator(final Class<E> enumType, final EnumSet<E> allowed, boolean safe) {
        super(ModelType.STRING);
        if (safe) {
            this.enumType = enumType;
            this.allowedValues = allowed;
        } else {
            assert enumType != null;
            assert allowed != null;
            this.enumType = enumType;
            this.allowedValues = EnumSet.copyOf(allowed);
        }
        for (E value : allowedValues) {
            toStringMap.put(value.toString(), value);
        }
    }

    /** @deprecated use {@link #EnumValidator(Class, Enum[])} with {@link EnumSet#allOf(Class)} since {@link org.jboss.as.controller.AttributeDefinition} handles the nullable and expressions checks.*/
    @SafeVarargs
    @Deprecated
    @SuppressWarnings("deprecation")
    public EnumValidator(final Class<E> enumType, final boolean nullable, final E... allowed) {
        this(enumType, nullable, false, allowed);
    }

    /** @deprecated use {@link #EnumValidator(Class, EnumSet)} with {@link EnumSet#allOf(Class)} since {@link org.jboss.as.controller.AttributeDefinition} handles the nullable and expression checks.*/
    @Deprecated
    public EnumValidator(final Class<E> enumType,  final boolean nullable, final boolean allowExpressions) {
        super(ModelType.STRING, nullable, allowExpressions);
        assert  enumType != null;
        this.enumType = enumType;
        this.allowedValues = EnumSet.allOf(enumType);
        for (E value : allowedValues) {
            toStringMap.put(value.toString(), value);
        }
    }

    /** @deprecated use {@link #EnumValidator(Class, Enum[])} with {@link EnumSet#allOf(Class)} since {@link org.jboss.as.controller.AttributeDefinition} handles the nullable and expression checks.*/
    @SafeVarargs
    @Deprecated
    public EnumValidator(final Class<E> enumType,  final boolean nullable, final boolean allowExpressions, final E... allowed) {
        super(ModelType.STRING, nullable, allowExpressions);
        this.enumType = enumType;
        this.allowedValues = setOf(enumType, allowed);
        for (E value : allowed) {
            toStringMap.put(value.toString(), value);
        }
    }

    @SafeVarargs
    private static <T extends Enum<T>> EnumSet<T> setOf(Class<T> enumType, T... allowed) {
        assert  enumType != null;
        assert  allowed != null;
        EnumSet<T> set = EnumSet.noneOf(enumType);
        Collections.addAll(set, allowed);
        return set;
    }

    /**
     * Creates a new validator for the enum type with the allowed values defined in the {@code allowed} parameter.
     *
     * @param enumType the type of the enum.
     * @param allowed  the enum values that are allowed. If {@code null} or zero length this is interpreted as meaning all values
     * @param <E>      the type of the enum.
     *
     * @return a new validator.
     */
    @SafeVarargs
    public static <E extends Enum<E>> EnumValidator<E> create(final Class<E> enumType, final E... allowed) {
        if (allowed == null || allowed.length == 0) {
            return create(enumType, EnumSet.allOf(enumType));
        }
        return new EnumValidator<E>(enumType, allowed);
    }

    /**
     * Creates a new validator for the enum type with the allowed values defined in the {@code allowed} parameter.
     *
     * @param enumType the type of the enum.
     * @param allowed  the enum values that are allowed.
     * @param <E>      the type of the enum.
     *
     * @return a new validator.
     */
    public static <E extends Enum<E>> EnumValidator<E> create(final Class<E> enumType, final EnumSet<E> allowed) {
        return new EnumValidator<E>(enumType, allowed);
    }

    /**
     * Creates a new validator for the enum type with the allowed values defined in the {@code allowed} parameter.
     *
     * @param enumType the type of the enum.
     * @param nullable {@code true} if the value is allowed to be {@code null}, otherwise {@code false}.
     * @param allowed  the enum values that are allowed.
     * @param <E>      the type of the enum.
     *
     * @return a new validator.
     * @deprecated use {@link #create(Class, Enum[])} since {@link org.jboss.as.controller.AttributeDefinition} handles the nullable check.
     */
    @SafeVarargs
    @Deprecated
    @SuppressWarnings("deprecation")
    public static <E extends Enum<E>> EnumValidator<E> create(final Class<E> enumType, final boolean nullable, final E... allowed) {
        return new EnumValidator<E>(enumType, nullable, allowed);
    }

    /**
     * Creates a new validator for the enum type with all values of the enum allowed.
     *
     * @param enumType         the type of the enum.
     * @param nullable         {@code true} if the value is allowed to be {@code null}, otherwise {@code false}.
     * @param allowExpressions {@code true} if an expression is allowed to define the value, otherwise {@code false}.
     * @param <E>              the type of the enum.
     *
     * @return a new validator.
     * @deprecated use {@link #create(Class, EnumSet)} with {@link EnumSet#allOf(Class)} since {@link org.jboss.as.controller.AttributeDefinition} handles the nullable and expression checks.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static <E extends Enum<E>> EnumValidator<E> create(final Class<E> enumType, final boolean nullable, final boolean allowExpressions) {
        return new EnumValidator<E>(enumType, nullable, allowExpressions);
    }

    /**
     * Creates a new validator for the enum type with the allowed values defined in the {@code allowed} parameter.
     *
     * @param enumType         the type of the enum.
     * @param nullable         {@code true} if the value is allowed to be {@code null}, otherwise {@code false}.
     * @param allowExpressions {@code true} if an expression is allowed to define the value, otherwise {@code false}.
     * @param allowed          the enum values that are allowed.
     * @param <E>              the type of the enum.
     *
     * @return a new validator.
     * @deprecated use {@link #create(Class, Enum[])} since {@link org.jboss.as.controller.AttributeDefinition} handles the nullable and expression checks.
     */
    @SafeVarargs
    @Deprecated
    @SuppressWarnings("deprecation")
    public static <E extends Enum<E>> EnumValidator<E> create(final Class<E> enumType, final boolean nullable, final boolean allowExpressions, final E... allowed) {
        return new EnumValidator<E>(enumType, nullable, allowExpressions, allowed);
    }

    @Override
    public void validateParameter(String parameterName, ModelNode value) throws OperationFailedException {
        super.validateParameter(parameterName, value);
        ModelType type = value.getType();
        if (type == ModelType.STRING ) {
            String tuString = ExpressionResolver.SIMPLE.resolveExpressions(value).asString(); // Sorry, no support for resolving against vault!
            E enumValue = toStringMap.get(tuString);
            if (enumValue == null) {
                try {
                    enumValue = Enum.valueOf(enumType, tuString.toUpperCase(Locale.ENGLISH));
                } catch (IllegalArgumentException e) {
                    throw ControllerLogger.ROOT_LOGGER.invalidEnumValue(tuString, parameterName, toStringMap.keySet());
                }
            }
            if (!allowedValues.contains(enumValue)) {
                throw ControllerLogger.ROOT_LOGGER.invalidEnumValue(tuString, parameterName, toStringMap.keySet());
            }
            // Hack to store the allowed value in the model, not the user input
            if (!value.isProtected()) {
                value.set(enumValue.toString());
            }
        }
    }

    @Override
    public List<ModelNode> getAllowedValues() {
        List<ModelNode> result = new ArrayList<ModelNode>();
        for (E value : allowedValues) {
            if (value.toString() != null) {
                result.add(new ModelNode().set(value.toString()));
            } else {
                result.add(new ModelNode().set(value.name()));
            }
        }
        return result;
    }
}
