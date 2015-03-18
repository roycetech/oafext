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
 * Mocked state of view object.
 * 
 * @author Royce Remulla
 * @version $Date$
 *
 */
public class ViewObjectMockedState {


    /** */
    private final transient String viewObjectName;

    /** */
    private transient Row currentRow;


    /** Internal row iterator pointer. #first(), #next, #last(), #reset(). */
    private transient Row rowPointer = BEFORE_FIRST_ROW;


    /** Pointer is NULL, before the fist row. */
    public static final Row BEFORE_FIRST_ROW = Mockito.mock(Row.class);

    /** Pointer is NULL, after the last row. */
    public static final Row AFTER_LAST_ROW = Mockito.mock(Row.class);


    /** isExecuted flag. */
    private transient boolean executed;

    /** Range Start. */
    private transient int rangeStart;

    /** Range Size. */
    private transient int rangeSize = 1;


    ViewObjectMockedState(final String pViewObjectName) {
        viewObjectName = pViewObjectName;
    }


    /**
     * @return the currentRow
     */
    Row getCurrentRow()
    {
        return this.currentRow;
    }

    /**
     * @param currentRow the currentRow to set
     */
    void setCurrentRow(final Row currentRow)
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
    void setRowPointer(final Row rowPointer)
    {
        this.rowPointer = rowPointer;
    }

    /**
     * @return the rangeSize
     */
    int getRangeSize()
    {
        return this.rangeSize;
    }


    /**
     * @param rangeSize the rangeSize to set
     */
    void setRangeSize(final int rangeSize)
    {
        this.rangeSize = rangeSize;
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
     * @return the executed
     */
    public boolean isExecuted()
    {
        return this.executed;
    }


    /**
     * @param executed the executed to set
     */
    public void setExecuted(final boolean executed)
    {
        this.executed = executed;
    }


    String getViewObjectName()
    {
        return viewObjectName;
    }


}
