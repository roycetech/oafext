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
package oafext.test.webui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.apps.fnd.framework.webui.beans.form.OASubmitButtonBean;
import oracle.apps.fnd.framework.webui.beans.layout.OAPageLayoutBean;
import oracle.apps.fnd.framework.webui.beans.message.OAMessageStyledTextBean;
import oracle.apps.fnd.framework.webui.beans.nav.OAButtonBean;
import oracle.apps.fnd.framework.webui.beans.nav.OALinkBean;
import oracle.apps.fnd.framework.webui.beans.table.OAAddTableRowBean;

import org.junit.Assert;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ph.rye.common.lang.StringUtil;

/**
 *
 * <pre>
 * @author $Author$
 * @version $Date$
 * </pre>
 */
public class MdsFixture2 {


    /** Internal source control version. */
    public static final String RCS_ID = "$Revision$";


    /** sl4j logger instance. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(MdsFixture2.class);


    /** WebBeanID to Element map. */
    private final transient BeanElementMap beanIdElementMap;

    /** WebBeanID to Mock Web Bean map. */
    private final transient BeanMockMap beanIdMockMap;

    /** WebBeanID Children WebBeanID map of List. */
    private final transient BeanChildrenMap beanIdChildrenMap;

    /** WebBeanID to Rendered State map. */
    private final transient BeanShowMap beanIdShowMap;

    /** WebBeanID to Mds path map. */
    private transient Map<String, MdsFixture2> beanIdMdsFixtureMap;

    /** WebBeanID to Editable State map. */
    private final transient BeanLockMap beanIdLockMap;

    /** WebBeanID to required State map. */
    private final transient BeanRequiredMap beanIdRequiredMap;

    /** WebBeanID to Parent WebBeanID map. */
    private final transient BeanParentMap beanIdParentIdMap;

    /** WebBeanID to Parent WebBeanID map. */
    private transient OAPageLayoutBean pageLayout;


    /** Children MDS Fixtures. */
    private final transient MdsFixtureList childFixtures;

    private transient MdsFixture2 rootFixture;


    /** List of ReadOnly web bean types. */
    static final List<Class<?>> READONLY_TYPES = Arrays.asList(new Class<?>[] {
            OAMessageStyledTextBean.class,
            OAButtonBean.class,
            OASubmitButtonBean.class,
            OALinkBean.class,
            OAAddTableRowBean.class });


    /** For read only type beans (e.g. OAMessageStyledTextBean). */
    private final List<String> PERMA_LOCK_IDS = new ArrayList<String>();

    /** Place holder for {@link #addCustomWebBean(String, OAWebBean)}. */
    private final List<String> CUSTOM_BEANS = new ArrayList<String>();


    //    /** Reference parent MdsFixture2. */
    //    private final transient MdsFixture2 parentFixture;


    /** External MDS path to parent WebBeanID Map. */
    private final transient ExtMdsParentIdMap extMdsParentIdMap;

    /** External MDS path to MdsFixture */
    private final transient MdsToFixtureMap extMdsFixtureMap;


    /** MDS path. */
    private final transient String mdsPath;


    /** TODO: Identify purpose of this variable. */
    private transient boolean containerIdSet;


    /** Container ID to Child ID map. */
    private final transient Map<String, String> contIdToChildMap;


    /**
     * Duplicates are okay as long as they are not referenced by
     * findChildRecursive. We cannot predict the outcome so it is flagged as
     * error if attempt to find duplicated Id.
     */
    private final transient DupedBeanMap dupedBeanMap;


    /** */
    private final transient MdsFixtureHelper helper = new MdsFixtureHelper();


    /** @param pMdsPath (e.g. "/xxx/oracle/apps/xx/module/webui/SomePG". */
    public MdsFixture2(final String pMdsPath) {
        this(pMdsPath, null);
    }

    MdsFixture2(final String pMdsPath, final MdsFixture2 parent) {

        assert StringUtil.hasValue(pMdsPath);

        this.mdsPath = pMdsPath;

        this.beanIdElementMap = new BeanElementMap();
        this.beanIdMockMap = new BeanMockMap();
        this.beanIdLockMap = new BeanLockMap();
        this.beanIdShowMap = new BeanShowMap();
        this.beanIdRequiredMap = new BeanRequiredMap();
        this.beanIdParentIdMap = new BeanParentMap();
        this.beanIdChildrenMap = new BeanChildrenMap();
        this.childFixtures = new MdsFixtureList();

        this.dupedBeanMap = new DupedBeanMap();
        this.contIdToChildMap = new HashMap<String, String>();
        this.extMdsParentIdMap = new ExtMdsParentIdMap();
        this.extMdsFixtureMap = new MdsToFixtureMap();

        this.rootFixture = this;


        if (parent == null) {
            this.rootFixture = this;

            processMds(pMdsPath, null);
            processDuplicates();
        } else {

            assert parent.rootFixture != null;
            this.rootFixture = parent.rootFixture;
        }
    }

    /**
     * Internal constructor for extended regions.
     *
     * @param pMdsPath MDS path of the extended region.
     * @param containerId Parent region ID using extended region implementation.
     * @param parent Parent MDS Fixture instance.
     */
    private MdsFixture2(final String pMdsPath, final String containerId,
            final MdsFixture2 parent) {

        this(pMdsPath, parent);
        processMds(pMdsPath, containerId);
    }


    /**
     * Should be invoked on unit test tearDown().
     */
    void tearDown()
    {
        //Not Cleared
        //beanIdElementMap

        //Cleared
        this.beanIdMockMap.clear();
        this.beanIdShowMap.clear();
        this.beanIdLockMap.clear();
        this.beanIdRequiredMap.clear();

        //        this.mdsParentMap.clear();
        //        this.mdsChildrenMap.clear();
        //        this.mdsActionMockersMap.clear();

        this.dupedBeanMap.clear();
        this.contIdToChildMap.clear();
    }

    public void recurseTearDown(final MdsFixture2 fixture)
    {
        fixture.tearDown();
        for (final MdsFixture2 nextFix : fixture.childFixtures) {
            recurseTearDown(nextFix);
        }
    }

    /**
     * @param pMdsPath MDS path.
     * @param extRegionId extended regions web bean ID.
     */
    final void processMds(final String pMdsPath, final String extRegionId)
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final String mdsFilename = pMdsPath + ".xml";
            LOGGER.info("mdsFilename: " + mdsFilename);

            final URL resource = getClass().getResource(mdsFilename);

            final Document document =
                    docBuilder.parse(new File(resource.getFile()));
            final Element root = document.getDocumentElement();

            if (extRegionId != null) {
                this.containerIdSet = false;
            }
            processNode(root, 0, extRegionId);

        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (final ParserConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    /**
     * @param pNode XML node to process.
     * @param level track recursive level.
     * @param extRegionId extended regions web bean ID.
     */
    final void processNode(final Node pNode, final int level,
                           final String extRegionId)
    {
        final NodeList nodes = pNode.getChildNodes();
        if (nodes != null) {

            final List<String> children = new ArrayList<String>();
            for (int i = 0; i < nodes.getLength(); i++) {
                processChildNode(nodes.item(i), extRegionId, children, level);
            }

            //children may be inserted upon inside processChildNode.
            if (!children.isEmpty() && pNode instanceof Element
                    && extRegionId != null) {

                this.beanIdChildrenMap.put(extRegionId, children);
            }
        }
    }

    /**
     * Refactored out of {@link #processNode(Node, int, String)} for
     * simplification.
     *
     * @param childNode Child Node.
     * @param containerId container ID.
     * @param children web bean instances under the container ID.
     * @param level p
     */
    void processChildNode(final Node childNode, final String containerId,
                          final List<String> children, final int level)
    {
        final String nodeName = childNode.getNodeName();
        if (nodeName.startsWith("oa:")) {
            final Element elem = (Element) childNode;
            final String childElemId = elem.getAttribute("id");

            if (containerId == null) {
                //                this.rootFixture.beanIdMdsPathMap
                //                    .put(childElemId, this.mdsPath);

            } else {

                if (!this.containerIdSet) {
                    this.beanIdParentIdMap.put(childElemId, containerId);
                    this.containerIdSet = true;
                }

                this.contIdToChildMap.put(childElemId, containerId);
            }
            children.add(childElemId);
            processOAElement(elem, nodeName, level);
        }
        if (this.containerIdSet) {
            processNode(childNode, level + 2, null);
        } else {
            processNode(childNode, level + 2, containerId);
        }
    }

    /**
     * DUPE_WEBBEAN_IDS initially is populated with List of MDS path in which
     * the ID appears.
     *
     * We will clear the map with IDS that has appear only once.
     */
    public final void processDuplicates()
    {
        final List<String> unique = new ArrayList<String>();
        for (final String webBeanId : this.dupedBeanMap.keySet()) {
            if (this.dupedBeanMap.get(webBeanId).size() == 1) {
                unique.add(webBeanId);
            }
        }
        for (final String string : unique) {
            this.dupedBeanMap.remove(string);
        }
    }

    /**
     * @param elem Element instance.
     * @param nodeName Node name.
     * @param level Recursion level.
     */
    final void processOAElement(final Element elem, final String nodeName,
                                final int level)
    {
        //        LOGGER.info("*** " + nodeName + " -> "
        //                + elem.getParentNode().getParentNode().getNodeName());

        final String webBeanId = elem.getAttribute("id");


        final String parentWebBeanId =
                ((Element) elem.getParentNode().getParentNode())
                    .getAttribute("id");

        //        LOGGER.info(webBeanId + " -> " + parentWebBeanId);
        if ("".equals(parentWebBeanId)) {

            final String mdsParentId = this.extMdsParentIdMap.get(this.mdsPath);
            if (mdsParentId != null) {
                registerChild(mdsParentId, webBeanId);
            }
        } else {

            this.beanIdParentIdMap.put(webBeanId, parentWebBeanId);
            registerChild(parentWebBeanId, webBeanId);

        }

        final Document doc = elem.getOwnerDocument();
        final String docPath = doc.getDocumentURI();

        final String mdsName = docPath.substring(docPath.lastIndexOf('/') + 1);


        if (this.dupedBeanMap.get(webBeanId) == null) {
            this.dupedBeanMap.put(webBeanId, new ArrayList<String>());
        }
        final List<String> mdsList = this.dupedBeanMap.get(webBeanId);
        mdsList.add(mdsName);

        this.beanIdElementMap.put(webBeanId, elem);
        final StringBuilder output = new StringBuilder(30);
        //LOGGER.info(getStrUtil().space(level) + nodeName + " - " + webBeanId);

        if ("oa:pageLayout".equals(nodeName)) {
            this.pageLayout =
                    (OAPageLayoutBean) mockBean(webBeanId, this.mdsPath);
        }

        output
            .append(String.format("%2s", ""))
            .append(nodeName)
            .append(" - ")
            .append(webBeanId);

        final String attrShow = elem.getAttribute("rendered");
        final String attrReadOnly = elem.getAttribute("readOnly");
        output.append(", show=" + attrShow + ", locked=" + attrReadOnly);
        final String attrExtends = elem.getAttribute("extends");

        if (StringUtil.hasValue(attrExtends)) {

            if (getHelper().getPackage(attrExtends) != null
                    && getHelper().getPackage(attrExtends).equals(
                        getHelper().getPackage(this.mdsPath))) {

                output.append(" - ").append(attrExtends).append('\n');
                final String extElementId = elem.getAttribute("id");

                this.extMdsParentIdMap.put(attrExtends, extElementId);
                this.childFixtures.add(new MdsFixture2(
                    attrExtends,
                    extElementId,
                    this));
            } else {
                output.append(" - external");
            }
        }
        LOGGER.info(output.toString()); //NOTE: important for checking MDS content.
    }

    /**
     * Register a child bean ID to a parent ID.
     *
     * @param parentBeanId parent web bean id.
     * @param childBeanId web bean id to add to children list of web bean id to
     *            parent.
     */
    void registerChild(final String parentBeanId, final String childBeanId)
    {

        if (this.beanIdChildrenMap.get(parentBeanId) == null) {
            this.beanIdChildrenMap.put(parentBeanId, new ArrayList<String>());
        }
        final List<String> childWebBeans =
                this.beanIdChildrenMap.get(parentBeanId);
        childWebBeans.add(childBeanId);
    }


    /**
     * Gets the MDS path of the given web bean id. MDS path can be the PG or an
     * external region reference by the PG.
     *
     * @param fixture MdsFixture instance.
     * @param webBeanId web bean ID.
     */
    String getWebBeanMdsPath(final MdsFixture2 fixture, final String webBeanId)
    {
        String retval = null; //NOPMD: null default, conditionally redefine.
        Element element = this.beanIdElementMap.get(webBeanId);
        if (element == null) {

            for (final MdsFixture2 nextFixture : fixture.childFixtures) {
                element = recurseFindElement(nextFixture, webBeanId);
                if (element != null) {
                    retval = nextFixture.mdsPath;
                    break;
                }
            }
        } else {
            retval = fixture.mdsPath;
            //getLogger().debug(webBeanId + " found in " + mdsPath);
        }
        return retval;
    }

    /**
     *
     * @param mdsFixture MDS fixture.
     * @param webBeanId web bean ID to find.
     */
    Element recurseFindElement(final MdsFixture2 mdsFixture,
                               final String webBeanId)
    {
        Element retval = mdsFixture.beanIdElementMap.get(webBeanId);
        if (retval == null) {
            for (final MdsFixture2 nextFixture : mdsFixture.childFixtures) {
                retval = recurseFindElement(nextFixture, webBeanId);
                if (retval != null) {
                    break;
                }
            }
        }
        return retval;
    }

    /**
     * Mock implementation for {@link OAWebBean#findChildRecursive(String)}.
     *
     * @param webBeanId Web Bean ID.
     * @return mocked state of the given webBeanId.
     */
    public OAWebBean findChildRecursive(final String webBeanId)
    {
        OAWebBean mockBean = null; //NOPMD: Default to null, will be redefined for certain conditions.
        if (this.dupedBeanMap.containsKey(webBeanId)) {
            Assert.fail("Reference to duplicate ID [" + webBeanId + " - "
                    + this.dupedBeanMap.get(webBeanId) + "] found.");
        } else {
            if (recurseFindElement(this, webBeanId) == null) {

                LOGGER.warn("WebBeanId [" + webBeanId + "]  was not found in "
                        + this.mdsPath);
                //                Assert.fail("WebBeanId [" + webBeanId + "]  was not found in "
                //                        + this.mdsPath);
            } else {
                final String webBeanMdsPath =
                        getWebBeanMdsPath(this, webBeanId);
                if (webBeanMdsPath == null) {

                    /**
                     * Custom RuntimeException subclass thrown when MDS path is
                     * not found.
                     */
                    class MissingMdsPathException extends RuntimeException {

                        /**
                         * Default constructor.
                         *
                         * @param string Exception string.
                         */
                        MissingMdsPathException(final String string) {
                            super(string);
                        }
                    }
                    throw new MissingMdsPathException("MdsPath for: "
                            + webBeanId + " was not found. ");
                }

                mockBean = this.beanIdMockMap.get(webBeanId);
                if (mockBean == null) {
                    mockBean = mockBean(webBeanId, webBeanMdsPath);
                }

            }
        }
        return mockBean;
    }

    /**
     * @param webBeanId
     * @param webBeanMdsPath
     * @return
     */
    OAWebBean mockBean(final String webBeanId, final String webBeanMdsPath)
    {
        //LOGGER.info("Mocking: " + webBeanId);

        final MdsFixture2 fixture = recurseFindMdsFixture(this, webBeanId);
        assert fixture != null;

        OAWebBean mockBean;
        final String elemName =
                fixture.beanIdElementMap.get(webBeanId).getNodeName();
        final String oaWebBeanType = buildOaWebBeanType(elemName);
        final Class<? extends OAWebBean> oaClass =
                OABeanUtil.getOABeanClass(oaWebBeanType);

        if (READONLY_TYPES.contains(oaClass)) {
            this.beanIdLockMap.put(webBeanId, true);
            this.PERMA_LOCK_IDS.add(webBeanId);
        }

        if (Modifier.isFinal(oaClass.getModifiers())) {
            mockBean = Mockito.mock(OAWebBean.class);
        } else {
            mockBean = Mockito.mock(oaClass);
        }

        this.beanIdMockMap.put(webBeanId, mockBean);
        mockWebBeanBehavior(mockBean, webBeanId, webBeanMdsPath);
        return mockBean;
    }

    /**
     * Will mock behavior of web bean {@link OAWebBean#setRendered(boolean)} and
     * {@link OAWebBean#isRendered()}.
     *
     * @param mockBean Mocked web bean instance.
     * @param webBeanId Web Bean ID.
     * @param webBeanMdsPath Web Bean MDS path.
     */
    void mockWebBeanBehavior(final OAWebBean mockBean, final String webBeanId,
                             final String webBeanMdsPath)
    {
        Mockito.when(mockBean.getID()).thenReturn(webBeanId);
        Mockito.doAnswer(new Answer<Object>() {

            private String mdsPath;

            Answer<Object> setMdsPath(final String pMdsPath)
            {
                this.mdsPath = pMdsPath;
                return this;
            }


            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final OAWebBean webBean = (OAWebBean) invocation.getMock();
                final boolean doRender = (Boolean) invocation.getArguments()[0];
                MdsFixture2.this.beanIdShowMap.put(webBean.getID(), doRender);
                return null;
            }

        }.setMdsPath(webBeanMdsPath))
            .when(mockBean)
            .setRendered(Matchers.anyBoolean());

        Mockito.when(mockBean.isRendered()).thenAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final OAWebBean webBean = (OAWebBean) invocation.getMock();
                return getAbsoluteRenderedState(webBean.getID());
            }
        });
    }

    /**
     * Mock implementation of web bean render state.
     *
     * @param webBeanId Web Bean ID.
     * @return True of webBeanId is set to rendered along with all its parents.
     */
    private boolean getAbsoluteRenderedState(final String webBeanId)
    {
        boolean retval;
        if (this.CUSTOM_BEANS.contains(webBeanId)) {
            retval = getRenderedState(webBeanId);
        } else {
            if (this.dupedBeanMap.containsKey(webBeanId)) {
                throw new UnsupportedOperationException(
                    "Duplicate ID detected for: " + webBeanId);
            }
            retval = getRenderedState(webBeanId);
            String curreWebBeanId = this.beanIdParentIdMap.get(webBeanId);

            //            if (curreWebBeanId == null
            //                    && CONTAINER_ID_MAP.get(webBeanId) != null) {
            //                curreWebBeanId = CONTAINER_ID_MAP.get(webBeanId);
            //            }

            while (retval && StringUtil.hasValue(curreWebBeanId)) {
                retval = getRenderedState(curreWebBeanId);
                curreWebBeanId = this.beanIdParentIdMap.get(curreWebBeanId);
            }
        }
        return retval;
    }

    /**
     * @param webBeanId Web Bean ID.
     *
     * @return true of webBeanId is set to rendered.
     */
    private boolean getRenderedState(final String webBeanId)
    {

        final MdsFixture2 fixture = recurseFindMdsFixture(this, webBeanId);
        assert fixture != null : "Web Bean ID: " + webBeanId;

        final Map<String, Boolean> wbRenderState;
        if (this.CUSTOM_BEANS.contains(webBeanId)) {
            if (this.beanIdShowMap.get(webBeanId) == null) {
                this.beanIdShowMap.put(webBeanId, true);
            }
        } else {

            if (fixture.beanIdShowMap.get(webBeanId) == null) {

                final Element elem = fixture.beanIdElementMap.get(webBeanId);
                final String attrRendered = elem.getAttribute("rendered");

                if (attrRendered == null || "".equals(attrRendered)
                        || "true".equalsIgnoreCase(attrRendered)) {


                    fixture.beanIdShowMap.put(webBeanId, true);

                    //                } else if (attrRendered != null
                    //                        && attrRendered.startsWith("${oa.")) {
                    //
                    //                    if (MdsFixture.amFixture == null) {
                    //                        wbRenderState.put(webBeanId, true);
                    //
                    //                    } else {
                    //                        final boolean spelResult = processSpel(attrRendered);
                    //                        wbRenderState.put(webBeanId, spelResult);
                    //
                    //                    }
                } else {
                    LOGGER.warn("Default Render false for [" + webBeanId + "]");
                    fixture.beanIdShowMap.put(webBeanId, false);
                }
            }
        }
        return fixture.beanIdShowMap.get(webBeanId);
    }

    /**
     *
     * @param mdsFixture MDS fixture.
     * @param webBeanId web bean ID to find.
     */
    MdsFixture2 recurseFindMdsFixture(final MdsFixture2 mdsFixture,
                                      final String webBeanId)
    {
        MdsFixture2 retval = null;

        if (mdsFixture.beanIdElementMap.keySet().contains(webBeanId)) {
            retval = mdsFixture;
        } else {
            for (final MdsFixture2 nextFixture : mdsFixture.childFixtures) {
                retval = recurseFindMdsFixture(nextFixture, webBeanId);
                if (retval != null) {
                    break;
                }
            }
        }
        return retval;
    }


    /**
     * Will construct OAF web bean simple name from element name.
     *
     * e.g. oa:messageStyledText will result into OAMessageStyledTextBean.
     *
     * @param elemName XML Element Name.
     * @return Web Bean class simple name.
     */
    private String buildOaWebBeanType(final String elemName)
    {
        final String[] arr = elemName.split(":");
        return "OA" + arr[1].substring(0, 1).toUpperCase()
                + arr[1].substring(1) + "Bean";
    }

    /**
     * @return the helper
     */
    public MdsFixtureHelper getHelper()
    {
        return this.helper;
    }


    //    /**
    //     * @return the refUtil
    //     */
    //    public ReflectUtil getRefUtil()
    //    {
    //        return this.refUtil;
    //    }


    /**
     * @return the pageLayout
     */
    public OAPageLayoutBean getPageLayout()
    {
        return this.pageLayout;
    }


    /**
     * @param pageLayout the pageLayout to set
     */
    public void setPageLayout(final OAPageLayoutBean pageLayout)
    {
        this.pageLayout = pageLayout;
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return super.toString() + " " + RCS_ID;
    }

}