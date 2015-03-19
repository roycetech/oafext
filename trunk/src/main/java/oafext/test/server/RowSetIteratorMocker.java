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

import oafext.test.mock.Mocker;
import oafext.test.server.responder.RowSetIteratorResponder;
import oracle.jbo.RowSetIterator;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author royce
 *
 */
public class RowSetIteratorMocker implements Mocker<RowSetIterator> {


    /** sl4j logger instance. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory
        .getLogger(RowSetIteratorMocker.class);


    /** */
    private final transient RowSetIterator mockRsIter;

    /** */
    private final transient IteratorMockState iterMockState;


    /** */
    private final transient RowSetIteratorResponder<RowSetIterator> responder;


    /**
     * @param pName iterator name.
     * @param voMocker view object mocker.
     */
    public RowSetIteratorMocker(final String pName,
            final BaseViewObjectMocker voMocker) {

        assert pName != null;

        this.mockRsIter = Mockito.mock(RowSetIterator.class);
        this.responder = new RowSetIteratorResponder<RowSetIterator>(
            this.mockRsIter);

        this.iterMockState = new IteratorMockState(voMocker
            .getRowMockerList()
            .size());

        /* getName() */
        Mockito.doReturn(pName).when(this.mockRsIter).getName();

        /* getRowAtRangeIndex() */
        getResponder()
            .mockGetRowAtRangeIndex(voMocker.getMock())
            .getRowAtRangeIndex(Matchers.anyInt());

        /* getRowCount() */
        getResponder().mockGetRowCount(voMocker).getRowCount();


        /* hasNext() */
        getResponder().mockHasNext(this).hasNext();

        /* next() */
        getResponder().mockNext(this, voMocker).next();

        /* previous() */
        getResponder().mockPrevious(this, voMocker).previous();

        /* setRangeSize(int) */
        getResponder().mockSetRangeSize(this).setRangeSize(Matchers.anyInt());

        /* closeRowSetIterator() */
        getResponder().mockCloseRsIterator(voMocker).closeRowSetIterator();

        /* reset() */
        getResponder().mockReset(this).reset();


    }

    RowSetIteratorResponder<RowSetIterator> getResponder()
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
        return this.mockRsIter;
    }


}
