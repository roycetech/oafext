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
import java.util.List;
import java.util.Map;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import org.junit.After;
import org.mockito.Matchers;
import org.mockito.Mockito;


/**
 * NOTE: Overloaded accessor method for VORowImpl is not supported.
 * 
 * @author royce
 */
public class RowMocker {


    /** Wrapper for the final method getViewObject. */
    public static final String CUSTOM_GET_VO = "getViewObj";


    /** */
    private final transient Row mockRow;

    /** */
    private final transient Map<String, Object> attrValueMap;


    RowMocker(final ViewObject mockVo, final Class<? extends Row> rowClass,
            final AppModuleFixture<?> amFixture) {
        this.mockRow = Mockito.mock(rowClass);

        this.attrValueMap = new HashMap<String, Object>();


        final Map<Class<? extends Row>, String> rowClsVoDefMap = amFixture
            .getRowClsVoDefMap();
        final String voDef = rowClsVoDefMap.get(rowClass);
        assert voDef != null;

        final List<String> attrList = amFixture
            .getVoDefAttrListMap()
            .get(voDef);


        /* getViewObj - anti zombie/anti final. */
        RowAnswers.mockGetViewObj(this.mockRow, mockVo);


        /* getAttribute(int) */
        RowAnswers
            .mockGetAttributeInt(this.mockRow, attrList, this)
            .getAttribute(Matchers.anyInt());

        /* getAttribute(String) */
        RowAnswers.mockGetAttributeString(this.mockRow, this).getAttribute(
            Matchers.anyString());

        /* getKey() */
        RowAnswers.mockGetKey(this.mockRow).getAttribute(Matchers.anyString());

        /* setAttribute(int) */
        RowAnswers
            .mockSetAttributeInt(this.mockRow, attrList, this)
            .setAttribute(Matchers.anyInt(), Matchers.any());

        /* setAttribute(String) */
        RowAnswers
            .mockSetAttributeInt(this.mockRow, attrList, this)
            .setAttribute(Matchers.anyInt(), Matchers.any());

        /* set*(Object) */
        RowAnswers.mockSetter(this.mockRow, rowClass, attrList, this);

        /* get*() */
        RowAnswers.mockGetter(this.mockRow, attrList, this);

    }

    @After
    void tearDown()
    {
        Mockito.reset(this.mockRow);
    }


    /**
     * @return the attrValueMap
     */
    Map<String, Object> getAttrValueMap()
    {
        return this.attrValueMap;
    }

    /**
     * @return the mockRow
     */
    Row getMockRow()
    {
        return this.mockRow;
    }


}
