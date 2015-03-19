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

import oafext.test.mock.Mocker;
import oafext.test.server.responder.ViewObjectResponder;
import oracle.jbo.Row;
import oracle.jbo.server.ViewObjectImpl;

import org.junit.After;
import org.mockito.Mockito;


/**
 * This wraps the view object mock to extends its functionality beyond the
 * mocked methods.
 *
 * Composite pattern used.
 *
 * @author royce
 */
public class BaseViewObjectMocker implements Mocker<ViewObjectImpl> {


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
    private final transient AppModuleFixture<?> amFixture;

    /** */
    private final transient ViewObjectType viewObjectType;


    /** */
    private final transient ViewObjectResponder<ViewObjectImpl> voResponder;


    /** */
    private final transient ViewObjectMockState mockedVoState;


    /**
     * Commented are the mocked methods.
     *
     * @param appModule mock application module instance.
     * @param viewObjectClass view object class.
     * @param rowClass row class
     * @param pViewObjectName view instance name.
     */
    BaseViewObjectMocker(final AppModuleFixture<?> pAmFixture,
            final String pViewObjectName, final ViewObjectType pVoType,
            final ViewObjectResponder<ViewObjectImpl> pVoResponder) {

        this.amFixture = pAmFixture;
        this.viewObjectType = pVoType;
        this.voResponder = pVoResponder;

        this.mockedVoState = new ViewObjectMockState(pViewObjectName);


        final Map<String, Class<? extends ViewObjectImpl>> voNameClassMap = pAmFixture
            .getVoNameClassMap();

        final Class<? extends ViewObjectImpl> viewObjectClass = voNameClassMap
            .get(pViewObjectName);


        this.mockVo = Mockito.mock(viewObjectClass);

        getVoResponder().mockMethods(pAmFixture, this);
    }


    @After
    void tearDown()
    {
        this.rowSetIterMap.clear();
    }


    /**
     * Removes the mocker and the mocked row along with it. <br/>
     *
     * <b>TODO</b>: What happens to currentRow if it is removed. Does it now
     * point to null or does it point to a dead row?
     *
     * @param rowMocker row mocker instance to remove.
     */
    public void remove(final RowMocker rowMocker)
    {
        this.rowMockerList.remove(rowMocker);
    }

    /**
     * @return the rowMockerList
     */
    public List<RowMocker> getRowMockerList()
    {
        return this.rowMockerList;
    }

    public boolean isHGrid()
    {
        return ViewObjectType.HGrid == this.viewObjectType;
    }

    /**
     * @return the rowSetIterMap
     */
    public Map<String, RowSetIteratorMocker> getRowSetIterMap()
    {
        return this.rowSetIterMap;
    }


    /**
     * @return the newRowsMap
     */
    public Map<Row, RowMocker> getNewRowsMap()
    {
        return this.newRowsMap;
    }

    ViewObjectType getViewObjectType()
    {
        return this.viewObjectType;
    }


    AppModuleFixture<?> getAmFixture()
    {
        return this.amFixture;
    }

    /** */
    enum ViewObjectType {

        /** Default type. */
        Single,

        /** HGrid based, recursive. */
        HGrid,

        /** TODO: View link. */
        Linked;
    }

    public ViewObjectMockState getMockedVoState()
    {
        return this.mockedVoState;
    }

    ViewObjectResponder<ViewObjectImpl> getVoResponder()
    {
        return this.voResponder;
    }


    @Override
    public ViewObjectImpl getMock()
    {
        return this.mockVo;
    }

}
