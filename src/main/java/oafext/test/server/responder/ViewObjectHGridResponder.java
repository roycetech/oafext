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

import oafext.test.RowSetMocker;
import oracle.jbo.Row;
import oracle.jbo.RowSet;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Convention: Mock View object is always the first parameter if present.
 *
 * @author royce
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class ViewObjectHGridResponder<V extends ViewObjectImpl, R extends ViewRowImpl>
        extends BaseViewObjectResponder<V, R> {


    @Override
    public RowSet mockGetAllRowsInRange(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Row[]>() {

            @Override
            public Row[] answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                if (rowSetMocker.getRowMockerList().isEmpty()) {
                    return new Row[0];
                } else {
                    return new Row[] { rowSetMocker
                        .getRowMockerList()
                        .get(0)
                        .getMock() };
                }
            }
        }).when(rowSetMocker.getMock());
    }

    @Override
    public RowSet mockGetRowAtRangeIndex(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer index = (Integer) invocation.getArguments()[0];
                assert index == 0;
                return rowSetMocker.getRowMockerList().get(index).getMock();
            }
        }).when(rowSetMocker.getMock());
    }

    @Override
    public RowSet mockGetRowCount(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                throw new UnsupportedOperationException(
                    "TODO: Determine the behavior for this.");
            }
        }).when(rowSetMocker.getMock());
    }

}
