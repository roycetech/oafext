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
package oafext.test.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import oafext.test.util.MockHelper;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author royce
 * 
 */
public final class RowAnswers {


    /** */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(RowAnswers.class);


    /** */
    private RowAnswers() {

    }

    static <M> M mockGetAttributeInt(final M mockRow,
                                     final List<String> attrList,
                                     final RowMocker rowMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer index = (Integer) invocation.getArguments()[0];
                final String attrName = attrList.get(index);
                return rowMocker.getAttrValueMap().get(attrName);
            }
        }).when(mockRow);

    }

    static <M> M mockGetAttributeString(final M mockRow,
                                        final RowMocker rowMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String attrName = (String) invocation.getArguments()[0];
                return rowMocker.getAttrValueMap().get(attrName);
            }
        }).when(mockRow);

    }

    static void mockGetViewObj(final Row mockRow, final ViewObject mockVo)
    {
        Method getViewObj = null; //NOPMD: null default, conditionally redefine.
        try {
            getViewObj = mockRow.getClass().getDeclaredMethod(
                RowMocker.CUSTOM_GET_VO,
                new Class<?>[0]);

            if (getViewObj != null) {

                Mockito
                    .when(getViewObj.invoke(mockRow, new Object[0]))
                    .thenAnswer(new Answer<ViewObject>() {

                        @Override
                        public ViewObject answer(final InvocationOnMock invocation)
                                throws Throwable
                        {
                            return mockVo;
                        }
                    });


            }

        } catch (final SecurityException e1) {
            LOGGER.error(e1.getMessage(), e1);
        } catch (final NoSuchMethodException e1) {
            LOGGER.error("Anti zombie method not found for: "
                    + mockRow.getClass().getSimpleName(), e1);
        } catch (final IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (final InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    static <M extends Row> M mockGetKey(final M mockRow)
    {
        return Mockito.doAnswer(new Answer<Key>() {

            @Override
            public Key answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Key key = Mockito.spy(new Key(new Object[] { mockRow
                    .getAttribute(0) }));
                return key;
            }
        }).when(mockRow);
    }

    static <M> M mockSetAttributeInt(final M mockRow,
                                     final List<String> attrList,
                                     final RowMocker rowMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer index = (Integer) invocation.getArguments()[0];
                final Object value = invocation.getArguments()[1];
                final String attrName = attrList.get(index);
                final Map<String, Object> attrValueMap = rowMocker
                    .getAttrValueMap();
                attrValueMap.put(attrName, value);
                return null;

            }
        }).when(mockRow);
    }

    static <M> M mockSetAttributeString(final M mockRow,
                                        final RowMocker rowMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String attrName = (String) invocation.getArguments()[0];
                final Object value = invocation.getArguments()[1];
                final Map<String, Object> attrValueMap = rowMocker
                    .getAttrValueMap();
                attrValueMap.put(attrName, value);
                return null;

            }
        }).when(mockRow);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    static void mockSetter(final Row mockRow, final Class<?> rowClass,
                           final List<String> attrList,
                           final RowMocker rowMocker)
    {
        final MockHelper helper = new MockHelper(); //NOPMD: Optimized Outside loop
        for (final String nextAttr : attrList) {

            final String methodName = "set"
                    + nextAttr.substring(0, 1).toUpperCase()
                    + nextAttr.substring(1);


            final Method method = helper.findMethod(rowClass, methodName);

            try {
                Mockito
                    .when(
                        method.invoke(mockRow, new Object[] { Matchers.any() }))
                    .thenAnswer(new Answer<Object>() {

                        @Override
                        public Object answer(final InvocationOnMock invocation)
                                throws Throwable
                        {
                            final Object value = invocation.getArguments()[0];
                            rowMocker.getAttrValueMap().put(nextAttr, value);
                            return null;
                        }
                    });
            } catch (final IllegalArgumentException e) {
                LOGGER.error(e.getMessage() + methodName, e);
            } catch (final IllegalAccessException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (final InvocationTargetException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }


    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    static void mockGetter(final Row mockRow, final List<String> attrList,
                           final RowMocker rowMocker)
    {
        final MockHelper helper = new MockHelper(); //NOPMD: Optimized Outside loop

        for (final String nextAttr : attrList) {

            final String methodName = "get"
                    + nextAttr.substring(0, 1).toUpperCase()
                    + nextAttr.substring(1);

            final Method method = helper.findMethod(
                mockRow.getClass(),
                methodName);
            assert method != null;

            try {
                Mockito.when(method.invoke(mockRow, new Object[0])).thenAnswer(
                    new Answer<Object>() {

                        @Override
                        public Object answer(final InvocationOnMock invocation)
                                throws Throwable
                        {
                            final Integer index = attrList.indexOf(nextAttr);
                            return rowMocker.getAttrValueMap().get(index);
                        }
                    });
            } catch (final IllegalArgumentException e) {
                LOGGER.error(e.getMessage() + methodName, e);
            } catch (final IllegalAccessException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (final InvocationTargetException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }


}
