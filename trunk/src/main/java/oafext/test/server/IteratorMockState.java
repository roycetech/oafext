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


/**
 * Mock state of RowSetIterator.
 *
 * @version $Date$
 */
public class IteratorMockState {


    /** */
    private static final int RANGE_B4_FIRST = -1;


    /** */
    private transient boolean hasNext = true;

    /** */
    private transient int rangeStart;

    /** */
    private transient int rangeEnd = 1;

    /** */
    private transient int rangeCurrent = RANGE_B4_FIRST;


    IteratorMockState(final int pRangeEnd) {
        this.rangeEnd = pRangeEnd;
    }

    /**
     * @return the hasNext
     */
    public boolean isHasNext()
    {
        return this.hasNext;
    }

    /**
     * @param hasNext the hasNext to set
     */
    public void setHasNext(final boolean hasNext)
    {
        this.hasNext = hasNext;
    }

    /**
     * @return the rangeStart
     */
    public int getRangeStart()
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
    public int getRangeEnd()
    {
        return this.rangeEnd;
    }

    /**
     * @param rangeEnd the rangeEnd to set
     */
    public void setRangeEnd(final int rangeEnd)
    {
        this.rangeEnd = rangeEnd;
    }

    /**
     * @return the rangeCurrent
     */
    public int getRangeCurrent()
    {
        return this.rangeCurrent;
    }

    /** */
    public void increment()
    {
        if (this.rangeCurrent < this.rangeEnd) {
            this.rangeCurrent++;
        }
    }

    /** */
    public void decrement()
    {
        if (this.rangeCurrent > this.rangeStart) {
            this.rangeCurrent--;
        }
    }

    /** */
    public void reset()
    {
        this.rangeCurrent = RANGE_B4_FIRST;
    }


    /**
     * @param rangeCurrent the rangeCurrent to set
     */
    private void setRangeCurrent(final int rangeCurrent)
    {
        this.rangeCurrent = rangeCurrent;
    }

}
