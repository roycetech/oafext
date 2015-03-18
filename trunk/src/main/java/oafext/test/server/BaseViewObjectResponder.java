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

import java.util.List;

import oafext.Constant;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.jbo.AttributeDef;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.server.ViewObjectImpl;

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
    public void mockMethods(final ViewObjectImpl mockVo,
                            final AppModuleFixture<?> amFixture,
                            final BaseViewObjectMocker voMocker)
    {
        final String voName = voMocker.getMockedVoState().getViewObjectName();

        final OAApplicationModuleImpl appModule = amFixture.getMockAppModule();

        final Class<? extends Row> rowClass = amFixture
            .getVoNameRowClsMap()
            .get(voName);

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
        mockSetRangeSize(mockVo, voMocker).setRangeSize(Matchers.anyInt());

        /* createRow() */
        mockCreateRow(mockVo, amFixture, voMocker).createRow();

        /* insertRow(Row) */
        mockInsertRow(mockVo, voMocker).insertRow((Row) Matchers.any());

        /* insertRowAtRangeIndex(int, Row) */
        mockInsertRowAtRangeIndex(mockVo, voMocker).insertRowAtRangeIndex(
            Matchers.anyInt(),
            (Row) Matchers.any());

        /* getCurrentRow() */
        mockGetCurrentRow(mockVo, voMocker.getMockedVoState().getCurrentRow())
            .getCurrentRow();

        /* setCurrentRow() */
        mockSetCurrentRow(mockVo, voMocker).setCurrentRow((Row) Matchers.any());

        /* first() */
        mockFirst(mockVo, voMocker).first();

        /* getRowCount() */
        mockGetRowCount(mockVo, voMocker).getRowCount();

        /* createRowSetIterator() */
        mockCreateRowSetIterator(mockVo, voMocker).createRowSetIterator(
            Matchers.anyString());

        //getRowAtRangeIndex(int).
        mockGetRowAtRangeIndex(mockVo, voMocker).getRowAtRangeIndex(
            Matchers.anyInt());

        //getAllRowsInRange().
        mockGetAllRowsInRange(mockVo, voMocker).getAllRowsInRange();

        //executeQuery().
        mockExecuteQuery(mockVo, voMocker).executeQuery();

        //isExecuted().
        mockIsExecuted(mockVo, voMocker).isExecuted();

        //getAttributeDef().
        mockGetAttributeDef(mockVo, amFixture).getAttributeDef(
            Matchers.anyInt());

        //getAttributeCount().
        mockGetAttributeCount(mockVo, amFixture).getAttributeCount();

        //getAttributeIndexOf().
        mockGetAttributeIndexOf(mockVo, amFixture).getAttributeIndexOf(
            Matchers.anyString());

    }


    /** {@inheritDoc} */
    @Override
    public ViewObjectImpl mockCreateRow(final ViewObjectImpl mockVo,
                                        final AppModuleFixture<?> amFixture,
                                        final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                @SuppressWarnings(Constant.UNCHECKED)
                final Class<? extends Row> rowClass = mockVo.getRowClass();


                //                final long baseline = System.currentTimeMillis();

                final RowMocker rowMocker = new RowMocker(
                    mockVo,
                    rowClass,
                    amFixture,
                    voMocker);

                //                LOGGER.info(" new rowMocker elapsed: "
                //                        + (System.currentTimeMillis() - baseline));

                voMocker.getNewRowsMap().put(rowMocker.getMockRow(), rowMocker);

                return rowMocker.getMockRow();
            }
        })
            .when(mockVo);
    }

    /**
     * @paraViewObjectImpl mockVo
     * @paraViewObjectImpl BaseViewObjectMocker
     * @return
     */
    @Override
    public ViewObjectImpl mockCreateRowSetIterator(final ViewObjectImpl mockVo,
                                                   final BaseViewObjectMocker voMocker)
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

                return rsIterMocker.getMockRsIter();
            }
        })
            .when(mockVo);
    }


    /**
     * @paraViewObjectImpl mockVo
     * @paraViewObjectImpl BaseViewObjectMocker
     */
    @Override
    public ViewObjectImpl mockExecuteQuery(final ViewObjectImpl mockVo,
                                           final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final ViewObjectMockedState voState = voMocker
                    .getMockedVoState();
                voState.setExecuted(true);
                return null;
            }
        }).when(mockVo);
    }

    /**
     * @paraViewObjectImpl mockVo
     */
    @Override
    public ViewObjectImpl mockFirst(final ViewObjectImpl mockVo,
                                    final BaseViewObjectMocker voMocker)
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
                    final Row firstRow = rowMockerList.get(0).getMockRow();
                    final ViewObjectMockedState voState = voMocker
                        .getMockedVoState();
                    voState.setRowPointer(firstRow);
                    return firstRow;
                }
            }
        }).when(mockVo);
    }


    @Override
    public ViewObjectImpl mockGetAttributeCount(final ViewObjectImpl mockVo,
                                                final AppModuleFixture<?> amFixture)
    {

        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<AttrDefMocker> attrDefMockerList = initAttributeList(
                    mockVo.getName(),
                    amFixture);

                return attrDefMockerList.size();
            }
        })
            .when(mockVo);
    }

    /**
     * @paraViewObjectImpl mockVo
     * @paraViewObjectImpl BaseViewObjectMocker
     */
    @Override
    public ViewObjectImpl mockGetAttributeDef(final ViewObjectImpl mockVo,
                                              final AppModuleFixture<?> amFixture)
    {

        return Mockito.doAnswer(new Answer<AttributeDef>() {

            @Override
            public AttributeDef answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<AttrDefMocker> attrDefMockerList = initAttributeList(
                    mockVo.getName(),
                    amFixture);

                final Integer index = (Integer) invocation.getArguments()[0];
                assert index > -1;
                assert index < attrDefMockerList.size();
                return attrDefMockerList.get(index).getMockAttrDef();
            }
        })
            .when(mockVo);
    }

    @Override
    public ViewObjectImpl mockGetAttributeIndexOf(final ViewObjectImpl mockVo,
                                                  final AppModuleFixture<?> amFixture)
    {
        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String attrName = (String) invocation.getArguments()[0];
                final String voDefFull = amFixture.getVoNameDefMap().get(
                    mockVo.getName());
                assert voDefFull != null;

                return amFixture
                    .getVoDefAttrListMap()
                    .get(voDefFull)
                    .indexOf(attrName);
            }
        }).when(mockVo);
    }


    @Override
    public ViewObjectImpl mockGetCurrentRow(final ViewObjectImpl mockVo,
                                            final Row currentRow)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return currentRow;
            }
        }).when(mockVo);
    }

    /**
     * @paraViewObjectImpl mockVo
     * @paraViewObjectImpl BaseViewObjectMocker
     */
    @Override
    public ViewObjectImpl mockIsExecuted(final ViewObjectImpl mockVo,
                                         final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final ViewObjectMockedState voState = voMocker
                    .getMockedVoState();
                return voState.isExecuted();
            }
        }).when(mockVo);
    }


    /**
     * @paraViewObjectImpl mockVo
     */
    @Override
    public ViewObjectImpl mockSetCurrentRow(final ViewObjectImpl mockVo,
                                            final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final ViewObjectMockedState voState = voMocker
                    .getMockedVoState();
                voState.setCurrentRow((Row) invocation.getArguments()[0]);
                return null;
            }
        }).when(mockVo);
    }


    /**
     * Consider putting insert locking after initialization. HGrid is not
     * allowed to insert do to limitation of modifying HGrid hierarchy in real
     * world.
     */
    @Override
    public ViewObjectImpl mockInsertRow(final ViewObjectImpl mockVo,
                                        final BaseViewObjectMocker voMocker)
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
        }).when(mockVo);
    }


    /**
     * Consider putting insert locking after initialization. HGrid is not
     * allowed to insert do to limitation of modifying HGrid hierarchy in real
     * world.
     */
    @Override
    public ViewObjectImpl mockInsertRowAtRangeIndex(final ViewObjectImpl mockVo,
                                                    final BaseViewObjectMocker voMocker)
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
        }).when(mockVo);
    }

    @Override
    public ViewObjectImpl mockSetRangeSize(final ViewObjectImpl mockVo,
                                           final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer size = (Integer) invocation.getArguments()[0];
                final ViewObjectMockedState voState = voMocker
                    .getMockedVoState();
                voState.setRangeSize(size);
                return null;
            }
        }).when(mockVo);
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
