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
import oafext.test.server.RowMocker;
import oafext.test.server.RowSetMockState;
import oracle.jbo.Row;
import oracle.jbo.RowSet;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Default ViewObject method responder. Used for simple view objects.
 *
 * @author royce
 * @param <R> Row type.
 * @param <V> View Object type.
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class ViewObjectDefaultResponder<V extends ViewObjectImpl, R extends ViewRowImpl>
        extends BaseViewObjectResponder<V, R> {


    /** {@inheritDoc} */
    @Override
    public RowSet mockGetAllRowsInRange(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Row[]>() {

            @Override
            public Row[] answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final RowSetMockState rsMockState = rowSetMocker
                    .getRowSetMockState();

                final List<Row> retval = new ArrayList<Row>();

                final List<RowMocker<R, V>> rowMockerList = rowSetMocker
                    .getRowMockerList();

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

}
