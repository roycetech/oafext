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

import oafext.test.server.BaseViewObjectMocker;
import oafext.test.server.IteratorMockState;
import oafext.test.server.RowSetIteratorMocker;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.server.ViewObjectImpl;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author royce
 *
 * @param <I> specific RowSetIterator sub class.
 *
 */
public final class RowSetIteratorResponder<I extends RowSetIterator> {


    /** sl4j logger instance. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory
        .getLogger(RowSetIteratorResponder.class);


    /** */
    private final transient I mockRsIter;


    /**
     * @param pMockRsIter mock RowSetIterator.
     */
    public RowSetIteratorResponder(final I pMockRsIter) {
        this.mockRsIter = pMockRsIter;
    }


    /**
     * @param mockVo mock view object instance.
     */
    public I mockGetRowAtRangeIndex(final ViewObjectImpl mockVo)
    {

        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer param = (Integer) invocation.getArguments()[0];
                if (param < 0 || param >= mockVo.getRowCount()) {
                    return null;
                } else {
                    return mockVo.getRowAtRangeIndex(param);
                }
            }
        }).when(this.mockRsIter);

    }

    /**
     * @param rsIterMocker row set iterator mocker.
     */
    public I mockHasNext(final RowSetIteratorMocker rsIterMocker)
    {
        return Mockito.doAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final IteratorMockState iterState = rsIterMocker
                    .getIterMockState();

                return iterState.isHasNext();

            }
        }).when(this.mockRsIter);
    }


    /**
     * @param rsIterMocker row set iterator mocker.
     * @param voMocker view object mocker.
     */
    public I mockNext(final RowSetIteratorMocker rsIterMocker,
                      final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final IteratorMockState iterState = rsIterMocker
                    .getIterMockState();

                if (iterState.getRangeCurrent() < iterState.getRangeEnd()
                        && iterState.getRangeCurrent() < voMocker
                            .getRowMockerList()
                            .size()) {
                    iterState.increment();
                }

                if (iterState.getRangeCurrent() < iterState.getRangeEnd()) {

                    iterState.setHasNext(iterState.getRangeCurrent() < iterState
                        .getRangeEnd() - 1);

                    return voMocker
                        .getRowMockerList()
                        .get(iterState.getRangeCurrent())
                        .getMock();
                } else {

                    return null;
                }

            }
        })
            .when(this.mockRsIter);
    }

    /**
     * @param rsIterMocker row set iterator mocker.
     * @param voMocker view object mocker.
     */
    public I mockPrevious(final RowSetIteratorMocker rsIterMocker,
                          final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final IteratorMockState iterState = rsIterMocker
                    .getIterMockState();

                if (iterState.getRangeCurrent() > iterState.getRangeStart()) {
                    iterState.decrement();

                    return voMocker
                        .getRowMockerList()
                        .get(iterState.getRangeCurrent())
                        .getMock();
                } else {
                    return null;
                }

            }
        }).when(this.mockRsIter);
    }


    /**
     * @param rowSetIterMocker row set iterator mocker.
     */
    public I mockReset(final RowSetIteratorMocker rowSetIterMocker)
    {

        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final IteratorMockState iterState = rowSetIterMocker
                    .getIterMockState();

                iterState.reset();
                return null;

            }
        }).when(this.mockRsIter);

    }

    /**
     * @param voMocker view object mocker.
     */
    public I mockCloseRsIterator(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                voMocker.getRowSetIterMap().remove(
                    RowSetIteratorResponder.this.mockRsIter.getName());
                return null;
            }
        }).when(this.mockRsIter);
    }

    /**
     * @param rsIterMocker row set iterator mocker.
     */
    public I mockSetRangeSize(final RowSetIteratorMocker rsIterMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final IteratorMockState iterState = rsIterMocker
                    .getIterMockState();

                final Integer newSize = (Integer) invocation.getArguments()[0];
                iterState.setRangeEnd(newSize);
                return null;
            }
        }).when(this.mockRsIter);
    }

    /**
     * @param voMocker view object mocker.
     */
    public I mockGetRowCount(final BaseViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return voMocker.getRowMockerList().size();
            }
        }).when(this.mockRsIter);
    }

}
