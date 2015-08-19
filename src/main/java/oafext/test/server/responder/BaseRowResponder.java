/**
 *   Copyright 2014 Royce Remulla
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package oafext.test.server.responder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import oafext.OafExtException;
import oafext.test.RowSetMocker;
import oafext.test.server.BaseViewObjectMocker;
import oafext.test.server.RowMocker;
import oafext.test.server.ViewObjectHGridMocker;
import oafext.util.ReflectUtil;
import oracle.jbo.Key;
import oracle.jbo.RowSet;
import oracle.jbo.ViewObject;
import oracle.jbo.domain.BlobDomain;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class was meant to represent the base functionality but HGrid type is
 * incorporated as well due to redundancy required when separating
 * implementation based inside inner class. Perhaps this can be enhance by
 * finding a way around the language limitation.
 *
 * @author royce
 * @param <R> specific row implementation type.
 * @param <V> View Object type.
 */
@SuppressWarnings({
        "PMD.TooManyMethods",
        "PMD.GodClass",
        "PMD.CyclomaticComplexity",
        "PMD.ModifiedCyclomaticComplexity",
        "PMD.StdCyclomaticComplexity" })
public class BaseRowResponder<R extends ViewRowImpl, V extends ViewObjectImpl>
        implements RowResponder<R, V> {


    /** */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(BaseRowResponder.class);


    /** {@inheritDoc} */
    @Override
    public void mockMethods(final List<String> attrList,
                            final RowMocker<R, V> rowMocker,
                            final Class<R> pRowClass)
    {
        mockRemove(rowMocker).remove();
        mockGetViewObj(rowMocker);
        mockGetAttributeCount(attrList, rowMocker);
        mockGetAttributeInt(attrList, rowMocker)
            .getAttribute(Matchers.anyInt());
        mockGetAttributeString(rowMocker).getAttribute(Matchers.anyString());
        mockGetKey(rowMocker).getKey();
        mockSetAttributeInt(attrList, rowMocker).setAttribute(
            Matchers.anyInt(),
            Matchers.any());
        mockSetAttributeString(rowMocker).setAttribute(
            Matchers.anyString(),
            Matchers.any());
        /* set*(Object) */
        mockSetter(pRowClass, attrList, rowMocker);
        /* get*() */
        mockGetter(attrList, rowMocker);
        mockGetAttributeNames(attrList, rowMocker).getAttributeNames();
        mockIsDead(rowMocker).isDead();

        mockToString(attrList, rowMocker).toString();
    }

    /** {@inheritDoc} */
    @Override
    public R mockRemove(final RowMocker<R, V> rowMocker)
    {
        return Mockito.doAnswer(
            invocation -> {
                final BaseViewObjectMocker<V, R> voMocker =
                        rowMocker.getVoMocker();
                voMocker.remove(rowMocker);
                return null;
            }).when(rowMocker.getMock());
    }


    /** {@inheritDoc} */
    @Override
    public void mockGetAttributeCount(final List<String> attrList,
                                      final RowMocker<R, V> rowMocker)
    {
        Method getAttrCountMeth = null; //NOPMD: null default, conditionally redefine.
        try {
            getAttrCountMeth =
                    rowMocker
                        .getMock()
                        .getClass()
                        .getDeclaredMethod(
                            RowMocker.CUST_GET_ATTR_CNT,
                            new Class<?>[0]);

            if (getAttrCountMeth != null) {

                Mockito
                    .when(
                        getAttrCountMeth.invoke(
                            rowMocker.getMock(),
                            new Object[0])).thenAnswer(
                        invocation -> attrList.size());
            }

        } catch (final SecurityException e1) {
            BaseRowResponder.LOGGER.error(e1.getMessage(), e1);
        } catch (final NoSuchMethodException e1) {
            BaseRowResponder.LOGGER.error("Anti zombie method not found for: "
                    + rowMocker.getMock().getClass().getSimpleName(), e1);
        } catch (final IllegalArgumentException e) {
            BaseRowResponder.LOGGER.error(e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            BaseRowResponder.LOGGER.error(e.getMessage(), e);
        } catch (final InvocationTargetException e) {
            BaseRowResponder.LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public R mockGetAttributeInt(final List<String> attrList,
                                 final RowMocker<R, V> rowMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer index = (Integer) invocation.getArguments()[0];
                final String attrName = attrList.get(index);

                if (isRowSetAttribute(rowMocker, index.intValue())
                        && rowMocker.getAttrValueMap().get(attrName) == null) {
                    final RowSetMocker<V, R> rowSetMocker =
                            RowSetMocker.<V, R> newInstance(rowMocker);
                    rowMocker.getAttrValueMap().put(
                        attrName,
                        rowSetMocker.getMock());
                }
                return rowMocker.getAttrValueMap().get(attrName);
            }
        }).when(rowMocker.getMock());

    }

    /** {@inheritDoc} */
    @Override
    public R mockGetAttributeString(final RowMocker<R, V> rowMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String attrName = (String) invocation.getArguments()[0];
                return rowMocker.getAttrValueMap().get(attrName);
            }
        }).when(rowMocker.getMock());

    }

    /** {@inheritDoc} */
    @Override
    public void mockGetViewObj(final RowMocker<R, V> rowMocker)
    {
        Method getViewObj = null; //NOPMD: null default, conditionally redefine.
        try {
            getViewObj =
                    rowMocker
                        .getMock()
                        .getClass()
                        .getDeclaredMethod(
                            RowMocker.CUSTOM_GET_VO,
                            new Class<?>[0]);

            if (getViewObj != null) {

                Mockito
                    .when(getViewObj.invoke(rowMocker.getMock(), new Object[0]))
                    .thenAnswer(new Answer<ViewObject>() {

                        @Override
                        public ViewObject answer(final InvocationOnMock invocation)
                                throws Throwable
                        {
                            final BaseViewObjectMocker<V, R> voMocker =
                                    rowMocker.getVoMocker();
                            return voMocker.getMock();
                        }
                    });


            }

        } catch (final SecurityException e1) {
            BaseRowResponder.LOGGER.error(e1.getMessage(), e1);
        } catch (final NoSuchMethodException e1) {
            BaseRowResponder.LOGGER.error("Anti zombie method not found for: "
                    + rowMocker.getMock().getClass().getSimpleName(), e1);
        } catch (final IllegalArgumentException e) {
            BaseRowResponder.LOGGER.error(e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            BaseRowResponder.LOGGER.error(e.getMessage(), e);
        } catch (final InvocationTargetException e) {
            BaseRowResponder.LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public R mockGetKey(final RowMocker<R, V> rowMocker)
    {
        return Mockito.doAnswer(new Answer<Key>() {

            @Override
            public Key answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Key key =
                        Mockito.spy(new Key(new Object[] { rowMocker
                            .getMock()
                            .getAttribute(0) }));
                return key;
            }
        }).when(rowMocker.getMock());
    }

    @Override
    public R mockSetAttributeInt(final List<String> attrList,
                                 final RowMocker<R, V> rowMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer index = (Integer) invocation.getArguments()[0];
                final Object value = invocation.getArguments()[1];
                final String attrName = attrList.get(index);
                final Map<String, Object> attrValueMap =
                        rowMocker.getAttrValueMap();
                attrValueMap.put(attrName, value);
                return null;

            }
        }).when(rowMocker.getMock());
    }

    @Override
    public R mockSetAttributeString(final RowMocker<R, V> rowMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String attrName = (String) invocation.getArguments()[0];
                final Object value = invocation.getArguments()[1];
                final Map<String, Object> attrValueMap =
                        rowMocker.getAttrValueMap();
                attrValueMap.put(attrName, value);
                return null;

            }
        }).when(rowMocker.getMock());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void mockSetter(final Class<R> rowClass,
                           final List<String> attrList,
                           final RowMocker<R, V> rowMocker)
    {
        for (final String nextAttr : attrList) {

            final String methodName =
                    "set" + nextAttr.substring(0, 1).toUpperCase()
                            + nextAttr.substring(1);


            final Method method = ReflectUtil.findMethod(rowClass, methodName);

            if (method != null) {
                try {

                    Mockito.when(
                        method.invoke(
                            rowMocker.getMock(),
                            new Object[] { Matchers.any() })).thenAnswer(
                        new Answer<Object>() {

                            @Override
                            public Object answer(final InvocationOnMock invocation)
                                    throws Throwable
                            {
                                final Object value =
                                        invocation.getArguments()[0];
                                rowMocker
                                    .getAttrValueMap()
                                    .put(nextAttr, value);
                                return null;
                            }
                        });

                } catch (final IllegalArgumentException e) {
                    BaseRowResponder.LOGGER.error(
                        e.getMessage() + methodName,
                        e);
                } catch (final IllegalAccessException e) {
                    BaseRowResponder.LOGGER.error(e.getMessage(), e);
                } catch (final InvocationTargetException e) {
                    BaseRowResponder.LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void mockGetter(final List<String> attrList,
                           final RowMocker<R, V> rowMocker)
    {
        for (final String nextAttr : attrList) {

            final String methodName =
                    "get" + nextAttr.substring(0, 1).toUpperCase()
                            + nextAttr.substring(1);

            final Method method =
                    ReflectUtil.findMethod(
                        rowMocker.getMock().getClass(),
                        methodName);

            assert method != null : "Method not found for attribute:"
                    + nextAttr;

            try {

                Mockito
                    .when(method.invoke(rowMocker.getMock(), new Object[0]))
                    .thenAnswer(new Answer<Object>() {

                        @Override
                        public Object answer(final InvocationOnMock invocation)
                                throws Throwable
                        {

                            if (isRowSetAttribute(
                                rowMocker,
                                attrList.indexOf(nextAttr))
                                    && rowMocker
                                        .getAttrValueMap()
                                        .get(nextAttr) == null) {

                                final RowSetMocker<V, R> rowSetMocker =
                                        RowSetMocker
                                            .<V, R> newInstance(rowMocker);

                                rowMocker.getAttrValueMap().put(
                                    nextAttr,
                                    rowSetMocker.getMock());
                            }

                            return rowMocker.getAttrValueMap().get(nextAttr);
                        }
                    });

            } catch (final IllegalArgumentException e) {
                BaseRowResponder.LOGGER.error(e.getMessage() + methodName, e);
            } catch (final IllegalAccessException e) {
                BaseRowResponder.LOGGER.error(e.getMessage(), e);
            } catch (final InvocationTargetException e) {
                BaseRowResponder.LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Currently for HGrid type only.
     *
     * @param rowMocker
     * @param attrIndex
     * @return
     */
    @SuppressWarnings("PMD.OnlyOneReturn" /* Two only. */)
    boolean isRowSetAttribute(final RowMocker<R, V> rowMocker, final int attrIdx)
    {
        final BaseViewObjectMocker<V, R> voMocker = rowMocker.getVoMocker();

        final boolean isRowSet = false; //NOPMD: null default, conditionally redefine.
        if (voMocker.isHGrid()) {
            final ViewObjectHGridMocker<V, R> voHGridMocker =
                    (ViewObjectHGridMocker<V, R>) voMocker;
            return voHGridMocker.isChildAttribute(attrIdx);
        }
        return isRowSet;
    }

    @Override
    public R mockGetAttributeNames(final List<String> attrList,
                                   final RowMocker<R, V> rowMocker)
    {
        return Mockito.doAnswer(new Answer<String[]>() {

            @Override
            public String[] answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                rowMocker.checkPulse();
                return attrList.toArray(new String[attrList.size()]);
            }
        }).when(rowMocker.getMock());
    }

    @Override
    public R mockIsDead(final RowMocker<R, V> rowMocker)
    {
        return Mockito.doAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return rowMocker.isRemoved();
            }
        }).when(rowMocker.getMock());
    }

    @Override
    public R mockToString(final List<String> attrList,
                          final RowMocker<R, V> rowMocker)
    {
        return Mockito.doAnswer(new Answer<String>() {

            boolean hasValue(final String string)
            {
                return string != null && !"".equals(string.trim());
            }

            boolean hasValue(final Object object)
            {
                return object != null && hasValue(object.toString());
            }

            @Override
            public String answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final StringBuilder retval = new StringBuilder();
                retval.append("Row Mock for OafExt, hashCode: "
                        + rowMocker.getMock().hashCode() + "\n");

                String methodName;
                final Object[] emptyObj = {}; //NOPMD: Optimized outside loop.
                final List<String> param = new ArrayList<String>();
                for (final Method method : rowMocker.getRowClass().getMethods()) {
                    methodName = method.getName();

                    final String propName = methodName.substring(3);
                    if (attrList.contains(propName)
                            && methodName.startsWith("get")) {

                        Object object;
                        try {
                            object =
                                    method.invoke(rowMocker.getMock(), emptyObj);
                            if (object instanceof BlobDomain) {
                                final StringBuilder paramValPair =
                                        new StringBuilder(); //NOPMD: necessary
                                paramValPair
                                    .append(propName)
                                    .append('=')
                                    .append("$BLOB");
                                param.add(paramValPair.toString());
                            } else {
                                if (hasValue(object)) {
                                    final StringBuilder paramValPair =
                                            new StringBuilder(); //NOPMD: necessary
                                    paramValPair.append(propName).append('=');
                                    if (object instanceof RowSet) {
                                        paramValPair.append("$RowSet");
                                    } else {
                                        paramValPair
                                        //                                            .append('(')
                                        //                                            .append(
                                        //                                                object
                                        //                                                    .getClass()
                                        //                                                    .getSimpleName())
                                        //                                            .append(')')
                                            .append(object);
                                    }
                                    param.add(paramValPair.toString());
                                }
                            }

                        } catch (final IllegalArgumentException e) {
                            throw new OafExtException(e);
                        } catch (final IllegalAccessException e) {
                            throw new OafExtException(e);
                        } catch (final InvocationTargetException e) {
                            throw new OafExtException(e);
                        }
                    }
                }
                Collections.sort(param);
                for (final String string : param) {
                    retval.append(string);
                    retval.append('\n');
                }
                return retval.toString();
            }
        })
            .when(rowMocker.getMock());

    }


}
