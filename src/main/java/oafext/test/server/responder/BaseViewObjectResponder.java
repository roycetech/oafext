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

import java.util.List;

import oafext.test.RowSetMocker;
import oafext.test.ViewCriteriaMocker;
import oafext.test.server.AppModuleFixture;
import oafext.test.server.AppModuleMocker;
import oafext.test.server.AttrDefMocker;
import oafext.test.server.BaseViewObjectMocker;
import oafext.test.server.RowMocker;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.jbo.AttributeDef;
import oracle.jbo.Row;
import oracle.jbo.RowSet;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Convention: Mock View object is always the first parameter if present.
 *
 * @author royce
 *
 * @param <R> Row type.
 * @param <V> View Object type.
 */
@SuppressWarnings({ "PMD.TooManyMethods" })
public abstract class BaseViewObjectResponder<V extends ViewObjectImpl, R extends ViewRowImpl>
        extends BaseRowSetResponder<V, R> implements ViewObjectResponder<V, R> {


    @Override
    public void mockMethods(final AppModuleFixture<?> amFixture,
                            final BaseViewObjectMocker<V, R> voMocker)
    {
        super.mockMethods(amFixture, voMocker);

        final String voName = voMocker.getMockedVoState().getViewObjectName();

        final OAApplicationModuleImpl appModule = amFixture.getMockAppModule();

        final Class<? extends Row> rowClass =
                amFixture.getVoNameRowClsMap().get(voName);

        final ViewObjectImpl mockVo = voMocker.getMock();

        /* getName() */
        Mockito.doReturn(voName).when(mockVo).getName();

        /* getApplicationModule() */
        Mockito.doReturn(appModule).when(mockVo).getApplicationModule();

        /* getRowClass() */
        Mockito.doReturn(rowClass).when(mockVo).getRowClass();

        /* getFullName() */
        Mockito
            .when(mockVo.getFullName())
            .thenReturn("Mock Full Name" + voName);

        /* getViewObject() */
        Mockito.when(mockVo.getViewObject()).thenReturn(mockVo);

        /* getRowSet() */
        Mockito.when(mockVo.getRowSet()).thenReturn(mockVo);

        //        ViewObjectImpl voImpl;
        //        RowSet rowSet;


        //        /* getRowCount() */
        //        mockGetRowCount(voMocker).getRowCount();
        //
        //        //getRowAtRangeIndex(int).
        //        mockGetRowAtRangeIndex(voMocker).getRowAtRangeIndex(Matchers.anyInt());
        //
        //        //getAllRowsInRange().
        //        mockGetAllRowsInRange(voMocker).getAllRowsInRange();

        //getAttributeDef().
        mockGetAttributeDef(amFixture, voMocker).getAttributeDef(
            Matchers.anyInt());

        //getAttributeCount().
        mockGetAttributeCount(amFixture, voMocker).getAttributeCount();

        //getAttributeIndexOf().
        mockGetAttributeIndexOf(amFixture, voMocker).getAttributeIndexOf(
            Matchers.anyString());

        //toString().
        mockToString(amFixture, voMocker).toString();


    }

    /**
     * @paraViewObjectImpl mockVo
     * @paraViewObjectImpl BaseViewObjectMocker
     * @return
     */
    @Override
    public V mockCreateViewCriteria(final BaseViewObjectMocker<V, R> voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                new ViewCriteriaMocker(voMocker);
                return null;
            }
        }).when(voMocker.getMock());
    }


    @Override
    public V mockGetAttributeCount(final AppModuleFixture<?> amFixture,
                                   final BaseViewObjectMocker<V, R> voMocker)
    {
        //        ViewObjectImpl voImpl;
        //        RowSet rowSet;
        //        voImpl.getAttributeCount();

        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<AttrDefMocker> attrDefMockerList =
                        initAttributeList(
                            voMocker.getMock().getName(),
                            amFixture);

                return attrDefMockerList.size();
            }
        }).when(voMocker.getMock());
    }

    /**
     * @paraViewObjectImpl mockVo
     * @paraViewObjectImpl BaseViewObjectMocker
     */
    @Override
    public V mockGetAttributeDef(final AppModuleFixture<?> amFixture,
                                 final BaseViewObjectMocker<V, R> voMocker)
    {

        return Mockito.doAnswer(new Answer<AttributeDef>() {

            @Override
            public AttributeDef answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<AttrDefMocker> attrDefMockerList =
                        initAttributeList(
                            voMocker.getMock().getName(),
                            amFixture);

                final Integer index = (Integer) invocation.getArguments()[0];
                assert index > -1;
                assert index < attrDefMockerList.size();
                return attrDefMockerList.get(index).getMockAttrDef();
            }
        }).when(voMocker.getMock());
    }

    @Override
    public V mockGetAttributeIndexOf(final AppModuleFixture<?> amFixture,
                                     final BaseViewObjectMocker<V, R> voMocker)
    {
        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String attrName = (String) invocation.getArguments()[0];
                final String voDefFull =
                        amFixture.getVoNameDefMap().get(
                            voMocker.getMock().getName());
                assert voDefFull != null;

                return amFixture
                    .getVoDefAttrListMap()
                    .get(voDefFull)
                    .indexOf(attrName);
            }
        }).when(voMocker.getMock());
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops" /* FP: */)
    private List<AttrDefMocker> initAttributeList(final String voInstName,
                                                  final AppModuleFixture<?> amFixture)
    {
        final String voDefFull = amFixture.getVoNameDefMap().get(voInstName);
        assert voDefFull != null;

        final AppModuleMocker<?> appModuleMocker =
                amFixture.getAppModuleMocker();
        final List<AttrDefMocker> attrDefMockerList =
                appModuleMocker.getAttrDefMockerMap().get(voDefFull);
        assert attrDefMockerList != null;

        if (attrDefMockerList.isEmpty()) {
            final List<String> attrList =
                    amFixture.getVoDefAttrListMap().get(voDefFull);
            for (final String nextAttribute : attrList) {
                attrDefMockerList.add(new AttrDefMocker(nextAttribute));
            }
        }
        return attrDefMockerList;

    }


    /** {@inheritDoc} */
    @Override
    public RowSet mockToString(final AppModuleFixture<?> amFixture,
                               final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<String>() {

            @Override
            public String answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final BaseViewObjectMocker<V, R> voMocker =
                        (BaseViewObjectMocker<V, R>) rowSetMocker;

                final StringBuilder strBuilder = new StringBuilder();
                strBuilder
                    .append("View Object Mock for OafExt, hashCode: ")
                    .append(voMocker.getMock().hashCode())
                    .append('\n')
                    .append("Type: ")
                    .append(voMocker.getViewObjectType())
                    .append('\n');

                for (final RowMocker<R, V> rowMocker : voMocker
                    .getRowMockerList()) {

                    strBuilder.append(rowMocker.getMock());
                }


                return strBuilder.toString();
            }
        }).when(rowSetMocker.getMock());
    }

}
