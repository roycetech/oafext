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

import oafext.test.server.BaseViewObjectMocker;
import oafext.test.server.RowMocker;
import oafext.test.server.ViewObjectMockState;
import oracle.jbo.Row;
import oracle.jbo.server.ViewObjectImpl;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Default ViewObject method responder. Used for simple view objects.
 *
 * @author royce
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class ViewObjectDefaultResponder extends BaseViewObjectResponder {


    /** {@inheritDoc} */
    @Override
    public ViewObjectImpl mockGetAllRowsInRange(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Row[]>() {

            @Override
            public Row[] answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final ViewObjectMockState voState = voMocker.getMockedVoState();

                final List<Row> retval = new ArrayList<Row>();

                final List<RowMocker> rowMockerList = voMocker
                    .getRowMockerList();

                final boolean fetchRangeAll = voState.getRangeSize() == -1;

                for (int i = voState.getRangeStart(); (i < voState
                    .getRangeSize() || fetchRangeAll)
                        && i < rowMockerList.size(); i++) {

                    retval.add(rowMockerList.get(i).getMock());
                }
                return retval.toArray(new Row[retval.size()]);
            }
        })
            .when(voMocker.getMock());
    }

    /** {@inheritDoc} */
    @Override
    public ViewObjectImpl mockGetRowAtRangeIndex(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer index = (Integer) invocation.getArguments()[0];
                return voMocker.getRowMockerList().get(index).getMock();
            }
        }).when(voMocker.getMock());

    }

    /** {@inheritDoc} */
    @Override
    public ViewObjectImpl mockGetRowCount(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return voMocker.getRowMockerList().size();
            }
        }).when(voMocker.getMock());
    }

}
