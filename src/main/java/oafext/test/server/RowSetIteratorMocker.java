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

import oracle.jbo.RowSetIterator;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author royce
 * 
 */
public class RowSetIteratorMocker {


    /** sl4j logger instance. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory
        .getLogger(RowSetIteratorMocker.class);


    /** */
    private final transient RowSetIterator mockRsIter;

    /** */
    private transient boolean hasNext = true;

    /** */
    private transient int rangeStart;

    /** */
    private transient int rangeEnd = 1;

    /** */
    private transient int rangeCurrent = -1;


    RowSetIteratorMocker(final String pName,
            final BaseViewObjectMocker voMocker) {

        assert pName != null;

        this.mockRsIter = Mockito.mock(RowSetIterator.class);
        this.rangeEnd = voMocker.getRowMockerList().size();

        //LOGGER.info("New Iterator: " + pName + ',' + this.rangeEnd);


        /* getName() */
        Mockito.doReturn(pName).when(this.mockRsIter).getName();

        /* getRowAtRangeIndex() */
        RowSetIteratorAnswers.mockGetRowAtRangeIndex(
            this.mockRsIter,
            voMocker.getMockVo()).getRowAtRangeIndex(Matchers.anyInt());

        /* getRowCount() */
        RowSetIteratorAnswers
            .mockGetRowCount(this.mockRsIter, voMocker)
            .getRowCount();


        /* hasNext() */
        RowSetIteratorAnswers.mockHasNext(this.mockRsIter, this).hasNext();

        /* next() */
        RowSetIteratorAnswers.mockNext(this.mockRsIter, this, voMocker).next();

        /* previous() */
        RowSetIteratorAnswers
            .mockPrevious(this.mockRsIter, this, voMocker)
            .previous();

        /* setRangeSize(int) */
        RowSetIteratorAnswers
            .mockSetRangeSize(this.mockRsIter, this)
            .setRangeSize(Matchers.anyInt());

        /* closeRowSetIterator() */
        RowSetIteratorAnswers
            .mockCloseRsIterator(this.mockRsIter, voMocker)
            .closeRowSetIterator();

        /* reset() */
        RowSetIteratorAnswers.mockReset(this.mockRsIter, this).reset();


    }

    /**
     * @return the mockRsIter
     */
    RowSetIterator getMockRsIter()
    {
        return this.mockRsIter;
    }

    /**
     * @return the hasNext
     */
    boolean isHasNext()
    {
        return this.hasNext;
    }

    /**
     * @param hasNext the hasNext to set
     */
    void setHasNext(final boolean hasNext)
    {
        this.hasNext = hasNext;
    }

    /**
     * @return the rangeStart
     */
    int getRangeStart()
    {
        return this.rangeStart;
    }

    /**
     * @param rangeStart the rangeStart to set
     */
    void setRangeStart(final int rangeStart)
    {
        this.rangeStart = rangeStart;
    }

    /**
     * @return the rangeEnd
     */
    int getRangeEnd()
    {
        return this.rangeEnd;
    }

    /**
     * @param rangeEnd the rangeEnd to set
     */
    void setRangeEnd(final int rangeEnd)
    {
        this.rangeEnd = rangeEnd;
    }

    /**
     * @return the rangeCurrent
     */
    int getRangeCurrent()
    {
        return this.rangeCurrent;
    }

    void increment()
    {
        if (this.rangeCurrent < this.rangeEnd) {
            this.rangeCurrent++;
        }
    }

    void decrement()
    {
        if (this.rangeCurrent > this.rangeStart) {
            this.rangeCurrent--;
        }
    }

    /**
     * @param rangeCurrent the rangeCurrent to set
     */
    void setRangeCurrent(final int rangeCurrent)
    {
        this.rangeCurrent = rangeCurrent;
    }

}
