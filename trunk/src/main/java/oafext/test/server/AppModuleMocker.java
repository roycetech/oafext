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

import oafext.test.util.MockHelper;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.apps.fnd.framework.server.OADBTransaction;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import org.junit.After;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author royce
 *
 */
public class AppModuleMocker<A extends OAApplicationModuleImpl> {


    /** */
    private final transient A mockAm;

    /** */
    private final transient Map<String, BaseViewObjectMocker> voInstMockerMap;

    /** View Object Definition Full Name to List of Attributes. */
    private final transient Map<String, List<AttrDefMocker>> attrDefFullMockerMap = new HashMap<String, List<AttrDefMocker>>();


    /** */
    private final transient MockHelper helper = new MockHelper();


    /** sl4j logger instance. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(AppModuleMocker.class);


    /** */
    AppModuleMocker(final Class<A> appModuleClass) {
        this(appModuleClass, false);
    }


    /**
     * @param appModuleClass application module class.
     * @param spyAm set to true if you need to spy the AM. This is not
     *            recommended due to performance and design limitation when
     *            using a single AM for model approach.
     */
    AppModuleMocker(final Class<A> appModuleClass, final boolean spyAm) {

        assert appModuleClass != null;

        if (spyAm) {
            try {
                final A appModule = appModuleClass.newInstance();
                this.mockAm = Mockito.spy(appModule);
            } catch (final InstantiationException e) {
                throw new OafExtException(e.getMessage());
            } catch (final IllegalAccessException e) {
                throw new OafExtException(e.getMessage());
            }

        } else {
            this.mockAm = Mockito.mock(appModuleClass);
        }

        this.voInstMockerMap = new HashMap<String, BaseViewObjectMocker>();

        /* findViewObject */
        AppModuleAnswers.mockFindViewObject(this.mockAm, this).findViewObject(
            Matchers.anyString());

        /* getOADBTransaction */
        final OADBTransaction mockTrx = Mockito.mock(OADBTransaction.class);
        Mockito.doReturn(mockTrx).when(this.mockAm).getOADBTransaction();

    }


    @After
    void tearDown()
    {
        //Mockito.reset(this.mockAm);  //Don't reset.  App Module mocker is retained between calls.
        //        for (final BaseViewObjectMocker voMocker : this.voInstMockerMap
        //            .values()) {
        //            Mockito.reset(voMocker.getMockVo());
        //            voMocker.tearDown();
        //        }
        this.voInstMockerMap.clear();
        //Mockito.reset((Object) mockAm); Doing this will wipe mocks, re-mock may be performance heavy
    }

    /**
     * e.g. Mock getFinFacilityVO1.
     *
     * @param voInstance
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void mockViewObjectSingle(final AppModuleFixture<?> amFixture,
                              final String voInstance)
    {
        final BaseViewObjectMocker voMocker = new BaseViewObjectMocker(
            amFixture,
            voInstance,
            BaseViewObjectMocker.ViewObjectType.Single,
            new ViewObjectDefaultResponder());
        mockViewObject_(voMocker);
    }

    /**
     * e.g. Mock getFinFacilityVO1.
     *
     * @param voInstance
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void mockViewObjectHGrid(final AppModuleFixture<?> amFixture,
                             final String voInstance, final int attrIdxChildren)
    {
        final ViewObjectHGridMocker voMocker = new ViewObjectHGridMocker(
            amFixture,
            voInstance,
            attrIdxChildren);
        mockViewObject_(voMocker);
    }

    /**
     * e.g. Mock getFinFacilityVO1.
     *
     * @param voInstance
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void mockViewObject_(final BaseViewObjectMocker voMocker)
    {
        final AppModuleFixture<?> amFixture = voMocker.getAmFixture();
        final String voInstance = voMocker
            .getMockedVoState()
            .getViewObjectName();

        this.voInstMockerMap.put(voInstance, voMocker);

        final String voDefFull = amFixture.getVoNameDefMap().get(voInstance);
        assert voDefFull != null;

        final List<AttrDefMocker> attrDefMocker = new ArrayList<AttrDefMocker>();
        final List<String> attrList = amFixture.getVoDefAttrListMap().get(
            voDefFull);
        assert attrList != null;
        for (final String string : attrList) {
            attrDefMocker.add(new AttrDefMocker(string));
        }

        this.attrDefFullMockerMap.put(voDefFull, attrDefMocker);


        final String methName = "get" + voInstance;
        Mockito
            .when(getHelper().invokeMethod(this.mockAm, methName))
            .thenReturn(voMocker.getMockVo());

    }


    /**
     * TODO: Optimize, this is slow.
     *
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

        final boolean isExisting = index < viewObject.getAllRowsInRange().length;
        Row row;
        if (isExisting) {
            row = viewObject.getRowAtRangeIndex(index);
        } else {
            row = viewObject.createRow();
            viewObject.insertRowAtRangeIndex(index, row);
        }

        for (int i = 0; i < pValues.length; i++) {
            row.setAttribute(pAttrs[i], pValues[i]);
        }
    }

    /**
     *
     */
    public void setAllViewObjectExecuted()
    {
        for (final BaseViewObjectMocker voMocker : this.voInstMockerMap
            .values()) {
            final ViewObjectMockedState voState = voMocker.getMockedVoState();
            voState.setExecuted(true);
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
        final BaseViewObjectMocker voMocker = this.voInstMockerMap
            .get(voInstance);
        assert voMocker != null : "Invoke mockViewObject(String) before calling this.";

        final ViewObjectMockedState voState = voMocker.getMockedVoState();
        voState.setExecuted(true);
    }

    /**
     * @return the voInstMockerMap
     */
    Map<String, BaseViewObjectMocker> getVoInstMockerMap()
    {
        return this.voInstMockerMap;
    }

    /**
     * @return the mockAm
     */
    A getMockAm()
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
        return this.attrDefFullMockerMap;
    }


}

class OafExtException extends RuntimeException {

    OafExtException(final String message) {
        super(message);
    }

}
