/**
 *   Copyright 2015 Royce Remulla
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
package oafext.test.webui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oafext.lang.ObjectUtil;
import oafext.lang.Return;
import oafext.logging.OafLogger;
import oafext.test.mock.Mocker;
import oafext.test.webui.responder.PageLayoutBeanResponder;
import oafext.test.webui.responder.WebBeanResponder;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.apps.fnd.framework.webui.beans.layout.OAPageLayoutBean;

import org.junit.After;
import org.mockito.Mockito;

/**
 * <ul>
 * <li>Root is top level web bean mocker. Top level of MDS.
 * <li>Top is top level web bean mocker. Top level of an MDS. Top level
 * references member mockers. Can have children mockers as well which are also
 * members.
 * </ul>
 *
 * <ul>
 * <li>Two approach, recursive Web Bean or,
 * <li>MDS (root type) Web Bean containing Map of Web Bean Mockers.
 * </ul>
 *
 * Will try recursive first.
 *
 * @author $Author: $
 * @version $Date: $
 *
 */
public class WebBeanMocker<W extends OAWebBean> implements Mocker<W> {


    /** */
    private static final OafLogger LOGGER = OafLogger.getInstance();

    /** */
    private final transient W mock;

    //    /** */
    //    private transient Element element;

    /** */
    private final transient String webBeanId;
    /** */
    transient boolean idxChildPrepared;

    /** Used for locating corresponding mocker when working with web beans. */
    private transient Map<OAWebBean, WebBeanMocker<? extends OAWebBean>> globBeanMockerMap;


    /** Web Bean Attributes. Transient */
    private final transient Map<Object, Object> attrTransientMap =
            new HashMap<>();

    /** Web Bean Attributes. Element Based. */
    private final transient Map<Object, Object> attrStaticMap = new HashMap<>();


    /**
     * Not final because parent can adopt later on, see setPageButton for
     * example.
     */
    transient WebBeanMocker<? extends OAWebBean> parent;

    /** These are mockers under the same fixture. */
    final transient Map<String, WebBeanMocker<? extends OAWebBean>> memberMockers =
            new HashMap<>();


    /** These are mockers reference from another fixture. */
    private final transient List<WebBeanMocker<? extends OAWebBean>> memberTopMockers =
            new ArrayList<>();


    /** Indexed child Mockers. The will also exist under the member mockers. */
    final transient List<WebBeanMocker<? extends OAWebBean>> idxChildMockers =
            new ArrayList<>();


    /** Identifies this mocker as the root Web Bean mocker. */
    private final transient boolean rootLevel;

    /** Identifies this mocker as the root Web Bean mocker. */
    private final transient Class<W> webBeanClass;


    /** MdsFixture used to lazy load indexed children of the mocked web bean. */
    private final transient MdsFixture mdsFixture;

    /** */
    static final Map<Class<? extends OAWebBean>, WebBeanResponder<?>> BEAN_RESP_MAP =
            new HashMap<>();
    static {
        BEAN_RESP_MAP
            .put(OAPageLayoutBean.class, new PageLayoutBeanResponder());
        BEAN_RESP_MAP.put(OAWebBean.class, new WebBeanResponder<OAWebBean>());
    }

    /**
     * Instantiate a top level(PG or RN) WebBeanMocker.
     *
     * @param mdsFixture PG fixture where the web bean belongs.
     * @param pWebBeanClass type of web bean.
     */
    WebBeanMocker(final MdsFixture mdsFixture, final Class<W> pWebBeanClass) {
        this(mdsFixture, null, pWebBeanClass, true, null);
    }

    /**
     *
     * @param pTopLevel true of top level, otherwise child Web Bean Mocker.
     */

    /**
     * Instantiate a WebBeanMocker.
     *
     * @param pMdsFixture MDS fixture instance.
     * @param pWebBeanId bean ID of this mocker.
     * @param pWebBeanClass type of web bean.
     * @param pRootLevel flag to indicate this is Root mocker.
     * @param pTopMocker null if top level mocker, else the top mocker.
     */
    @SuppressWarnings({
            "unchecked",
            "PMD.NullAssignment" })
    WebBeanMocker(final MdsFixture pMdsFixture, final String pWebBeanId,
            final Class<W> pWebBeanClass, final boolean pRootLevel,
            final WebBeanMocker<? extends OAWebBean> pParent) {

        this.rootLevel = pRootLevel;

        this.parent = pParent;
        if (this.rootLevel) {
            this.globBeanMockerMap = new HashMap<>();
        }

        this.mdsFixture = pMdsFixture;
        this.mock = Mockito.mock(pWebBeanClass);
        this.webBeanId = pWebBeanId;
        this.webBeanClass = pWebBeanClass;

        WebBeanResponder<W> responder;
        if (BEAN_RESP_MAP.get(this.webBeanClass) == null) {
            responder =
                    (WebBeanResponder<W>) BEAN_RESP_MAP
                        .values()
                        .iterator()
                        .next();
        } else {
            responder =
                    (WebBeanResponder<W>) BEAN_RESP_MAP.get(this.webBeanClass);
        }
        responder.mockMethods(this.mdsFixture, this);
    }

    @After
    public void tearDown()
    {
        this.attrTransientMap.clear();
    }

    @Override
    public W getMock()
    {
        return this.mock;
    }

    /**
     * @return the topLevel
     */
    public boolean isTopLevel()
    {
        return getMdsFixture() == null ? false : getMdsFixture()
            .getTopWbMocker() == this;
    }


    /** Add a child WebBeanMocker. */
    public void addMember(//final String pWebBeanId,
    final WebBeanMocker<? extends OAWebBean> childMocker)
    {
        childMocker.setParent(this);
        this.memberMockers.put(childMocker.getWebBeanId(), childMocker);
        registerMocker(childMocker);
    }

    /** Add a top external child WebBeanMocker. */
    public void addTopExtMember(final WebBeanMocker<? extends OAWebBean> childMocker)
    {
        addMember(childMocker);
        this.memberTopMockers.add(childMocker);
    }


    /**
     * Add a child WebBeanMocker.
     */
    public void addIndexedChild(final int index, final OAWebBean child)
    {
        final WebBeanMocker<? extends OAWebBean> childMocker =
                findGlobBeanMockerMap().get(child);
        assert childMocker != null : "Child mocker was not found in GLOBAL registry. ";

        this.idxChildMockers.add(index, childMocker);
        this.addMember(childMocker);
    }

    /**
     * @return the childMockers
     */
    public WebBeanMocker<? extends OAWebBean> getMemberMocker(final String webBeanId)
    {
        return this.memberMockers.get(webBeanId);
    }

    public WebBeanMocker<? extends OAWebBean> findMockerRecursive(final String webBeanId)
    {
        final Return<WebBeanMocker<? extends OAWebBean>> retval =
                new Return<>();
        if (this.memberMockers.get(webBeanId) == null) {

            for (final WebBeanMocker<? extends OAWebBean> webBeanMocker : this.memberTopMockers) {
                retval.set(webBeanMocker.findMockerRecursive(webBeanId));
            }

        } else {
            retval.set(this.memberMockers.get(webBeanId));
        }
        return retval.get();
    }

    /**
     * Need to prepare Indexed children before insertion. Check to do single
     * preparation only
     */
    public void prepareIdxChildren()
    {
        if (!this.idxChildPrepared) {

            OafLogger.getInstance().debug(this.getWebBeanId());
            final DOMManager domManager = getMdsFixture().getDomManager();

            @SuppressWarnings("PMD.NullAssignment" /* PMD Bug.*/)
            final String[] childrenIDs =
                    domManager.getChildrenIDS(isTopLevel() ? null
                            : getWebBeanId());

            for (int i = 0; i < childrenIDs.length; i++) {
                final WebBeanMocker<? extends OAWebBean> childBeanMocker =
                        getMdsFixture().mockWebBean(childrenIDs[i]);
                addIndexedChild(i, childBeanMocker.getMock());
                registerMocker(childBeanMocker);
            }
            this.idxChildPrepared = true;
        }
    }

    /**
     * @return the webBeanId
     */
    public String getWebBeanId()
    {
        return this.webBeanId;
    }

    /**
     * @return the mdsFixture
     */
    MdsFixture getMdsFixture()
    {
        return this.mdsFixture;
    }

    @SuppressWarnings("unchecked")
    public WebBeanMocker<OAPageLayoutBean> getRootMocker()
    {
        final Return<WebBeanMocker<OAPageLayoutBean>> retval = new Return<>();
        if (this.rootLevel) {
            retval.set((WebBeanMocker<OAPageLayoutBean>) this);
        } else {
            retval.set(this.parent.getRootMocker());
        }
        return retval.get();
    }

    /**
     * Register the mocker to the global mocker map.
     *
     * @param pMocker mocker to register.
     */
    public void registerMocker(final WebBeanMocker<? extends OAWebBean> pMocker)
    {
        //        LOGGER.info(pMocker.getWebBeanId());
        //
        final Map<OAWebBean, WebBeanMocker<? extends OAWebBean>> globBnMockerMap =
                findGlobBeanMockerMap();
        assert globBnMockerMap != null;

        globBnMockerMap.put(pMocker.getMock(), pMocker);
    }

    @SuppressWarnings("unchecked")
    public final <T extends OAWebBean> WebBeanMocker<T> getMocker(final T mockWebBean)
    {
        return (WebBeanMocker<T>) findGlobBeanMockerMap().get(mockWebBean);
    }

    /** Locate the global mocker map. */
    public Map<OAWebBean, WebBeanMocker<? extends OAWebBean>> findGlobBeanMockerMap()
    {
        final Return<Map<OAWebBean, WebBeanMocker<? extends OAWebBean>>> retval =
                new Return<>();
        if (this.rootLevel) {
            retval.set(this.globBeanMockerMap);
        } else if (isTopLevel()) {
            retval.set(this.parent.findGlobBeanMockerMap());
        } else {
            retval
                .set(getMdsFixture().getTopWbMocker().findGlobBeanMockerMap());
        }
        assert retval.get() != null;
        return retval.get();
    }

    /**
     * @return the parent
     */
    WebBeanMocker<? extends OAWebBean> getParent()
    {
        return this.parent;
    }

    /**
     * @param parent the parent to set
     */
    void setParent(final WebBeanMocker<? extends OAWebBean> parent)
    {
        this.parent = parent;
    }

    /**
     * @return the webBeanClass
     */
    public Class<W> getWebBeanClass()
    {
        return this.webBeanClass;
    }

    /** @return the attrTransientMap */
    void setStaticAttribute(final Object key, final Object value)
    {
        this.attrStaticMap.put(key, value);
    }

    /** @return the attrTransientMap */
    public void setTransientAttribute(final Object key, final Object value)
    {
        this.attrTransientMap.put(key, value);
    }

    /**
     * @return the attrStaticMap
     */
    public Object getAttrValue(final Object key)
    {
        return ObjectUtil.nvl(
            this.attrTransientMap.get(key),
            this.attrStaticMap.get(key));
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return WebBeanMockerHelper.toString(this);
    }

}
