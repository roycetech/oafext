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

import java.util.ArrayList;
import java.util.List;

import oafext.test.RowSetMocker;
import oafext.test.server.AppModuleFixture;
import oafext.test.server.BaseViewObjectMocker;
import oafext.test.server.RowMocker;
import oafext.test.server.RowSetIteratorMocker;
import oafext.test.server.RowSetMockState;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.jbo.Row;
import oracle.jbo.RowIterator;
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
 * @param <V> View Object type.
 * @param <R> Row type.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class BaseRowSetResponder<V extends ViewObjectImpl, R extends ViewRowImpl>
        implements RowSetResponder<V, R> {

    @Override
    public void mockMethods(final AppModuleFixture<?> amFixture,
                            final RowSetMocker<V, R> rowSetMocker)
    {
        final BaseViewObjectMocker<V, R> voMocker =
                rowSetMocker.getViewObjMocker();

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


        /* setRangeSize(int) */
        mockSetRangeSize(rowSetMocker).setRangeSize(Matchers.anyInt());

        /* createRow() */
        mockCreateRow(amFixture, rowSetMocker).createRow();

        /* insertRow(Row) */
        mockInsertRow(rowSetMocker).insertRow((Row) Matchers.any());

        /* insertRowAtRangeIndex(int, Row) */
        mockInsertRowAtRangeIndex(rowSetMocker).insertRowAtRangeIndex(
            Matchers.anyInt(),
            (Row) Matchers.any());

        /* getCurrentRow() */
        mockGetCurrentRow(rowSetMocker).getCurrentRow();

        /* setCurrentRow() */
        mockSetCurrentRow(rowSetMocker).setCurrentRow((Row) Matchers.any());

        /* first() */
        mockFirst(rowSetMocker).first();

        /* getRowCount() */
        mockGetRowCount(rowSetMocker).getRowCount();

        /* createRowSetIterator() */
        mockCreateRowSetIterator(rowSetMocker).createRowSetIterator(
            Matchers.anyString());

        //getRowAtRangeIndex(int).
        mockGetRowAtRangeIndex(rowSetMocker).getRowAtRangeIndex(
            Matchers.anyInt());

        //getAllRowsInRange().
        mockGetAllRowsInRange(rowSetMocker).getAllRowsInRange();

        //executeQuery().
        mockExecuteQuery(rowSetMocker).executeQuery();

        //isExecuted().
        mockIsExecuted(rowSetMocker).isExecuted();

        //isExecuted().
        mockToString(amFixture, rowSetMocker).toString();

    }


    /** {@inheritDoc} */
    @Override
    public RowSet mockCreateRow(final AppModuleFixture<?> amFixture,
                                final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final BaseViewObjectMocker<V, R> voMocker =
                        rowSetMocker.getViewObjMocker();
                final Class<R> rowClass = rowSetMocker.getRowClass();
                final RowMocker<R, V> rowMocker =
                        new RowMocker<R, V>(rowClass, amFixture, voMocker);
                voMocker.getNewRowsMap().put(rowMocker.getMock(), rowMocker);
                voMocker.callClient(rowMocker);
                return rowMocker.getMock();
            }
        }).when(rowSetMocker.getMock());
    }

    /**
     * @paraViewObjectImpl mockVo
     * @paraViewObjectImpl BaseViewObjectMocker
     * @return
     */
    @Override
    public RowSet mockCreateRowSetIterator(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<RowIterator>() {

            @Override
            public RowIterator answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String iterName = invocation.getArguments()[0].toString();

                assert iterName != null;
                assert rowSetMocker.getRowSetIterMap().get(iterName) == null;

                final RowSetIteratorMocker<V, R> rsIterMocker =
                        new RowSetIteratorMocker<V, R>(iterName, rowSetMocker);
                rowSetMocker.getRowSetIterMap().put(iterName, rsIterMocker);

                return rsIterMocker.getMock();
            }
        })
            .when(rowSetMocker.getMock());
    }

    /**
     * @paraViewObjectImpl mockVo
     * @paraViewObjectImpl BaseViewObjectMocker
     */
    @Override
    public RowSet mockExecuteQuery(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final RowSetMockState rsMockState =
                        rowSetMocker.getRowSetMockState();
                rsMockState.setExecuted(true);
                return null;
            }
        }).when(rowSetMocker.getMock());
    }

    /**
     * @paraViewObjectImpl mockVo
     */
    @Override
    public RowSet mockFirst(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<RowMocker<R, V>> rowMockerList =
                        rowSetMocker.getRowMockerList();

                if (rowMockerList.isEmpty()) {
                    return null;
                } else {
                    final Row firstRow = rowMockerList.get(0).getMock();
                    final RowSetMockState rsMockState =
                            rowSetMocker.getRowSetMockState();
                    rsMockState.setRowPointer(firstRow);
                    return firstRow;
                }
            }
        }).when(rowSetMocker.getMock());
    }

    @Override
    public RowSet mockGetCurrentRow(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final RowSetMockState rsMockState =
                        rowSetMocker.getRowSetMockState();
                return rsMockState.getCurrentRow();
            }
        }).when(rowSetMocker.getMock());
    }


    /** {@inheritDoc} */
    @Override
    public RowSet mockIsExecuted(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final RowSetMockState rsMockState =
                        rowSetMocker.getRowSetMockState();
                return rsMockState.isExecuted();
            }
        }).when(rowSetMocker.getMock());
    }


    /**
     * @paraViewObjectImpl mockVo
     */
    @Override
    public RowSet mockSetCurrentRow(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final RowSetMockState rowSetState =
                        rowSetMocker.getRowSetMockState();
                rowSetState.setCurrentRow((Row) invocation.getArguments()[0]);
                return null;
            }
        }).when(rowSetMocker.getMock());
    }


    /*
     * Consider putting insert locking after initialization. HGrid is not
     * allowed to insert do to limitation of modifying HGrid hierarchy in real
     * world.
     */
    @Override
    public RowSet mockInsertRow(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Row mockRow = (Row) invocation.getArguments()[0];
                final RowMocker<R, V> rowMocker =
                        rowSetMocker.getNewRowsMap().remove(mockRow);

                rowSetMocker.getRowMockerList().add(rowMocker);
                return null;
            }
        }).when(rowSetMocker.getMock());
    }


    /**
     * Consider putting insert locking after initialization. HGrid is not
     * allowed to insert do to limitation of modifying HGrid hierarchy in real
     * world.
     */
    @Override
    public RowSet mockInsertRowAtRangeIndex(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer index = (Integer) invocation.getArguments()[0];
                assert index <= rowSetMocker.getRowMockerList().size();
                final Row mockRow = (Row) invocation.getArguments()[1];

                final RowMocker<R, V> rowMocker =
                        rowSetMocker.getNewRowsMap().remove(mockRow);

                final List<RowMocker<R, V>> rowMockerList =
                        rowSetMocker.getRowMockerList();
                rowMockerList.add(index, rowMocker);
                return null;
            }
        }).when(rowSetMocker.getMock());
    }

    @Override
    public RowSet mockSetRangeSize(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer size = (Integer) invocation.getArguments()[0];
                final RowSetMockState rsMockState =
                        rowSetMocker.getRowSetMockState();
                rsMockState.setRangeSize(size);
                return null;
            }
        }).when(rowSetMocker.getMock());
    }


    /** {@inheritDoc} */
    @Override
    public RowSet mockGetAllRowsInRange(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Row[]>() {

            @Override
            public Row[] answer(final InvocationOnMock invocation)
                    throws Throwable
            {

                final List<Row> retval = new ArrayList<Row>();

                final List<RowMocker<R, V>> rowMockerList =
                        rowSetMocker.getRowMockerList();

                final RowSetMockState rsMockState =
                        rowSetMocker.getRowSetMockState();
                final boolean fetchRangeAll = rsMockState.getRangeSize() == -1;

                for (int i = rsMockState.getRangeStart(); (i < rsMockState
                    .getRangeSize() || fetchRangeAll)
                        && i < rowMockerList.size(); i++) {

                    retval.add(rowMockerList.get(i).getMock());
                }
                return retval.toArray(new Row[retval.size()]);
            }
        }).when(rowSetMocker.getMock());
    }

    /** {@inheritDoc} */
    @Override
    public RowSet mockGetRowAtRangeIndex(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer index = (Integer) invocation.getArguments()[0];
                return rowSetMocker.getRowMockerList().get(index).getMock();
            }
        }).when(rowSetMocker.getMock());

    }

    /** {@inheritDoc} */
    @Override
    public RowSet mockGetRowCount(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return rowSetMocker.getRowMockerList().size();
            }
        }).when(rowSetMocker.getMock());
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
                final StringBuilder strBuilder = new StringBuilder();
                strBuilder
                    .append("Mock for OafExt, hashCode: ")
                    .append(rowSetMocker.getMock().hashCode())
                    .append('\n');

                for (final RowMocker<R, V> rowMocker : rowSetMocker
                    .getRowMockerList()) {

                    strBuilder.append(rowMocker.getMock());
                }

                return strBuilder.toString();
            }
        }).when(rowSetMocker.getMock());
    }
}
