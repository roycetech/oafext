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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.jbo.Row;
import oracle.jbo.server.ViewObjectImpl;

import org.junit.After;
import org.mockito.Matchers;
import org.mockito.Mockito;


/**
 * @author royce
 * 
 */
public class ViewObjectMocker {


    /** */
    private final transient ViewObjectImpl mockVo;


    /** */
    private final transient List<RowMocker> rowMockerList = new ArrayList<RowMocker>();


    /** */
    private final transient Map<String, RowSetIteratorMocker> rowSetIterMap = new HashMap<String, RowSetIteratorMocker>();

    /**
     * Temporary place holder for new rows. This is cleared when the row is
     * actually inserted in the VO.
     */
    private final transient Map<Row, RowMocker> newRowsMap = new HashMap<Row, RowMocker>();


    /** */
    private transient Row currentRow;


    /** Internal row iterator pointer. #first(), #next, #last(), #reset(). */
    private transient Row rowPointer = BEFORE_FIRST_ROW;


    /** Pointer is NULL, before the fist row. */
    public static final Row BEFORE_FIRST_ROW = Mockito.mock(Row.class);

    /** Pointer is NULL, after the last row. */
    public static final Row AFTER_LAST_ROW = Mockito.mock(Row.class);


    /** */
    private transient boolean executed;

    /** */
    private transient int rangeStart;

    /** */
    private transient int rangeSize = 1;


    @After
    void tearDown()
    {
        this.rowSetIterMap.clear();
    }


    /**
     * Commented are the mocked methods.
     * 
     * @param appModule mock application module instance.
     * @param viewObjectClass view object class.
     * @param rowClass row class
     * @param viewObjectName view instance name.
     */
    ViewObjectMocker(final AppModuleFixture<?> amFixture,
            final String viewObjectName) {

        final OAApplicationModuleImpl appModule = amFixture.getMockAppModule();

        final Map<String, Class<? extends ViewObjectImpl>> voNameClassMap = amFixture
            .getVoNameClassMap();

        final Class<? extends ViewObjectImpl> viewObjectClass = voNameClassMap
            .get(viewObjectName);

        final Class<? extends Row> rowClass = amFixture
            .getVoNameRowClsMap()
            .get(viewObjectName);

        this.mockVo = Mockito.mock(viewObjectClass);

        /* getName() */
        Mockito.doReturn(viewObjectName).when(this.mockVo).getName();

        /* getApplicationModule() */
        Mockito.doReturn(appModule).when(this.mockVo).getApplicationModule();

        /* getRowClass() */
        Mockito.doReturn(rowClass).when(this.mockVo).getRowClass();

        /* getFullName() */
        Mockito.when(this.mockVo.getFullName()).thenReturn(
            "Mock Full Name" + viewObjectName);

        /* getViewObject() */
        Mockito.when(this.mockVo.getViewObject()).thenReturn(this.mockVo);

        /* getRowSet() */
        Mockito.when(this.mockVo.getRowSet()).thenReturn(this.mockVo);

        /* setRangeSize(int) */
        ViewObjectAnswers.mockSetRangeSize(this.mockVo, this).setRangeSize(
            Matchers.anyInt());

        /* createRow() */
        ViewObjectAnswers
            .mockCreateRow(this.mockVo, amFixture, this)
            .createRow();

        /* insertRow(Row) */
        ViewObjectAnswers.mockInsertRow(this.mockVo, this).insertRow(
            (Row) Matchers.any());

        /* insertRowAtRangeIndex(int, Row) */
        ViewObjectAnswers
            .mockInsertRowAtRangeIndex(this.mockVo, this)
            .insertRowAtRangeIndex(Matchers.anyInt(), (Row) Matchers.any());

        /* getCurrentRow() */
        ViewObjectAnswers
            .mockGetCurrentRow(this.mockVo, this.currentRow)
            .getCurrentRow();

        /* setCurrentRow() */
        ViewObjectAnswers.mockSetCurrentRow(this.mockVo, this).setCurrentRow(
            (Row) Matchers.any());

        /* first() */
        ViewObjectAnswers.mockFirst(this.mockVo, this).first();

        /* getRowCount() */
        ViewObjectAnswers.mockGetRowCount(this.mockVo, this).getRowCount();

        /* createRowSetIterator() */
        ViewObjectAnswers
            .mockCreateRowSetIterator(this.mockVo, this)
            .createRowSetIterator(Matchers.anyString());

        //getRowAtRangeIndex(int).
        ViewObjectAnswers
            .mockGetRowAtRangeIndex(this.mockVo, this)
            .getRowAtRangeIndex(Matchers.anyInt());

        //getAllRowsInRange().
        ViewObjectAnswers
            .mockGetAllRowsInRange(this.mockVo, this)
            .getAllRowsInRange();

        //executeQuery().
        ViewObjectAnswers.mockExecuteQuery(this.mockVo, this).executeQuery();

        //isExecuted().
        ViewObjectAnswers.mockIsExecuted(this.mockVo, this).isExecuted();

        //getAttributeDef().
        ViewObjectAnswers
            .mockGetAttributeDef(this.mockVo, amFixture)
            .getAttributeDef(Matchers.anyInt());

        //getAttributeCount().
        ViewObjectAnswers
            .mockGetAttributeCount(this.mockVo, amFixture)
            .getAttributeCount();

        //getAttributeIndexOf().
        ViewObjectAnswers
            .mockGetAttributeIndexOf(this.mockVo, amFixture)
            .getAttributeIndexOf(Matchers.anyString());


    }


    //TODO: What happens to currentRow if it is removed.  Does it now point to null or does it point to a dead row?
    void remove(final RowMocker rowMocker)
    {
        this.rowMockerList.remove(rowMocker);
    }

    /**
     * @return the mockVo
     */
    ViewObjectImpl getMockVo()
    {
        return this.mockVo;
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
     * @return the rowMockerList
     */
    List<RowMocker> getRowMockerList()
    {
        return this.rowMockerList;
    }


    /**
     * @return the rowSetIterMap
     */
    Map<String, RowSetIteratorMocker> getRowSetIterMap()
    {
        return this.rowSetIterMap;
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


    /**
     * @return the newRowsMap
     */
    Map<Row, RowMocker> getNewRowsMap()
    {
        return this.newRowsMap;
    }

}
