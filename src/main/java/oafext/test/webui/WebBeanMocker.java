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
    private transient boolean idxChildPrepared;

    /** Used for locating corresponding mocker when working with web beans. */
    private transient Map<OAWebBean, WebBeanMocker<? extends OAWebBean>> globBeanMockerMap;


    /** Web Bean Attributes. */
    private final Map<String, Object> attrMap = new HashMap<String, Object>();

    /**
     * Not final because parent can adopt later on, see setPageButton for
     * example.
     */
    private transient WebBeanMocker<? extends OAWebBean> parent;

    //    /** If is top, then return itself. Top mocker in an MDS scope. */
    //    private transient WebBeanMocker<? extends OAWebBean> topMocker;

    /** These are mockers under the same fixture. */
    private final transient Map<String, WebBeanMocker<? extends OAWebBean>> memberMockers =
            new HashMap<>();


    /** These are mockers under the same fixture. */
    private final transient List<WebBeanMocker<? extends OAWebBean>> memberTopMockers =
            new ArrayList<>();


    /** Indexed child Mockers. The will also exist under the member mockers. */
    private final transient List<WebBeanMocker<? extends OAWebBean>> idxChildMockers =
            new ArrayList<>();

    //    /**
    //     * Temporary place holder for created Web Bean. TODO: Move to
    //     * WebBeanFactoryMocker.
    //     */
    //    private final transient Map<String, WebBeanMocker<? extends OAWebBean>> createdWebBeanMap =
    //            new HashMap<>();


    /** Identifies this mocker as the root Web Bean mocker. */
    private final transient boolean rootLevel;


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
     * @param webBeanClass type of web bean.
     */
    WebBeanMocker(final MdsFixture mdsFixture, final Class<W> webBeanClass) {
        this(mdsFixture, null, webBeanClass, true, null);
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
     * @param webBeanClass type of web bean.
     * @param pRootLevel flag to indicate this is Root mocker.
     * @param pTopMocker null if top level mocker, else the top mocker.
     */
    @SuppressWarnings({
            "unchecked",
            "PMD.NullAssignment" })
    WebBeanMocker(final MdsFixture pMdsFixture, final String pWebBeanId,
            final Class<W> webBeanClass, final boolean pRootLevel,
            final WebBeanMocker<? extends OAWebBean> pParent) {

        this.rootLevel = pRootLevel;

        this.parent = pParent;
        if (this.rootLevel) {
            //            this.parent = null;
            this.globBeanMockerMap = new HashMap<>();
            //        } else {
            //            if (pMdsFixture.getTopWbMocker() == this) {
            //                this.parent = pMdsFixture.getParent().getTopWbMocker();
            //            } else {
            //                this.parent = pMdsFixture.getTopWbMocker();
            //            }
        }

        this.mdsFixture = pMdsFixture;
        this.mock = Mockito.mock(webBeanClass);
        this.webBeanId = pWebBeanId;

        WebBeanResponder<W> responder;
        if (BEAN_RESP_MAP.get(webBeanClass) == null) {
            responder =
                    (WebBeanResponder<W>) BEAN_RESP_MAP
                        .values()
                        .iterator()
                        .next();
        } else {
            responder = (WebBeanResponder<W>) BEAN_RESP_MAP.get(webBeanClass);
        }
        responder.mockMethods(this.mdsFixture, this);
    }

    @After
    public void tearDown()
    {
        if (this.globBeanMockerMap != null) {
            this.globBeanMockerMap.clear();
        }
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

    /**
     * @return the attrMap
     */
    public Map<String, Object> getAttrMap()
    {
        return this.attrMap;
    }

    //    /**
    //     * Add a child WebBeanMocker.
    //     */
    //    public void addMember(final String webBeanId,
    //                          final WebBeanMocker<? extends OAWebBean> childMocker)
    //    {
    //        childMocker.setParent(this);
    //        this.memberMockers.put(webBeanId, childMocker);
    //        registerMocker(childMocker);
    //    }

    /**
     * Add a child WebBeanMocker.
     */
    public void addMember(//final String pWebBeanId,
    final WebBeanMocker<? extends OAWebBean> childMocker)
    {
        LOGGER.debug((this.rootLevel ? "Root" : "")
                + childMocker.getWebBeanId());
        childMocker.setParent(this);
        this.memberMockers.put(childMocker.getWebBeanId(), childMocker);
        registerMocker(childMocker);
    }

    /**
     * Add a child WebBeanMocker.
     */
    public void addIndexedChild(final int index, final OAWebBean child)
    {
        LOGGER.debug("Index: " + index + ", ID: " + child.getNodeID());

        final WebBeanMocker<? extends OAWebBean> childMocker =
                findGlobBeanMockerMap().get(child);
        assert childMocker != null : "Child mocker was not found in GLOBAL registry. ";

        this.idxChildMockers.add(index, childMocker);
        this.addMember(childMocker);
        //this.memberMockers.put(child.getNodeID(), childMocker);
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

            for (final String string : childrenIDs) {
                final WebBeanMocker<? extends OAWebBean> childBeanMocker =
                        getMdsFixture().mockWebBean(string);
                //addMember(string, childBeanMocker);
                addMember(childBeanMocker);
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
     * @return the globBeanMockerMap
     */
    public void registerMocker(final WebBeanMocker<? extends OAWebBean> mocker)
    {
        LOGGER
            .debug(getMdsFixture().getMdsPath() + ' ' + mocker.getWebBeanId());

        final Map<OAWebBean, WebBeanMocker<? extends OAWebBean>> globBnMockerMap =
                findGlobBeanMockerMap();
        assert globBnMockerMap != null;

        globBnMockerMap.put(mocker.getMock(), mocker);
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

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        final StringBuilder strBuilder = new StringBuilder();

        String paddedNewline;
        if (isTopLevel()) {
            paddedNewline = "\n";
        } else {
            paddedNewline = "\n" + String.format("%4s", "");
        }

        strBuilder
            .append(getClass().getSimpleName())
            .append(paddedNewline)
            .append("Type: ")
            .append(getMock().getClass().getSimpleName())
            .append(paddedNewline)
            .append("ID: ")
            .append(this.webBeanId)
            .append(paddedNewline)
            .append("MDS: ")
            .append(
                getMdsFixture() == null ? null : getMdsFixture().getMdsPath())
            .append(paddedNewline)
            .append("ICP: ")
            .append(this.idxChildPrepared)
            .append(paddedNewline)
            .append("TOP: ")
            .append(this.isTopLevel())
            .append(paddedNewline)
            .append("Children Size: ")
            .append(this.idxChildMockers.size());

        if (!this.idxChildMockers.isEmpty()) {
            strBuilder.append(this.idxChildMockers);
        }

        if (this.parent != null) {
            strBuilder
                .append(paddedNewline)
                .append("Parent: ")
                .append(
                    this.parent == null ? null : this.parent
                        .getMdsFixture()
                        .getMdsPath());
        }

        strBuilder
            .append(paddedNewline)
            .append("Members Size: ")
            .append(this.memberMockers.size())
            .append(paddedNewline);

        if (!this.memberMockers.isEmpty()) {
            strBuilder.append(this.memberMockers);
        }

        return strBuilder.toString();

    }

}
