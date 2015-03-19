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

import oafext.Constant;
import oafext.test.server.AppModuleFixture;
import oafext.test.server.AppModuleMocker;
import oafext.test.server.AttrDefMocker;
import oafext.test.server.BaseViewObjectMocker;
import oafext.test.server.RowMocker;
import oafext.test.server.RowSetIteratorMocker;
import oafext.test.server.ViewObjectMockState;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.jbo.AttributeDef;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convention: Mock View object is always the first parameter if present.
 *
 * @author royce
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class BaseViewObjectResponder implements
        ViewObjectResponder<ViewObjectImpl> {


    /** sl4j logger instance. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(BaseViewObjectResponder.class);

    /** */
    BaseViewObjectResponder() {}


    @Override
    public void mockMethods(final AppModuleFixture<?> amFixture,
                            final BaseViewObjectMocker voMocker)
    {
        final String voName = voMocker.getMockedVoState().getViewObjectName();

        final OAApplicationModuleImpl appModule = amFixture.getMockAppModule();

        final Class<? extends Row> rowClass = amFixture
            .getVoNameRowClsMap()
            .get(voName);

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


        /* setRangeSize(int) */
        mockSetRangeSize(voMocker).setRangeSize(Matchers.anyInt());

        /* createRow() */
        mockCreateRow(amFixture, voMocker).createRow();

        /* insertRow(Row) */
        mockInsertRow(voMocker).insertRow((Row) Matchers.any());

        /* insertRowAtRangeIndex(int, Row) */
        mockInsertRowAtRangeIndex(voMocker).insertRowAtRangeIndex(
            Matchers.anyInt(),
            (Row) Matchers.any());

        /* getCurrentRow() */
        mockGetCurrentRow(voMocker, voMocker.getMockedVoState().getCurrentRow())
            .getCurrentRow();

        /* setCurrentRow() */
        mockSetCurrentRow(voMocker).setCurrentRow((Row) Matchers.any());

        /* first() */
        mockFirst(voMocker).first();

        /* getRowCount() */
        mockGetRowCount(voMocker).getRowCount();

        /* createRowSetIterator() */
        mockCreateRowSetIterator(voMocker).createRowSetIterator(
            Matchers.anyString());

        //getRowAtRangeIndex(int).
        mockGetRowAtRangeIndex(voMocker).getRowAtRangeIndex(Matchers.anyInt());

        //getAllRowsInRange().
        mockGetAllRowsInRange(voMocker).getAllRowsInRange();

        //executeQuery().
        mockExecuteQuery(voMocker).executeQuery();

        //isExecuted().
        mockIsExecuted(voMocker).isExecuted();

        //getAttributeDef().
        mockGetAttributeDef(amFixture, voMocker).getAttributeDef(
            Matchers.anyInt());

        //getAttributeCount().
        mockGetAttributeCount(amFixture, voMocker).getAttributeCount();

        //getAttributeIndexOf().
        mockGetAttributeIndexOf(amFixture, voMocker).getAttributeIndexOf(
            Matchers.anyString());

    }


    /** {@inheritDoc} */
    @Override
    public ViewObjectImpl mockCreateRow(final AppModuleFixture<?> amFixture,
                                        final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final ViewObjectImpl mockVo = voMocker.getMock();

                @SuppressWarnings(Constant.UNCHECKED)
                final Class<? extends ViewRowImpl> rowClass = mockVo
                    .getRowClass();

                final RowMocker rowMocker = new RowMocker(
                    rowClass,
                    amFixture,
                    voMocker);

                voMocker.getNewRowsMap().put(rowMocker.getMock(), rowMocker);

                return rowMocker.getMock();
            }
        }).when(voMocker.getMock());
    }

    /**
     * @paraViewObjectImpl mockVo
     * @paraViewObjectImpl BaseViewObjectMocker
     * @return
     */
    @Override
    public ViewObjectImpl mockCreateRowSetIterator(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<RowSetIterator>() {

            @Override
            public RowSetIterator answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String iterName = invocation.getArguments()[0].toString();

                assert iterName != null;
                assert voMocker.getRowSetIterMap().get(iterName) == null;

                final RowSetIteratorMocker rsIterMocker = new RowSetIteratorMocker(
                    iterName,
                    voMocker);
                voMocker.getRowSetIterMap().put(iterName, rsIterMocker);

                return rsIterMocker.getMock();
            }
        })
            .when(voMocker.getMock());
    }

    /**
     * @paraViewObjectImpl mockVo
     * @paraViewObjectImpl BaseViewObjectMocker
     */
    @Override
    public ViewObjectImpl mockExecuteQuery(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final ViewObjectMockState voState = voMocker.getMockedVoState();
                voState.setExecuted(true);
                return null;
            }
        })
            .when(voMocker.getMock());
    }

    /**
     * @paraViewObjectImpl mockVo
     */
    @Override
    public ViewObjectImpl mockFirst(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<RowMocker> rowMockerList = voMocker
                    .getRowMockerList();

                if (rowMockerList.isEmpty()) {
                    return null;
                } else {
                    final Row firstRow = rowMockerList.get(0).getMock();
                    final ViewObjectMockState voState = voMocker
                        .getMockedVoState();
                    voState.setRowPointer(firstRow);
                    return firstRow;
                }
            }
        }).when(voMocker.getMock());
    }


    @Override
    public ViewObjectImpl mockGetAttributeCount(final AppModuleFixture<?> amFixture,
                                                final BaseViewObjectMocker voMocker)
    {

        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<AttrDefMocker> attrDefMockerList = initAttributeList(
                    voMocker.getMock().getName(),
                    amFixture);

                return attrDefMockerList.size();
            }
        })
            .when(voMocker.getMock());
    }

    /**
     * @paraViewObjectImpl mockVo
     * @paraViewObjectImpl BaseViewObjectMocker
     */
    @Override
    public ViewObjectImpl mockGetAttributeDef(final AppModuleFixture<?> amFixture,
                                              final BaseViewObjectMocker voMocker)
    {

        return Mockito.doAnswer(new Answer<AttributeDef>() {

            @Override
            public AttributeDef answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<AttrDefMocker> attrDefMockerList = initAttributeList(
                    voMocker.getMock().getName(),
                    amFixture);

                final Integer index = (Integer) invocation.getArguments()[0];
                assert index > -1;
                assert index < attrDefMockerList.size();
                return attrDefMockerList.get(index).getMockAttrDef();
            }
        })
            .when(voMocker.getMock());
    }

    @Override
    public ViewObjectImpl mockGetAttributeIndexOf(final AppModuleFixture<?> amFixture,
                                                  final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String attrName = (String) invocation.getArguments()[0];
                final String voDefFull = amFixture.getVoNameDefMap().get(
                    voMocker.getMock().getName());
                assert voDefFull != null;

                return amFixture
                    .getVoDefAttrListMap()
                    .get(voDefFull)
                    .indexOf(attrName);
            }
        }).when(voMocker.getMock());
    }


    @Override
    public ViewObjectImpl mockGetCurrentRow(final BaseViewObjectMocker voMocker,
                                            final Row currentRow)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return currentRow;
            }
        }).when(voMocker.getMock());
    }


    /** {@inheritDoc} */
    @Override
    public ViewObjectImpl mockIsExecuted(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final ViewObjectMockState voState = voMocker.getMockedVoState();
                return voState.isExecuted();
            }
        })
            .when(voMocker.getMock());
    }


    /**
     * @paraViewObjectImpl mockVo
     */
    @Override
    public ViewObjectImpl mockSetCurrentRow(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final ViewObjectMockState voState = voMocker.getMockedVoState();
                voState.setCurrentRow((Row) invocation.getArguments()[0]);
                return null;
            }
        })
            .when(voMocker.getMock());
    }


    /*
     * Consider putting insert locking after initialization. HGrid is not
     * allowed to insert do to limitation of modifying HGrid hierarchy in real
     * world.
     */
    @Override
    public ViewObjectImpl mockInsertRow(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Row mockRow = (Row) invocation.getArguments()[0];
                final RowMocker rowMocker = voMocker.getNewRowsMap().remove(
                    mockRow);

                voMocker.getRowMockerList().add(rowMocker);
                return null;
            }
        }).when(voMocker.getMock());
    }


    /**
     * Consider putting insert locking after initialization. HGrid is not
     * allowed to insert do to limitation of modifying HGrid hierarchy in real
     * world.
     */
    @Override
    public ViewObjectImpl mockInsertRowAtRangeIndex(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer index = (Integer) invocation.getArguments()[0];
                assert index <= voMocker.getRowMockerList().size();
                final Row mockRow = (Row) invocation.getArguments()[1];

                final RowMocker rowMocker = voMocker.getNewRowsMap().remove(
                    mockRow);

                final List<RowMocker> rowMockerList = voMocker
                    .getRowMockerList();
                rowMockerList.add(index, rowMocker);
                return null;
            }
        }).when(voMocker.getMock());
    }

    @Override
    public ViewObjectImpl mockSetRangeSize(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer size = (Integer) invocation.getArguments()[0];
                final ViewObjectMockState voState = voMocker.getMockedVoState();
                voState.setRangeSize(size);
                return null;
            }
        })
            .when(voMocker.getMock());
    }


    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops" /* FP: */)
    private List<AttrDefMocker> initAttributeList(final String voInstName,
                                                  final AppModuleFixture<?> amFixture)
    {
        final String voDefFull = amFixture.getVoNameDefMap().get(voInstName);
        assert voDefFull != null;

        final AppModuleMocker<?> appModuleMocker = amFixture
            .getAppModuleMocker();
        final List<AttrDefMocker> attrDefMockerList = appModuleMocker
            .getAttrDefMockerMap()
            .get(voDefFull);
        assert attrDefMockerList != null;

        if (attrDefMockerList.isEmpty()) {
            final List<String> attrList = amFixture.getVoDefAttrListMap().get(
                voDefFull);
            for (final String nextAttribute : attrList) {
                attrDefMockerList.add(new AttrDefMocker(nextAttribute));
            }
        }
        return attrDefMockerList;

    }

}
