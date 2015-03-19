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

import java.util.HashMap;
import java.util.Map;

import oafext.test.mock.Mocker;
import oafext.test.server.responder.BaseRowResponder;
import oafext.test.server.responder.RowResponder;
import oracle.apps.fnd.framework.server.OAViewRowImpl;
import oracle.jbo.server.ViewRowImpl;

import org.junit.After;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NOTE: Overloaded accessor method for VORowImpl is not supported.
 *
 * @author royce
 */
public class RowMocker implements Mocker<OAViewRowImpl> {


    /** sl4j logger instance. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(RowMocker.class);


    /** Wrapper for the final method getViewObject. */
    public static final String CUSTOM_GET_VO = "getViewObj";

    /** Wrapper for the final method getAttributeCount. */
    public static final String CUST_GET_ATTR_CNT = "getAttrCount";


    /** */
    private final transient OAViewRowImpl mockRow;

    /** */
    private final transient Class<? extends OAViewRowImpl> rowClass;

    /** */
    private final transient Map<String, Object> attrValueMap;

    /** */
    private final BaseViewObjectMocker voMocker;

    /** */
    private final transient RowResponder<ViewRowImpl> responder;

    /**
     * @param pRowClass row class.
     * @param amFixture application module fixture.
     * @param pVoMocker view object mocker.
     */
    public RowMocker(final Class<? extends OAViewRowImpl> pRowClass,
            final AppModuleFixture<?> amFixture,
            final BaseViewObjectMocker pVoMocker) {

        this.rowClass = pRowClass;
        this.mockRow = Mockito.mock(pRowClass);
        this.voMocker = pVoMocker;

        this.responder = new BaseRowResponder();

        this.attrValueMap = new HashMap<String, Object>();

        getResponder().mockMethods(amFixture, this, pRowClass);

    }

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
    public BaseViewObjectMocker getVoMocker()
    {
        return this.voMocker;
    }

    @Override
    public OAViewRowImpl getMock()
    {
        return this.mockRow;
    }

    /**
     * @return the responder
     */
    public RowResponder<ViewRowImpl> getResponder()
    {
        return this.responder;
    }

    /**
     * @return the rowClass
     */
    public Class<? extends OAViewRowImpl> getRowClass()
    {
        return this.rowClass;
    }


}
