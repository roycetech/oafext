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
package oafext.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oafext.test.server.AbstractIteratorMocker;
import oafext.test.server.AppModuleFixture;
import oafext.test.server.BaseViewObjectMocker;
import oafext.test.server.RowMocker;
import oafext.test.server.RowSetMockState;
import oafext.test.server.ViewObjectHGridMocker;
import oafext.test.server.responder.BaseRowSetResponder;
import oafext.test.server.responder.RowSetResponder;
import oracle.jbo.Row;
import oracle.jbo.RowSet;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

import org.mockito.Mockito;


/**
 * Composite. Can be View Object or View Link Row based.
 *
 * @version $Date$
 *
 * @param <V> View Object Type.
 * @param <R> Row Type.
 */
public class RowSetMocker<V extends ViewObjectImpl, R extends ViewRowImpl>
        extends AbstractIteratorMocker<RowSet> {


    /** */
    private final transient AppModuleFixture<?> amFixture;

    /** */
    private transient RowSet mock;

    /** */
    private transient RowSetMockState rowSetMockState;


    /** */
    private final transient RowSetResponder<V, R> rsResponder =
            new BaseRowSetResponder<V, R>();


    /**
     * Temporary place holder for new rows. This is cleared when the row is
     * actually inserted in the VO.
     */
    private final transient Map<Row, RowMocker<R, V>> newRowsMap =
            new HashMap<>();

    /** */
    private final transient List<RowMocker<R, V>> rowMockerList =
            new ArrayList<>();


    /** Non-null for Row-based RowSet. */
    private transient RowMocker<R, V> rowMocker;

    /** */
    private final transient Class<R> rowClass;


    /**
     * Allow instantiation for ViewObject sub class only and through Factory
     * method for Row based VL RowSet.
     *
     * @param pAmFixture application module fixture.
     * @param pVoMocker view object mocker instance. null for sub classed
     *            ViewObject mocker. non-null for attribute RowSet.
     * @param pVoName view object instance name.
     */
    @SuppressWarnings("unchecked")
    protected RowSetMocker(final AppModuleFixture<?> pAmFixture,
            final String pVoName) {
        this.amFixture = pAmFixture;

        this.rowClass =
                (Class<R>) this.amFixture.getVoNameRowClsMap().get(pVoName);
        setRowSetMockState(new RowSetMockState());
    }

    /**
     * Factory method.
     *
     * @param pRowMocker RowMocker instance.
     *
     * @param <V> View Object Type.
     * @param <R> Row Type.
     */
    public static <V extends ViewObjectImpl, R extends ViewRowImpl> RowSetMocker<V, R> newInstance(final RowMocker<R, V> pRowMocker)
    {
        assert pRowMocker != null;
        assert pRowMocker.getVoMocker().isHGrid();

        final String voName =
                pRowMocker.getVoMocker().getMockedVoState().getViewObjectName();

        final RowSetMocker<V, R> retval =
                new RowSetMocker<V, R>(pRowMocker.getAmFixture(), voName);

        retval.setMock(Mockito.mock(RowSet.class));
        retval.setRowMocker(pRowMocker);
        retval.getRsResponder().mockMethods(pRowMocker.getAmFixture(), retval);
        final ViewObjectHGridMocker<V, R> voHgridMocker =
                (ViewObjectHGridMocker<V, R>) pRowMocker.getVoMocker();

        for (final RowMocker<R, V> rowMocker : voHgridMocker.getRowMockerList()) {
            final AppModuleFixture<?> amFixture = pRowMocker.getAmFixture();
            final String voDef = amFixture.getVoNameDefMap().get(voName);
            final List<String> attrList =
                    amFixture.getVoDefAttrListMap().get(voDef);
            final String pkeyAttr = attrList.get(0);
            final String fkeyAttr =
                    attrList.get(voHgridMocker.getParentAttrIdx());
            final Object pkey = pRowMocker.getAttrValueMap().get(pkeyAttr);

            final Object fkey = rowMocker.getAttrValueMap().get(fkeyAttr);
            if (pkey.equals(fkey)) {
                retval.getRowMockerList().add(rowMocker);
            }
        }
        retval.getRowSetMockState().setRangeEnd(
            retval.getRowMockerList().size());
        return retval;
    }

    public Map<Row, RowMocker<R, V>> getNewRowsMap()
    {
        return this.newRowsMap;
    }

    public List<RowMocker<R, V>> getRowMockerList()
    {
        return this.rowMockerList;
    }

    @Override
    public RowSet getMock()
    {
        return this.mock;
    }

    protected void setMock(final RowSet mock)
    {
        this.mock = mock;
    }


    /**
     * True for View Link Row based RowSet.
     */
    public boolean isRow()
    {
        return this.rowMocker != null;
    }

    /**
     * True for ViewObject sub classing this RowSet.
     */
    public boolean isViewObject()
    {
        return !isRow();
    }

    /**
     * @return the viewObjMocker
     */
    @SuppressWarnings("PMD.OnlyOneReturn" /* Two only. */)
    public BaseViewObjectMocker<V, R> getViewObjMocker()
    {
        if (isRow()) {
            return getRowMocker().getVoMocker();
        }
        return (BaseViewObjectMocker<V, R>) this;
    }


    /**
     * Return row class.
     */
    @SuppressWarnings("PMD.OnlyOneReturn" /* Two only. */)
    public Class<R> getRowClass()
    {
        if (isRow()) {
            return getRowMocker().getRowClass();
        }
        return this.rowClass;
    }

    /**
     * @return the rowSetMockState
     */
    public RowSetMockState getRowSetMockState()
    {
        return this.rowSetMockState;
    }

    /**
     * @param rowSetMockState the rowSetMockState to set
     */
    public final void setRowSetMockState(final RowSetMockState rowSetMockState)
    {
        this.rowSetMockState = rowSetMockState;
    }

    /**
     * @return the amFixture
     */
    public AppModuleFixture<?> getAmFixture()
    {
        return this.amFixture;
    }


    /**
     * @return the rsResponder
     */
    public RowSetResponder<V, R> getRsResponder()
    {
        return this.rsResponder;
    }

    /**
     * @return the rowMocker
     */
    public RowMocker<R, V> getRowMocker()
    {
        return this.rowMocker;
    }

    /**
     * @param rowMocker the rowMocker to set
     */
    public void setRowMocker(final RowMocker<R, V> rowMocker)
    {
        this.rowMocker = rowMocker;
    }

}
