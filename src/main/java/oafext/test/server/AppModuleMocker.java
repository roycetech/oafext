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

import oafext.test.util.MockHelper;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import org.junit.After;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author royce
 * 
 */
public class AppModuleMocker {


    /** */
    private final transient OAApplicationModuleImpl mockAm;

    /** */
    private final transient Map<String, ViewObjectMocker> voInstMockerMap;

    /** */
    private final transient Map<String, List<AttrDefMocker>> attrDefMockerMap = new HashMap<String, List<AttrDefMocker>>();


    /** */
    private final transient MockHelper helper = new MockHelper();


    /** */
    AppModuleMocker(
            final Class<? extends OAApplicationModuleImpl> appModuleClass) {

        this.mockAm = Mockito.mock(appModuleClass);
        this.voInstMockerMap = new HashMap<String, ViewObjectMocker>();

        /* findViewObject */
        AppModuleAnswers.mockFindViewObject(this.mockAm, this).findViewObject(
            Matchers.anyString());
    }


    @After
    void tearDown()
    {
        //Mockito.reset(this.mockAm);  //Don't reset.  App Module mocker is retained between calls.
        for (final ViewObjectMocker voMocker : this.voInstMockerMap.values()) {
            Mockito.reset(voMocker.getMockVo());
            voMocker.tearDown();
        }
        this.voInstMockerMap.clear();
    }

    /**
     * e.g. Mock getFinFacilityVO1.
     * 
     * @param voInstance
     */
    void mockViewObject(final AppModuleFixture<?> amFixture,
                        final String voInstance)
    {

        final ViewObjectMocker voMocker = new ViewObjectMocker(
            amFixture,
            voInstance);

        this.voInstMockerMap.put(voInstance, voMocker);

        final String methName = "get" + voInstance;
        Mockito
            .when(getHelper().invokeMethod(this.mockAm, methName))
            .thenReturn(voMocker.getMockVo());

    }


    /**
     * @param voInstance view object instance name.
     * @param index row index.
     * @param pAttrs attribute to set.
     * @param pValues values to set.
     */
    public void initRowAtIndex(final String voInstance, final int index,
                               final int[] pAttrs, final Object[] pValues)
    {
        assert this.voInstMockerMap.get(voInstance) != null;

        final ViewObject viewObject = getMockAm().findViewObject(voInstance);
        assert viewObject != null;

        final Row newRow = viewObject.createRow();
        for (int i = 0; i < pValues.length; i++) {
            newRow.setAttribute(pAttrs[i], pValues[i]);
        }
        viewObject.insertRowAtRangeIndex(index, newRow);
    }

    /**
     *
     */
    public void setAllViewObjectExecuted()
    {
        for (final ViewObjectMocker voMocker : this.voInstMockerMap.values()) {
            voMocker.setExecuted(true);
        }

    }

    /**
     * Make calls to ViewObject.isExecuted return true for the given view object
     * instance..
     * 
     * @param voInstance view object instance.
     */
    public void setViewObjectExecuted(final String voInstance)
    {
        final ViewObjectMocker voMocker = this.voInstMockerMap.get(voInstance);
        assert voMocker != null : "Invoke mockViewObject(String) before calling this.";

        voMocker.setExecuted(true);
    }

    /**
     * @return the voInstMockerMap
     */
    Map<String, ViewObjectMocker> getVoInstMockerMap()
    {
        return this.voInstMockerMap;
    }

    /**
     * @return the mockAm
     */
    OAApplicationModuleImpl getMockAm()
    {
        return this.mockAm;
    }

    /**
     * @return the helper
     */
    MockHelper getHelper()
    {
        return this.helper;
    }


    /**
     * @return the attrDefMockerLst
     */
    Map<String, List<AttrDefMocker>> getAttrDefMockerMap()
    {
        return this.attrDefMockerMap;
    }


}
