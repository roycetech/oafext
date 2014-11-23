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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import oafext.test.util.MockHelper;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;

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
            final List<String> attrList, final RowMocker rowMocker)
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
                RowMocker.METH_WRAPPED_GET_VO,
                new Class<?>[0]);
        } catch (final SecurityException e1) {
            LOGGER.error(e1.getMessage(), e1);
        } catch (final NoSuchMethodException e1) {
            LOGGER.error("Anti zombie method not found for: "
                    + mockRow.getClass().getSimpleName(), e1);
        }

        if (getViewObj != null) {

            try {
                Mockito.doAnswer(new Answer<ViewObject>() {

                    @Override
                    public ViewObject answer(final InvocationOnMock invocation)
                            throws Throwable
                    {
                        return mockVo;
                    }
                })
                    .when(mockRow)
                    .getClass()
                    .getDeclaredMethod(
                    RowMocker.METH_WRAPPED_GET_VO,
                    new Class<?>[0])
                    .invoke(mockRow, new Object[0]);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
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
            final List<String> attrList, final RowMocker rowMocker)
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

    static void mockSetterInt(final Row mockRow, final List<String> attrList,
            final RowMocker rowMocker)
    {
        final MockHelper helper = new MockHelper();
        for (final String nextAttr : attrList) {

            final String methodName = "set"
                    + nextAttr.substring(0, 1).toUpperCase()
                    + nextAttr.substring(1);

            Mockito.when(helper.invokeMethod(mockRow, methodName)).thenAnswer(
                new Answer<Object>() {

                    @Override
                    public Object answer(final InvocationOnMock invocation)
                            throws Throwable
                    {
                        final Object value = invocation.getArguments()[0];
                        rowMocker.getAttrValueMap().put(nextAttr, value);
                        return null;
                    }
                });
        }
    }

    static void mockSetterString(final Row mockRow, final List<String> attrList,
            final RowMocker rowMocker)
    {
        final MockHelper helper = new MockHelper();

        final Class[] classParam = new Class[] {};


        for (final String nextAttr : attrList) {

            final String setterName = "set"
                    + nextAttr.substring(0, 1).toUpperCase()
                    + nextAttr.substring(1);

            final String getterName = "get"
                    + nextAttr.substring(0, 1).toUpperCase()
                    + nextAttr.substring(1);

            final Class type = helper.findMethod(klass, methodName)


                    Mockito.when(helper.invokeMethod(mockRow, setterName, )).thenAnswer(
                        new Answer<Object>() {

                            @Override
                            public Object answer(final InvocationOnMock invocation)
                                    throws Throwable
                            {
                                final Object value = invocation.getArguments()[0];
                                rowMocker.getAttrValueMap().put(nextAttr, value);
                                return null;
                            }
                        });
        }
    }
}
