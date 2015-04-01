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

import org.mockito.Mockito;


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


    /** Range Start. */
    private transient int rangeStart;

    /** */
    private transient int rangeEnd = 1;

    /** */
    private transient int rangeCurrent = RANGE_B4_FIRST;

    /** */
    private transient Row currentRow;

    /** Range Size. */
    private transient int rangeSize = 1;


    /** Internal row iterator pointer. #first(), #next, #last(), #reset(). */
    private transient Row rowPointer = BEFORE_FIRST_ROW;


    /** Pointer is NULL, before the fist row. */
    public static final Row BEFORE_FIRST_ROW = Mockito.mock(Row.class);

    /** Pointer is NULL, after the last row. */
    public static final Row AFTER_LAST_ROW = Mockito.mock(Row.class);


    //    IteratorMockState(final int pRangeEnd) {
    //        this.rangeEnd = pRangeEnd;
    //    }

    //IteratorMockState() {}

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
        if (this.rangeSize == 1) {
            this.rangeSize = rangeEnd;
        }
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
     * @return the currentRow
     */
    public Row getCurrentRow()
    {
        return this.currentRow;
    }

    /**
     * @param currentRow the currentRow to set
     */
    public void setCurrentRow(final Row currentRow)
    {
        this.currentRow = currentRow;
    }

    /**
     * @return the rowPointer
     */
    Row getRowPointer()
    {
        return this.rowPointer;
    }

    /**
     * @param rowPointer the rowPointer to set
     */
    public void setRowPointer(final Row rowPointer)
    {
        this.rowPointer = rowPointer;
    }

    /**
     * @return the rangeSize
     */
    public int getRangeSize()
    {
        return this.rangeSize;
    }


    /**
     * @param rangeSize the rangeSize to set
     */
    public void setRangeSize(final int rangeSize)
    {
        this.rangeSize = rangeSize;
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        final StringBuilder strBuilder = new StringBuilder();
        strBuilder
            .append(getClass().getSimpleName())
            .append(" Hash Code: ")
            .append(hashCode())
            .append('\n')
            .append("Start=")
            .append(this.rangeStart)
            .append("\nEnd=")
            .append(this.rangeEnd)
            .append("\nSize=")
            .append(this.rangeSize)
            .append("\nHas Next=")
            .append(this.hasNext)
            .append("\ncurrent=")
            .append(this.currentRow)
            .append("\nRange current=")
            .append(this.rangeCurrent);

        return strBuilder.toString();
    }
}
