/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace.event;

import java.util.stream.IntStream;

import org.eclipse.tracecompass.ctf.core.event.types.AbstractArrayDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.ByteArrayDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.CompoundDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StringDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDefinition;

import com.efficios.jabberwocky.trace.event.field.ArrayValue;
import com.efficios.jabberwocky.trace.event.field.EnumValue;
import com.efficios.jabberwocky.trace.event.field.FloatValue;
import com.efficios.jabberwocky.trace.event.field.IFieldValue;
import com.efficios.jabberwocky.trace.event.field.IntegerValue;
import com.efficios.jabberwocky.trace.event.field.StringValue;
import com.efficios.jabberwocky.trace.event.field.StructValue;
import com.google.common.collect.ImmutableMap;

public final class CtfTraceEventFieldParser {

    private CtfTraceEventFieldParser() {}

    public static IFieldValue parseField(IDefinition fieldDef) {

        if (fieldDef instanceof IntegerDefinition) {
            IntegerDefinition integerDef = (IntegerDefinition) fieldDef;
            int base = integerDef.getDeclaration().getBase();
            return new IntegerValue(integerDef.getValue(), base, null);

        } else if (fieldDef instanceof FloatDefinition) {
            FloatDefinition floatDef = (FloatDefinition) fieldDef;
            return new FloatValue(floatDef.getValue(), null);

        } else if (fieldDef instanceof StringDefinition) {
            StringDefinition stringDef = (StringDefinition) fieldDef;
            return new StringValue(stringDef.getValue(), null);

        } else if (fieldDef instanceof AbstractArrayDefinition) {
            AbstractArrayDefinition arrayDef = (AbstractArrayDefinition) fieldDef;
            IDeclaration decl = arrayDef.getDeclaration();
            if (!(decl instanceof CompoundDeclaration)) {
                throw new IllegalArgumentException("Array definitions should only come from sequence or array declarations"); //$NON-NLS-1$
            }
            CompoundDeclaration arrDecl = (CompoundDeclaration) decl;
            IDeclaration elemType = null;
            elemType = arrDecl.getElementType();
            if (elemType instanceof IntegerDeclaration) {
                /*
                 * Array of integers => either an array of integers values or a
                 * string.
                 */
                IntegerDeclaration elemIntType = (IntegerDeclaration) elemType;
                /* Are the integers characters and encoded? */
                if (elemIntType.isCharacter()) {
                    /* it's a banal string */
                    return new StringValue(arrayDef.toString(), null);

                } else if (arrayDef instanceof ByteArrayDefinition) {
                    /*
                     * Unsigned byte array, consider this field an array of integers
                     */
                    ByteArrayDefinition byteArrayDefinition = (ByteArrayDefinition) arrayDef;
                    IntegerValue[] elements = IntStream.range(0, arrayDef.getLength())
                            .mapToLong(idx -> Byte.toUnsignedLong(byteArrayDefinition.getByte(idx)))
                            .mapToObj(longVal -> new IntegerValue(longVal, elemIntType.getBase(), null))
                            .toArray(IntegerValue[]::new);
                    return new ArrayValue<>(elements, null);

                } else {
                    /* Consider this a straight array of integers */
                    IntegerValue[] elements = arrayDef.getDefinitions().stream()
                            .filter(elem -> elem != null)
                            .map(elem -> {
                                IntegerDefinition integerDef = (IntegerDefinition) elem;
                                long value = integerDef.getValue();
                                int base = integerDef.getDeclaration().getBase();
                                return new IntegerValue(value, base, null);
                            })
                            .toArray(IntegerValue[]::new);
                    return new ArrayValue<>(elements, null);
                }

            } else {
                /* Arrays of elements of any other type */
                IFieldValue[] elements = arrayDef.getDefinitions().stream()
                        .map(CtfTraceEventFieldParser::parseField)
                        .toArray(IFieldValue[]::new);
                return new ArrayValue<>(elements, null);
            }

        } else if (fieldDef instanceof ICompositeDefinition) {
            ICompositeDefinition compositeDef = (ICompositeDefinition) fieldDef;
            /*
             * Recursively parse the fields, and save the results in a struct value.
             */
            ImmutableMap.Builder<String, IFieldValue> structElements = ImmutableMap.builder();
            compositeDef.getFieldNames().forEach(curFieldName -> {
                IDefinition curFieldDef = compositeDef.getDefinition(curFieldName);
                IFieldValue curFieldValue = CtfTraceEventFieldParser.parseField(curFieldDef);
                structElements.put(curFieldName, curFieldValue);
            });
            return new StructValue(structElements.build(), null);

        } else if (fieldDef instanceof EnumDefinition) {
            EnumDefinition enumDef = (EnumDefinition) fieldDef;
            return new EnumValue(enumDef.getValue(), enumDef.getIntegerValue().longValue(), null);

        } else if (fieldDef instanceof VariantDefinition) {
            VariantDefinition variantDef = (VariantDefinition) fieldDef;
            // variantDef.getCurrentFieldName();
            IDefinition curFieldDef = variantDef.getCurrentField();
            // It's as simple as that??
            return CtfTraceEventFieldParser.parseField(curFieldDef);

        } else {
            /* This field type is not supported */
            throw new IllegalArgumentException("Field type: " + fieldDef.getClass().toString()
                    + " is not supported.");
        }
    }

}
