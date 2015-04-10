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

import java.util.Map;

import oafext.test.RowSetMocker;
import oafext.test.ViewCriteriaMocker;
import oafext.test.mock.MockRowCallback;
import oafext.test.server.responder.ViewObjectResponder;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

import org.junit.After;
import org.mockito.Mockito;


/**
 * This wraps the view object mock to extends its functionality beyond the
 * mocked methods.
 *
 * Composite pattern used.
 *
 * @author royce
 *
 * @param <V> view object type.
 * @param <R> row type.
 */
public class BaseViewObjectMocker<V extends ViewObjectImpl, R extends ViewRowImpl>
        extends RowSetMocker<V, R> {

    /** */
    private transient ViewCriteriaMocker viewCritMocker;


    /** */
    private final transient ViewObjectType viewObjectType;


    /** */
    private final transient ViewObjectResponder<V, R> voResponder;

    /** */
    private transient MockRowCallback<R, V> rowMockCallback;


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
            final ViewObjectResponder<V, R> pVoResponder) {

        super(pAmFixture, pViewObjectName);
        this.viewObjectType = pVoType;
        this.voResponder = pVoResponder;

        setRowSetMockState(new ViewObjectMockState(pViewObjectName));

        final Map<String, Class<? extends ViewObjectImpl>> voNameClassMap =
                pAmFixture.getVoNameClassMap();

        @SuppressWarnings("unchecked")
        final Class<V> viewObjectClass =
                (Class<V>) voNameClassMap.get(pViewObjectName);

        setMock(Mockito.mock(viewObjectClass));
        getVoResponder().mockMethods(pAmFixture, this);
    }


    @After
    void tearDown()
    {
        assert getRowSetIterMap().isEmpty();
        //this.rowSetIterMap.clear(); //TODO: Remove after test verification.
        for (final RowMocker<R, V> rowMocker : getRowMockerList()) {
            rowMocker.tearDown();
        }

        getRowMockerList().clear();
    }


    /**
     * Removes the mocker and the mocked row along with it. <br/>
     *
     * <b>TODO</b>: What happens to currentRow if it is removed. Does it now
     * point to null or does it point to a dead row?
     *
     * @param rowMocker row mocker instance to remove.
     */
    public void remove(final RowMocker<R, V> rowMocker)
    {
        rowMocker.setRemoved(true);
        getRowMockerList().remove(rowMocker);
    }


    public boolean isHGrid()
    {
        return ViewObjectType.HGrid == this.viewObjectType;
    }


    public ViewObjectType getViewObjectType()
    {
        return this.viewObjectType;
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
        return (ViewObjectMockState) super.getRowSetMockState();
    }

    ViewObjectResponder<V, R> getVoResponder()
    {
        return this.voResponder;
    }

    /**
     * This will allow client specific mock functionality.
     *
     * @param rowMocker row mocker instance.
     * @param setUp {@link oafext.test.mock.MockRowCallback#callback(RowMocker, boolean)}
     */
    public void callClient(final RowMocker<R, V> rowMocker, final boolean setUp)
    {
        if (this.rowMockCallback != null) {
            this.rowMockCallback.callback(rowMocker, setUp);
        }
    }


    /**
     * @param rowMockCallback the rowMockCallback to set
     */
    public void setRowMockCallback(final MockRowCallback<R, V> rowMockCallback)
    {
        this.rowMockCallback = rowMockCallback;
    }


    public ViewCriteriaMocker getViewCritMocker()
    {
        return this.viewCritMocker;
    }


    public void setViewCritMocker(final ViewCriteriaMocker viewCritMocker)
    {
        this.viewCritMocker = viewCritMocker;
    }

    /**
     * Down cast.
     *
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public V getMock()
    {
        return (V) super.getMock();
    }

}
