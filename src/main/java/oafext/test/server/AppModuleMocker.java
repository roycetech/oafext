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

import oafext.test.mock.MockRowCallback;
import oafext.test.mock.Mocker;
import oafext.test.server.responder.AppModuleResponder;
import oafext.test.server.responder.ViewObjectDefaultResponder;
import oafext.test.util.ReflectUtil;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.apps.fnd.framework.server.OADBTransaction;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.domain.Number;

import org.junit.After;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author royce
 *
 * @param <A>
 */
public class AppModuleMocker<A extends OAApplicationModuleImpl> implements
        Mocker<A> {


    /** */
    private final transient A mockAm;

    /** */
    private final transient Map<String, BaseViewObjectMocker> voInstMockerMap;

    /** View Object Definition Full Name to List of Attributes. */
    private final transient Map<String, List<AttrDefMocker>> attrDefFullMockerMap = new HashMap<String, List<AttrDefMocker>>();


    /** */
    private final transient ReflectUtil helper = new ReflectUtil();


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
                throw new OafExtException(e);
            } catch (final IllegalAccessException e) {
                throw new OafExtException(e);
            }

        } else {
            this.mockAm = Mockito.mock(appModuleClass);
        }

        this.voInstMockerMap = new HashMap<String, BaseViewObjectMocker>();

        /* findViewObject */
        new AppModuleResponder<A>()
            .mockFindViewObject(this.mockAm, this)
            .findViewObject(Matchers.anyString());

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
    void mockViewObjectSingle(final AppModuleFixture<?> amFixture,
                              final String voInstance,
                              final MockRowCallback rowMockCallback)
    {
        final BaseViewObjectMocker voMocker = new BaseViewObjectMocker(
            amFixture,
            voInstance,
            BaseViewObjectMocker.ViewObjectType.Single,
            new ViewObjectDefaultResponder());
        voMocker.setRowMockCallback(rowMockCallback);
        mockViewObjectInternal(voMocker);
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
        mockViewObjectSingle(amFixture, voInstance, null);
    }

    /**
     * e.g. Mock getFinFacilityVO1.
     *
     * @param voInstance
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void mockViewObjectHGrid(final AppModuleFixture<?> amFixture,
                             final String voInstance, final int attrIdxParent,
                             final int attrIdxChildren,
                             final MockRowCallback rowMockCallback)
    {
        final ViewObjectHGridMocker voMocker = new ViewObjectHGridMocker(
            amFixture,
            voInstance,
            attrIdxParent,
            attrIdxChildren);
        voMocker.setRowMockCallback(rowMockCallback);
        mockViewObjectInternal(voMocker);
    }

    /**
     * e.g. Mock getFinFacilityVO1.
     *
     * @param voInstance
     */
    void mockViewObjectHGrid(final AppModuleFixture<?> amFixture,
                             final String voInstance, final int attrIdxParent,
                             final int attrIdxChildren)
    {
        mockViewObjectHGrid(
            amFixture,
            voInstance,
            attrIdxParent,
            attrIdxChildren,
            null);
    }


    /**
     * e.g. Mock getFinFacilityVO1.
     *
     * @param voInstance
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void mockViewObjectInternal(final BaseViewObjectMocker voMocker)
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
            .when(ReflectUtil.invokeMethod(this.mockAm, methName))
            .thenReturn(voMocker.getMock());

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
        assert this.voInstMockerMap.get(voInstance) != null : "Invoke one of mockViewObject* before invoking this method.";

        final BaseViewObjectMocker voMocker = this.voInstMockerMap
            .get(voInstance);


        final ViewObject viewObject = voMocker.getMock();
        assert viewObject != null;

        final boolean isExisting = index < viewObject.getAllRowsInRange().length;
        Row row;
        if (isExisting) {
            row = viewObject.getRowAtRangeIndex(index);
        } else {
            row = viewObject.createRow();
        }

        for (int i = 0; i < pValues.length; i++) {
            row.setAttribute(pAttrs[i], pValues[i]);
        }

        if (voMocker.isHGrid()) {
            final ViewObjectHGridMocker voHgridMocker = (ViewObjectHGridMocker) voMocker;
            final Number parentId = (Number) row.getAttribute(voHgridMocker
                .getParentAttrIdx());
            voHgridMocker.registerChild(parentId, row);
        }
        viewObject.insertRowAtRangeIndex(index, row);
    }

    /**
     *
     */
    public void setAllViewObjectExecuted()
    {
        for (final BaseViewObjectMocker voMocker : this.voInstMockerMap
            .values()) {
            final ViewObjectMockState voState = voMocker.getMockedVoState();
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

        final ViewObjectMockState voState = voMocker.getMockedVoState();
        voState.setExecuted(true);
    }

    /**
     * @return the voInstMockerMap
     */
    public Map<String, BaseViewObjectMocker> getVoInstMockerMap()
    {
        return this.voInstMockerMap;
    }

    /**
     * @return the helper
     */
    ReflectUtil getHelper()
    {
        return this.helper;
    }


    /**
     * @return the attrDefMockerLst
     */
    public Map<String, List<AttrDefMocker>> getAttrDefMockerMap()
    {
        return this.attrDefFullMockerMap;
    }


    @Override
    public A getMock()
    {
        return this.mockAm;
    }


}


/**
 * @version $Date: $
 */
class OafExtException extends RuntimeException {

    OafExtException(final Throwable cause) {
        super(cause);
    }

}
