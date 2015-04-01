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

import oafext.test.RowSetMocker;
import oafext.test.mock.Mocker;
import oafext.test.server.responder.RowSetIteratorResponder;
import oracle.jbo.RowSetIterator;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author royce
 *
 * @param <V> View Object type.
 * @param <R> Row type.
 */
public class RowSetIteratorMocker<V extends ViewObjectImpl, R extends ViewRowImpl>
        implements Mocker<RowSetIterator> {


    /** sl4j logger instance. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory
        .getLogger(RowSetIteratorMocker.class);


    /** */
    private final transient RowSetIterator mockIter;

    /** */
    private final transient IteratorMockState iterMockState;


    /** */
    private final transient RowSetIteratorResponder<V, R> responder;


    /**
     * @param pName iterator name.
     * @param rowSetMocker row set mocker.
     */
    public RowSetIteratorMocker(final String pName,
            final RowSetMocker<V, R> rowSetMocker) {

        assert pName != null;
        this.mockIter = Mockito.mock(RowSetIterator.class);
        this.responder = new RowSetIteratorResponder(this.mockIter);

        this.iterMockState = new IteratorMockState();

        /* getName() */
        Mockito.doReturn(pName).when(this.mockIter).getName();

        /* getRowAtRangeIndex() */
        getResponder().mockGetRowAtRangeIndex(rowSetMocker).getRowAtRangeIndex(
            Matchers.anyInt());

        /* getRowCount() */
        getResponder().mockGetRowCount(rowSetMocker).getRowCount();


        /* hasNext() */
        getResponder().mockHasNext(this).hasNext();

        /* next() */
        getResponder().mockNext(this, rowSetMocker).next();

        /* previous() */
        getResponder().mockPrevious(this, rowSetMocker).previous();

        /* setRangeSize(int) */
        getResponder().mockSetRangeSize(this).setRangeSize(Matchers.anyInt());

        /* closeRowSetIterator() */
        getResponder().mockCloseRsIterator(rowSetMocker).closeRowSetIterator();

        /* reset() */
        getResponder().mockReset(this).reset();


    }

    RowSetIteratorResponder<V, R> getResponder()
    {
        return this.responder;
    }

    public IteratorMockState getIterMockState()
    {
        return this.iterMockState;
    }

    @Override
    public RowSetIterator getMock()
    {
        return this.mockIter;
    }


}
