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
import java.util.List;
import java.util.Map;

import oafext.test.server.AppModuleFixture;
import oafext.test.server.BaseViewObjectMocker;
import oafext.test.server.RowMocker;
import oafext.test.util.MockHelper;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ViewRowImpl;

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
public class BaseRowResponder implements RowResponder<ViewRowImpl> {


    /** */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(BaseRowResponder.class);


    /** */
    public BaseRowResponder() {}

    /** {@inheritDoc} */
    @Override
    public void mockMethods(final AppModuleFixture<?> amFixture,
                            final RowMocker rowMocker,
                            final Class<? extends Row> pRowClass)
    {

        final Map<Class<? extends Row>, String> rowClsVoDefFullMap = amFixture
            .getRowClsVoDefMap();

        final String voDefFull = rowClsVoDefFullMap.get(pRowClass);
        assert voDefFull != null;

        final List<String> attrList = amFixture.getVoDefAttrListMap().get(
            voDefFull);


        /* remove(). */
        mockRemove(rowMocker).remove();

        /* getViewObj - anti zombie/anti final. */
        mockGetViewObj(rowMocker);

        /* getAttribute(int) */
        mockGetAttributeInt(attrList, rowMocker)
            .getAttribute(Matchers.anyInt());

        /* getAttribute(String) */
        mockGetAttributeString(rowMocker).getAttribute(Matchers.anyString());

        /* getKey() */
        mockGetKey(rowMocker).getKey();

        /* setAttribute(int) */
        mockSetAttributeInt(attrList, rowMocker).setAttribute(
            Matchers.anyInt(),
            Matchers.any());

        /* setAttribute(String) */
        mockSetAttributeString(rowMocker).setAttribute(
            Matchers.anyString(),
            Matchers.any());

        /* set*(Object) */
        mockSetter(pRowClass, attrList, rowMocker);

        /* get*() */
        mockGetter(attrList, rowMocker);
    }

    /** {@inheritDoc} */
    @Override
    public ViewRowImpl mockRemove(final RowMocker rowMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final BaseViewObjectMocker voMocker = rowMocker.getVoMocker();
                voMocker.remove(rowMocker);
                return null;
            }
        }).when(rowMocker.getMock());
    }

    @Override
    public ViewRowImpl mockGetAttributeInt(final List<String> attrList,
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
        }).when(rowMocker.getMock());

    }

    /** {@inheritDoc} */
    @Override
    public ViewRowImpl mockGetAttributeString(final RowMocker rowMocker)
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
    public void mockGetViewObj(final RowMocker rowMocker)
    {
        Method getViewObj = null; //NOPMD: null default, conditionally redefine.
        try {
            getViewObj = rowMocker
                .getMock()
                .getClass()
                .getDeclaredMethod(RowMocker.CUSTOM_GET_VO, new Class<?>[0]);

            if (getViewObj != null) {

                Mockito
                    .when(getViewObj.invoke(rowMocker.getMock(), new Object[0]))
                    .thenAnswer(new Answer<ViewObject>() {

                        @Override
                        public ViewObject answer(final InvocationOnMock invocation)
                                throws Throwable
                        {
                            final BaseViewObjectMocker voMocker = rowMocker
                                .getVoMocker();
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
    public ViewRowImpl mockGetKey(final RowMocker rowMocker)
    {
        return Mockito.doAnswer(new Answer<Key>() {

            @Override
            public Key answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Key key = Mockito.spy(new Key(new Object[] { rowMocker
                    .getMock()
                    .getAttribute(0) }));
                return key;
            }
        }).when(rowMocker.getMock());
    }

    @Override
    public ViewRowImpl mockSetAttributeInt(final List<String> attrList,
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
        }).when(rowMocker.getMock());
    }

    @Override
    public ViewRowImpl mockSetAttributeString(final RowMocker rowMocker)
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
        }).when(rowMocker.getMock());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void mockSetter(final Class<? extends Row> rowClass,
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
                Mockito.when(
                    method.invoke(
                        rowMocker.getMock(),
                        new Object[] { Matchers.any() })).thenAnswer(
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
            } catch (final IllegalArgumentException e) {
                BaseRowResponder.LOGGER.error(e.getMessage() + methodName, e);
            } catch (final IllegalAccessException e) {
                BaseRowResponder.LOGGER.error(e.getMessage(), e);
            } catch (final InvocationTargetException e) {
                BaseRowResponder.LOGGER.error(e.getMessage(), e);
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void mockGetter(final List<String> attrList,
                           final RowMocker rowMocker)
    {
        final MockHelper helper = new MockHelper(); //NOPMD: Optimized Outside loop

        for (final String nextAttr : attrList) {

            final String methodName = "get"
                    + nextAttr.substring(0, 1).toUpperCase()
                    + nextAttr.substring(1);

            final Method method = helper.findMethod(rowMocker
                .getMock()
                .getClass(), methodName);
            assert method != null;

            try {
                Mockito
                    .when(method.invoke(rowMocker.getMock(), new Object[0]))
                    .thenAnswer(new Answer<Object>() {

                        @Override
                        public Object answer(final InvocationOnMock invocation)
                                throws Throwable
                        {
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


}
