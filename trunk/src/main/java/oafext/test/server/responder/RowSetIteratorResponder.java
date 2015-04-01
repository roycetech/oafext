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
import oafext.test.server.IteratorMockState;
import oafext.test.server.RowSetIteratorMocker;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author royce
 * @param <V> View Object type.
 * @param <R> Row Type.
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class RowSetIteratorResponder<V extends ViewObjectImpl, R extends ViewRowImpl> {


    /** sl4j logger instance. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory
        .getLogger(RowSetIteratorResponder.class);


    /** */
    private final transient RowSetIterator mockRsIter;


    /**
     * @param pMockRsIter mock RowSetIterator.
     */
    public RowSetIteratorResponder(final RowSetIterator pMockRsIter) {
        this.mockRsIter = pMockRsIter;
    }


    /**
     * @param rowMockerBox row mocker list container.
     */
    public RowSetIterator mockGetRowAtRangeIndex(final RowSetMocker<V, R> rowMockerBox)
    {

        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer param = (Integer) invocation.getArguments()[0];
                if (param < 0
                        || param >= rowMockerBox.getRowMockerList().size()) {
                    return null;
                } else {
                    return rowMockerBox.getRowMockerList().get(param).getMock();
                }
            }
        })
            .when(this.mockRsIter);

    }

    /**
     * @param rsIterMocker row set iterator mocker.
     */
    public RowSetIterator mockHasNext(final RowSetIteratorMocker<V, R> rsIterMocker)
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
     * @param rowMockerBox row mocker list container instance.
     */
    public RowSetIterator mockNext(final RowSetIteratorMocker<V, R> rsIterMocker,
                                   final RowSetMocker<V, R> rowMockerBox)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final IteratorMockState iterState = rsIterMocker
                    .getIterMockState();

                if (iterState.getRangeCurrent() < iterState.getRangeEnd()
                        && iterState.getRangeCurrent() < rowMockerBox
                            .getRowMockerList()
                            .size()) {
                    iterState.increment();
                }

                if (iterState.getRangeCurrent() < iterState.getRangeEnd()) {

                    iterState.setHasNext(iterState.getRangeCurrent() < iterState
                        .getRangeEnd() - 1);

                    return rowMockerBox
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
     */
    public RowSetIterator mockPrevious(final RowSetIteratorMocker<V, R> rsIterMocker,
                                       final RowSetMocker<V, R> rowSetMocker)
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

                    return rowSetMocker
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
    public RowSetIterator mockReset(final RowSetIteratorMocker<V, R> rowSetIterMocker)
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
     * @param rowSetMocker row mocker list container instance.
     */
    public RowSetIterator mockCloseRsIterator(final RowSetMocker<V, R> rowSetMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                rowSetMocker.getRowSetIterMap().remove(
                    RowSetIteratorResponder.this.mockRsIter.getName());
                return null;
            }
        }).when(this.mockRsIter);
    }

    /**
     * @param rsIterMocker row set iterator mocker.
     */
    public RowSetIterator mockSetRangeSize(final RowSetIteratorMocker<V, R> rsIterMocker)
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
     * @param rowMockerBox row mocker container instance.
     */
    public RowSetIterator mockGetRowCount(final RowSetMocker<V, R> rowMockerBox)
    {
        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return rowMockerBox.getRowMockerList().size();
            }
        }).when(this.mockRsIter);
    }

}
