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
 */
public final class RowSetIteratorAnswers {


    /** sl4j logger instance. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory
        .getLogger(RowSetIteratorAnswers.class);

    /** */
    private RowSetIteratorAnswers() {}


    static <M> M mockGetRowAtRangeIndex(final M mockRsIter,
            final ViewObjectImpl mockVo)
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
        }).when(mockRsIter);

    }

    /**
     * @param mockRsIter
     * @param mockVo
     */
    static <M> M mockHasNext(final M mockRsIter,
            final RowSetIteratorMocker rsIterMocker)
    {
        return Mockito.doAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return rsIterMocker.isHasNext();

            }
        }).when(mockRsIter);
    }


    static <M extends RowSetIterator> M mockNext(final M mockRsIter,
            final RowSetIteratorMocker rsIterMocker,
            final ViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                if (rsIterMocker.getRangeCurrent() < rsIterMocker.getRangeEnd()
                        && rsIterMocker.getRangeCurrent() < voMocker
                            .getRowMockerList()
                            .size()) {
                    rsIterMocker.increment();
                }

                if (rsIterMocker.getRangeCurrent() < rsIterMocker.getRangeEnd()) {

                    rsIterMocker.setHasNext(rsIterMocker.getRangeCurrent() < rsIterMocker
                        .getRangeEnd() - 1);

                    return voMocker
                        .getRowMockerList()
                        .get(rsIterMocker.getRangeCurrent())
                        .getMockRow();
                } else {

                    return null;
                }

            }
        })
            .when(mockRsIter);
    }

    static <M> M mockPrevious(final M mockRsIter,
            final RowSetIteratorMocker rsIterMocker,
            final ViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                if (rsIterMocker.getRangeCurrent() > rsIterMocker
                    .getRangeStart()) {
                    rsIterMocker.decrement();

                    return voMocker
                        .getRowMockerList()
                        .get(rsIterMocker.getRangeCurrent())
                        .getMockRow();
                } else {
                    return null;
                }

            }
        }).when(mockRsIter);
    }


    /**
     * @param mockRsIter
     * @param rowSetIterMocker
     */
    static <M> M mockReset(final M mockRsIter,
            final RowSetIteratorMocker rowSetIterMocker)
    {

        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                rowSetIterMocker.setRangeCurrent(-1);
                return null;

            }
        }).when(mockRsIter);

    }

    static <M extends RowSetIterator> M mockCloseRsIterator(final M mockRsIter,
            final ViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                voMocker.getRowSetIterMap().remove(mockRsIter.getName());
                return null;
            }
        }).when(mockRsIter);
    }

    static <M> M mockSetRangeSize(final M mockRsIter,
            final RowSetIteratorMocker rowSetIterMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer newSize = (Integer) invocation.getArguments()[0];
                rowSetIterMocker.setRangeEnd(newSize);
                return null;
            }
        }).when(mockRsIter);
    }

    static <M> M mockGetRowCount(final M mockRsIter,
            final ViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return voMocker.getRowMockerList().size();
            }
        }).when(mockRsIter);
    }

}
