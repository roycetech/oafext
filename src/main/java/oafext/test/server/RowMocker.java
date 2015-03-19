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
public class RowMocker implements Mocker<ViewRowImpl> {


    /** sl4j logger instance. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(RowMocker.class);


    /** Wrapper for the final method getViewObject. */
    public static final String CUSTOM_GET_VO = "getViewObj";


    /** */
    private final transient ViewRowImpl mockRow;

    /** */
    private final transient Map<String, Object> attrValueMap;

    /** */
    private final BaseViewObjectMocker voMocker;

    /** */
    private final transient RowResponder<ViewRowImpl> responder = new BaseRowResponder();


    /**
     * @param mockVo
     * @param rowClass
     * @param amFixture
     */
    public RowMocker(final Class<? extends ViewRowImpl> pRowClass,
            final AppModuleFixture<?> amFixture,
            final BaseViewObjectMocker pVoMocker) {

        this.mockRow = Mockito.mock(pRowClass);
        this.voMocker = pVoMocker;

        this.attrValueMap = new HashMap<String, Object>();

        getResponder().mockMethods(amFixture, this, pRowClass);

        //        /* remove(). */
        //        RowResponder.mockRemove(this.mockRow, voMocker, this).remove();
        //
        //        /* getViewObj - anti zombie/anti final. */
        //        RowResponder.mockGetViewObj(this.mockRow, mockVo);
        //        //
        //        /* getAttribute(int) */
        //        RowResponder
        //            .mockGetAttributeInt(this.mockRow, attrList, this)
        //            .getAttribute(Matchers.anyInt());
        //
        //        /* getAttribute(String) */
        //        RowResponder.mockGetAttributeString(this.mockRow, this).getAttribute(
        //            Matchers.anyString());
        //
        //        /* getKey() */
        //        RowResponder.mockGetKey(this.mockRow).getKey();
        //
        //        /* setAttribute(int) */
        //        RowResponder
        //            .mockSetAttributeInt(this.mockRow, attrList, this)
        //            .setAttribute(Matchers.anyInt(), Matchers.any());
        //
        //        /* setAttribute(String) */
        //        RowResponder.mockSetAttributeString(this.mockRow, this).setAttribute(
        //            Matchers.anyString(),
        //            Matchers.any());
        //
        //        /* set*(Object) */
        //        RowResponder.mockSetter(this.mockRow, rowClass, attrList, this);
        //
        //        /* get*() */
        //        RowResponder.mockGetter(this.mockRow, attrList, this);

        //LOGGER.info("Elapse: " + (baseline - System.currentTimeMillis()));
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
    public ViewRowImpl getMock()
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


}
