/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.js.runtime.array.dyn;

import static com.oracle.truffle.js.runtime.builtins.JSAbstractArray.arraySetArray;
import static com.oracle.truffle.js.runtime.builtins.JSAbstractArray.arraySetLength;
import static com.oracle.truffle.js.runtime.builtins.JSAbstractArray.arraySetUsedLength;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.JSConfig;
import com.oracle.truffle.js.runtime.array.ScriptArray;

public final class ZeroBasedDoubleArray extends AbstractDoubleArray {

    private static final ZeroBasedDoubleArray ZERO_BASED_DOUBLE_ARRAY = new ZeroBasedDoubleArray(INTEGRITY_LEVEL_NONE, createCache());

    public static ZeroBasedDoubleArray makeZeroBasedDoubleArray(DynamicObject object, int length, int usedLength, double[] array, int integrityLevel) {
        ZeroBasedDoubleArray arrayType = createZeroBasedDoubleArray().setIntegrityLevel(integrityLevel);
        arraySetLength(object, length);
        arraySetUsedLength(object, usedLength);
        arraySetArray(object, array);
        return arrayType;
    }

    public static ZeroBasedDoubleArray createZeroBasedDoubleArray() {
        return ZERO_BASED_DOUBLE_ARRAY;
    }

    private ZeroBasedDoubleArray(int integrityLevel, DynamicArrayCache cache) {
        super(integrityLevel, cache);
    }

    @Override
    public double getInBoundsFastDouble(DynamicObject object, int index, boolean condition) {
        return getArray(object, condition)[index];
    }

    @Override
    public void setInBoundsFast(DynamicObject object, int index, double value, boolean condition) {
        getArray(object, condition)[index] = value;
        if (JSConfig.TraceArrayWrites) {
            traceWriteValue("InBoundsFast", index, value);
        }
    }

    @Override
    public boolean isSupported(DynamicObject object, long index, boolean condition) {
        return isSupportedZeroBased(object, (int) index, condition);
    }

    @Override
    protected int prepareInBoundsFast(DynamicObject object, long index, boolean condition) {
        return (int) index;
    }

    @Override
    protected int prepareInBounds(DynamicObject object, int index, boolean condition, ProfileHolder profile) {
        prepareInBoundsZeroBased(object, index, condition, profile);
        return index;
    }

    @Override
    protected int prepareSupported(DynamicObject object, int index, boolean condition, ProfileHolder profile) {
        prepareSupportedZeroBased(object, index, condition, profile);
        return index;
    }

    @Override
    protected void setLengthLess(DynamicObject object, long length, boolean condition, ProfileHolder profile) {
        setLengthLessZeroBased(object, length, condition, profile);
    }

    @Override
    public Object[] toArray(DynamicObject object) {
        return toArrayZeroBased(object);
    }

    @Override
    public ZeroBasedObjectArray toObject(DynamicObject object, long index, Object value, boolean condition) {
        double[] array = getArray(object, condition);
        int length = lengthInt(object, condition);
        int usedLength = getUsedLength(object, condition);
        Object[] doubleCopy = ArrayCopy.doubleToObject(array, 0, usedLength);
        ZeroBasedObjectArray newArray = ZeroBasedObjectArray.makeZeroBasedObjectArray(object, length, usedLength, doubleCopy, integrityLevel);
        if (JSConfig.TraceArrayTransitions) {
            traceArrayTransition(this, newArray, index, value);
        }
        return newArray;
    }

    @Override
    public ContiguousDoubleArray toContiguous(DynamicObject object, long index, Object value, boolean condition) {
        double[] array = getArray(object, condition);
        int length = lengthInt(object, condition);
        int usedLength = getUsedLength(object, condition);
        ContiguousDoubleArray newArray = ContiguousDoubleArray.makeContiguousDoubleArray(object, length, array, 0, 0, usedLength, integrityLevel);
        if (JSConfig.TraceArrayTransitions) {
            traceArrayTransition(this, newArray, index, value);
        }
        return newArray;
    }

    @Override
    public HolesDoubleArray toHoles(DynamicObject object, long index, Object value, boolean condition) {
        double[] array = getArray(object, condition);
        int length = lengthInt(object, condition);
        int usedLength = getUsedLength(object, condition);
        HolesDoubleArray newArray = HolesDoubleArray.makeHolesDoubleArray(object, length, array, 0, 0, usedLength, 0, integrityLevel);
        if (JSConfig.TraceArrayTransitions) {
            traceArrayTransition(this, newArray, index, value);
        }
        return newArray;
    }

    @Override
    public long firstElementIndex(DynamicObject object, boolean condition) {
        return 0;
    }

    @Override
    public long lastElementIndex(DynamicObject object, boolean condition) {
        return getUsedLength(object, condition) - 1;
    }

    @Override
    public ScriptArray removeRangeImpl(DynamicObject object, long start, long end) {
        double[] array = getArray(object);
        int usedLength = getUsedLength(object);
        System.arraycopy(array, (int) end, array, (int) start, Math.max(0, (int) (usedLength - end)));
        return this;
    }

    @Override
    public ScriptArray addRangeImpl(DynamicObject object, long offset, int size) {
        return addRangeImplZeroBased(object, offset, size);
    }

    @Override
    public boolean hasHoles(DynamicObject object, boolean condition) {
        int length = lengthInt(object, condition);
        int usedLength = getUsedLength(object, condition);
        return usedLength < length;
    }

    @Override
    protected ZeroBasedDoubleArray withIntegrityLevel(int newIntegrityLevel) {
        return new ZeroBasedDoubleArray(newIntegrityLevel, cache);
    }

    @Override
    public long nextElementIndex(DynamicObject object, long index, boolean condition) {
        return nextElementIndexZeroBased(object, index, condition);
    }
}
