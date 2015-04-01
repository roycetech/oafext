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

import java.util.LinkedHashMap;
import java.util.Map;

import oafext.test.RowSetMocker;
import oafext.test.mock.Mocker;
import oafext.test.server.responder.BaseRowResponder;
import oafext.test.server.responder.RowResponder;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

import org.junit.After;
import org.mockito.Mockito;


/**
 * NOTE: Overloaded accessor method for VORowImpl is not supported.
 *
 * @author royce
 *
 * @param <R> Row type.
 * @param <V> View Object type.
 */
public class RowMocker<R extends ViewRowImpl, V extends ViewObjectImpl>
        implements Mocker<R> {


    /** Wrapper for the final method getViewObject. */
    public static final String CUSTOM_GET_VO = "getViewObj";

    /** Wrapper for the final method getAttributeCount. */
    public static final String CUST_GET_ATTR_CNT = "getAttrCount";

    /** */
    private final transient AppModuleFixture<?> amFixture;

    /** */
    private final transient R mockRow;

    /** */
    private final transient Class<R> rowClass;

    /** */
    private final transient Map<String, Object> attrValueMap;

    /** */
    private final transient BaseViewObjectMocker<V, R> voMocker;

    /** */
    private transient RowSetMocker<V, R> rowSetMocker;

    /** */
    private final transient RowResponder<R, V> responder;

    /** Row state when removed. */
    private transient boolean removed;

    /**
     * @param pRowClass row class.
     * @param pAmFixture application module fixture.
     * @param pVoMocker view object mocker.
     */
    public RowMocker(final Class<R> pRowClass,
            final AppModuleFixture<?> pAmFixture,
            final BaseViewObjectMocker<V, R> pVoMocker) {

        this.rowClass = pRowClass;
        this.amFixture = pAmFixture;
        this.mockRow = Mockito.mock(pRowClass);
        this.voMocker = pVoMocker;

        this.responder = new BaseRowResponder<R, V>();

        this.attrValueMap = new LinkedHashMap<String, Object>();

        getResponder().mockMethods(this.amFixture, this, pRowClass);

    }

    @SuppressWarnings("unchecked")
    @After
    void tearDown()
    {
        Mockito.reset(this.mockRow);
    }


    /**
     * @return the attrValueMap
     */
    public Map<String, Object> getAttrValueMap()
    {
        return this.attrValueMap;
    }

    /**
     * @return the voMocker
     */
    public BaseViewObjectMocker<V, R> getVoMocker()
    {
        return this.voMocker;
    }

    @Override
    public R getMock()
    {
        return this.mockRow;
    }

    /**
     * @return the responder
     */
    public RowResponder<R, V> getResponder()
    {
        return this.responder;
    }

    /**
     * @return the rowClass
     */
    public Class<R> getRowClass()
    {
        return this.rowClass;
    }

    public boolean isRemoved()
    {
        return this.removed;
    }

    void setRemoved(final boolean removed)
    {
        this.removed = removed;
    }

    /**
     * @return the rowSetMocker
     */
    public RowSetMocker<V, R> getRowSetMocker()
    {
        return this.rowSetMocker;
    }

    /**
     * @return the amFixture
     */
    public AppModuleFixture<?> getAmFixture()
    {
        return this.amFixture;
    }

    /**
     * @param rowSetMocker the rowSetMocker to set
     */
    public void setRowSetMocker(final RowSetMocker<V, R> rowSetMocker)
    {
        this.rowSetMocker = rowSetMocker;
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {

        final StringBuilder strBuilder = new StringBuilder();
        strBuilder
            .append(this.getClass().getSimpleName())
            .append('\n')
            .append(this.rowClass)
            .append('\n')
            .append("Removed: ")
            .append(this.removed)
            .append('\n')
            .append(this.mockRow);
        return strBuilder.toString();
    }

}
