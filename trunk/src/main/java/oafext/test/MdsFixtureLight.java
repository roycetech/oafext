/**
 * 
 */
package oafext.test;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import oafext.logging.OafLogger;
import oafext.test.webui.OABeanUtil;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.apps.fnd.framework.webui.beans.form.OASubmitButtonBean;
import oracle.apps.fnd.framework.webui.beans.message.OAMessageStyledTextBean;
import oracle.apps.fnd.framework.webui.beans.nav.OAButtonBean;
import oracle.apps.fnd.framework.webui.beans.nav.OALinkBean;
import oracle.apps.fnd.framework.webui.beans.table.OAAddTableRowBean;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import org.junit.Assert;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Used for integration test of controller to MDS Objects. Full UNIT type of
 * test very hard to do since it will make a significant reduction in the
 * convenience provided by the Snow/OAF MDS framework.
 * 
 * Class variables to minimize memory foot print. Instance is recursive if
 * external region (within same scope) is utilized.
 * 
 * Provide mock functionality for OAWebBean methods:
 * <ul>
 * <li>findChildRecursive
 * <li>isRendered
 * <li>setRendered
 * <li>isReadOnly
 * <li>setReadOnly
 * </ul>
 * 
 * @author r39
 */
public class MdsFixtureLight {


    /**
     * Duplicates are okay as long as they are not referenced by
     * findChildRecursive. We cannot predict the outcome so it is flagged as
     * error if attempt to find duplicated Id.
     */
    private static final Map<String, List<String>> DUPED_WB_IDS = new HashMap<String, List<String>>();

    /** Container ID to Child ID map. */
    private static final Map<String, String> CONTAINER_ID_MAP = new HashMap<String, String>();

    /** For read only type beans (e.g. OAMessageStyledTextBean). */
    private static final List<String> PERMA_LOCK_IDS = new ArrayList<String>();

    /** MDS Path to (WebBeanID-Element) map of map. */
    private static final Map<String, Map<String, Element>> MDS_TO_ID2EL_MMAP = new HashMap<String, Map<String, Element>>();

    /** MDS Path to (WebBeanID-Mock Web Bean) map or map. */
    private static final Map<String, Map<String, OAWebBean>> MDS_TO_ID2WB_MMAP = new HashMap<String, Map<String, OAWebBean>>();

    /** MDS Path to (WebBeanID-Rendered State) map of map. */
    private static final Map<String, Map<String, Boolean>> MDS2_ID2SHOW_MMAP = new HashMap<String, Map<String, Boolean>>();

    /** MDS Path to (WebBeanID-Editable State) map of map. */
    private static final Map<String, Map<String, Boolean>> MDS2_ID2EDIT_MMAP = new HashMap<String, Map<String, Boolean>>();

    /** MDS Path to (WebBeanID-required State) map of map. */
    private static final Map<String, Map<String, Boolean>> MDS2_ID2REQR_MMAP = new HashMap<String, Map<String, Boolean>>();

    /** MDS Path to (WebBeanID-Parent WebBeanID) map of map. */
    private static final Map<String, Map<String, String>> MDS2_ID2PID_MMAP = new HashMap<String, Map<String, String>>();

    /** MDS Path to (WebBeanID-Children WebBeanID) map of map of List. */
    private static final Map<String, Map<String, List<String>>> MDS2_ID2CHILDREN_MMAP = new HashMap<String, Map<String, List<String>>>();

    /** MDS Path to Children MDS Fixtures. */
    private static final Map<String, List<MdsFixtureLight>> MDS2_FIXLIST_MAP = new HashMap<String, List<MdsFixtureLight>>();

    /** WebBeanID to external MDS path Map. */
    private static final Map<String, String> WBID_TO_EXTMDS_MAP = new HashMap<String, String>();

    /** External MDS path to parent WebBeanID Map. */
    private static final Map<String, String> MDS_TO_PARENTID_MAP = new HashMap<String, String>();

    /** Place holder for {@link #addCustomWebBean(String, OAWebBean)}. */
    private static final List<String> CUSTOM_BEANS = new ArrayList<String>();

    /** Reference parent MdsFixture. */
    private transient MdsFixtureLight parentMdsFixture = null;

    /** This is optional AM Fixture to mock SPEL mechanism. */
    private static CustomAmFixture amFixture = null;

    /** MDS Path. */
    private transient String mdsPath;

    /** blech! global!. */
    private transient boolean containerIdSet = false;

    /**
     * Synchronize object to implement block synchronization mechanism in the
     * constructor.
     */
    private static Object lock = new Object();

    /** Flag to initialize state of class variables in the constructor. */
    private static boolean initialized = false;

    /**
     * Allow to add custom Web Bean to simulate beans injected by framework code
     * not present in the MDS file.
     * 
     * @param webBeanId Web Bean ID of the custom web bean.
     * @param webBean custom web bean instance.
     */
    public void addCustomWebBean(final String webBeanId, final OAWebBean webBean)
    {
        for (final Class<?> nextClass : READONLY_TYPES) {
            if (nextClass.isAssignableFrom(webBean.getClass())) {
                PERMA_LOCK_IDS.add(webBeanId);
                break;
            }
        }

        CUSTOM_BEANS.add(webBeanId);
        final Map<String, OAWebBean> map = MDS_TO_ID2WB_MMAP.get(this.mdsPath);
        map.put(webBeanId, webBean);
        mockWebBeanBehavior(webBean, webBeanId, this.mdsPath);
    }

    /** @param pMdsPath e.g. "/xxx/oracle/apps/xx/somemodule/webui/SomepagePG". */
    public MdsFixtureLight(final String pMdsPath) {
        this.mdsPath = pMdsPath;
        synchronized (lock) {
            if (!initialized) {
                MDS_TO_ID2EL_MMAP.put(
                    this.mdsPath,
                    new HashMap<String, Element>());
                MDS_TO_ID2WB_MMAP.put(
                    pMdsPath,
                    new HashMap<String, OAWebBean>());
                MDS2_ID2SHOW_MMAP.put(pMdsPath, new HashMap<String, Boolean>());
                MDS2_ID2EDIT_MMAP.put(pMdsPath, new HashMap<String, Boolean>());
                MDS2_ID2REQR_MMAP.put(pMdsPath, new HashMap<String, Boolean>());
                MDS2_ID2PID_MMAP.put(pMdsPath, new HashMap<String, String>());
                MDS2_ID2CHILDREN_MMAP.put(
                    pMdsPath,
                    new HashMap<String, List<String>>());
                MDS2_FIXLIST_MAP
                    .put(pMdsPath, new ArrayList<MdsFixtureLight>());
                processMds(pMdsPath, null); //NOPMD: method in question is already final, why still flagged?
                processDuplicates();
                initialized = true;
            }
        }
    }

    /**
     * Internal constructor for extended regions.
     * 
     * @param pMdsPath MDS path of the extended region.
     * @param containerId Parent region ID using extended region implementation.
     * @param parent Parent MDS Fixture instance.
     */
    private MdsFixtureLight(final String pMdsPath, final String containerId,
            final MdsFixtureLight parent) {
        this.mdsPath = pMdsPath;
        MDS_TO_ID2EL_MMAP.put(pMdsPath, new HashMap<String, Element>());
        MDS_TO_ID2WB_MMAP.put(pMdsPath, new HashMap<String, OAWebBean>());
        MDS2_ID2SHOW_MMAP.put(pMdsPath, new HashMap<String, Boolean>());
        MDS2_ID2EDIT_MMAP.put(pMdsPath, new HashMap<String, Boolean>());
        MDS2_ID2REQR_MMAP.put(pMdsPath, new HashMap<String, Boolean>());
        MDS2_ID2PID_MMAP.put(pMdsPath, new HashMap<String, String>());
        MDS2_ID2CHILDREN_MMAP
            .put(pMdsPath, new HashMap<String, List<String>>());
        MDS2_FIXLIST_MAP.put(pMdsPath, new ArrayList<MdsFixtureLight>());

        this.parentMdsFixture = parent;
        processMds(pMdsPath, containerId);
    }

    /** Parallel setup must be called on JUnit test case setUp(). */
    public void setUp()
    {
        if (getAmFixture() != null) {
            getAmFixture().setUp();
        }

        //NOTE: Initialize here for convenience rather than do lazy loading. 
        for (final String nextMds : MDS_TO_ID2WB_MMAP.keySet()) {
            MDS_TO_ID2WB_MMAP.put(nextMds, new HashMap<String, OAWebBean>()); //NOPMD: See Note.
            MDS2_ID2SHOW_MMAP.put(nextMds, new HashMap<String, Boolean>()); //NOPMD: See Note.
            MDS2_ID2EDIT_MMAP.put(nextMds, new HashMap<String, Boolean>()); //NOPMD: See Note.
            MDS2_ID2REQR_MMAP.put(nextMds, new HashMap<String, Boolean>()); //NOPMD: See Note.

        }
    }

    /**
     * @param pMdsPath MDS path.
     * @param containerId parent web bean id of the extended MDS path.
     */
    final void processMds(final String pMdsPath, final String containerId)
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final String currentPath = new File(".").getAbsolutePath();
            final String projectPath = currentPath.substring(
                0,
                currentPath.length() - 2)
                    + "\\build";
            final String mdsFilename = projectPath + pMdsPath + ".xml";
            final Document document = docBuilder.parse(new File(mdsFilename));
            final Element root = document.getDocumentElement();

            if (containerId != null) {
                this.containerIdSet = false;
            }
            processNode(root, 0, containerId);
        } catch (final Exception exception) {
            MdsFixtureLight.getLogger().error(exception);
        }
    }

    /**
     * Used to determine package of path. Used to identify if MDS path is
     * outside the package, that it can be by passed.
     * 
     * @param pMdsPath MDS Path.
     * @return package of the MDS path.
     */
    final String getPackage(final String pMdsPath)
    {
        String retval = null; //NOPMD: Reviewed.
        if (pMdsPath != null) {
            retval = pMdsPath.substring(0, pMdsPath.lastIndexOf('/'));
        }
        return retval;
    }

    /**
     * @param pNode XML node to process.
     * @param level track recursive level.
     * @param containerId web bean parent Id.
     */
    final void processNode(final Node pNode, final int level,
            final String containerId)
    {
        final NodeList nodes = pNode.getChildNodes();
        if (nodes != null) {
            final List<String> children = new ArrayList<String>();
            for (int i = 0; i < nodes.getLength(); i++) {
                processChildNode(nodes.item(i), containerId, children, level);
            }

            //children may be inserted upon inside processChildNode.
            if (!children.isEmpty() && pNode instanceof Element
                    && containerId != null) {
                final Map<String, List<String>> wbChildrenMap = MDS2_ID2CHILDREN_MMAP
                    .get(getWebBeanMdsPath(this.parentMdsFixture, containerId));
                wbChildrenMap.put(containerId, children);
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

            if (containerId != null) {
                if (!this.containerIdSet) {
                    if (MDS2_ID2PID_MMAP.get(this.mdsPath) == null
                            && this.mdsPath != null) {
                        MDS2_ID2PID_MMAP.put(
                            this.mdsPath,
                            new HashMap<String, String>());
                    }
                    final Map<String, String> webBeanParentMap = MDS2_ID2PID_MMAP
                        .get(this.mdsPath);

                    webBeanParentMap.put(childElemId, containerId);
                    this.containerIdSet = true;
                }
                CONTAINER_ID_MAP.put(childElemId, containerId);
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
     * @param parent parent web bean id.
     * @param child web bean id to add to children list of web bean id to
     *            parent.
     */
    private void registerChild(final String parent, final String child)
    {
        if (this.mdsPath != null
                && MDS2_ID2CHILDREN_MMAP.get(this.mdsPath) == null) {
            MDS2_ID2CHILDREN_MMAP.put(
                this.mdsPath,
                new HashMap<String, List<String>>());
        }

        Map<String, List<String>> webBeanChildrenMap;
        if (MdsFixtureLight.WBID_TO_EXTMDS_MAP.get(parent) == null) {
            webBeanChildrenMap = MDS2_ID2CHILDREN_MMAP.get(this.mdsPath);
        } else {
            webBeanChildrenMap = MDS2_ID2CHILDREN_MMAP
                .get(MdsFixtureLight.WBID_TO_EXTMDS_MAP.get(parent));
        }

        if (webBeanChildrenMap.get(parent) == null) {
            webBeanChildrenMap.put(parent, new ArrayList<String>());
        }
        final List<String> children = webBeanChildrenMap.get(parent);
        children.add(child);
    }

    /**
     * @param elem Element instance.
     * @param nodeName Node name.
     * @param level Recursion level.
     */
    final void processOAElement(final Element elem, final String nodeName,
            final int level)
    {
        //getLogger().debug("*** " + nodeName + " -> " + elem.getParentNode().getParentNode().getNodeName());

        final String webBeanId = elem.getAttribute("id");
        final String parentWebBeanId = ((Element) elem
            .getParentNode()
            .getParentNode()).getAttribute("id");

        //getLogger().debug(webBeanId + " -> " + parentWebBeanId);
        if (!"".equals(parentWebBeanId)) {
            final Map<String, String> webBeanParentMap = MDS2_ID2PID_MMAP
                .get(this.mdsPath);
            webBeanParentMap.put(webBeanId, parentWebBeanId);
            registerChild(parentWebBeanId, webBeanId);
        } else {
            final String mdsParentId = MDS_TO_PARENTID_MAP.get(this.mdsPath);
            if (mdsParentId != null) {
                registerChild(mdsParentId, webBeanId);
            }
        }

        final Document doc = elem.getOwnerDocument();
        final String docPath = doc.getDocumentURI();

        final String mdsName = docPath.substring(docPath.lastIndexOf('/') + 1);

        if (DUPED_WB_IDS.get(webBeanId) == null) {
            DUPED_WB_IDS.put(webBeanId, new ArrayList<String>());
        }
        final List<String> mdsList = MdsFixtureLight.DUPED_WB_IDS
            .get(webBeanId);
        mdsList.add(mdsName);
        MdsFixtureLight.MDS_TO_ID2EL_MMAP
            .get(this.mdsPath)
            .put(webBeanId, elem);
        final StringBuilder output = new StringBuilder();
        // getLogger().debug(space(level) + nodeName + " - " + webBeanId);
        output.append(space(level) + nodeName + " - " + webBeanId);
        final String attrShow = elem.getAttribute("rendered");
        final String attrReadOnly = elem.getAttribute("readOnly");
        output.append(", show=" + attrShow + ", locked=" + attrReadOnly);
        final String attrExtends = elem.getAttribute("extends");
        if (attrExtends != null && !"".equals(attrExtends)) {
            if (getPackage(attrExtends) != null
                    && getPackage(attrExtends).equals(getPackage(this.mdsPath))) {
                output.append(" - " + attrExtends + "\n");
                final String extElementId = elem.getAttribute("id");
                final List<MdsFixtureLight> children = MDS2_FIXLIST_MAP
                    .get(this.mdsPath);
                WBID_TO_EXTMDS_MAP.put(extElementId, attrExtends);
                MDS_TO_PARENTID_MAP.put(attrExtends, extElementId);
                //webBeanIdWithExtChild.add(extElementId);
                children.add(new MdsFixtureLight(
                    attrExtends,
                    extElementId,
                    this));
                //processMds(attrExtends, extElementId);
            } else {
                output.append(" - external");
            }
        }
        //MdsFixture.getLogger().debug(output);
    }

    /**
     * Generate spaces.
     * 
     * @param count number of spaces to generate.
     * @return string with the specified length.
     */
    final String space(final int count)
    {
        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            strBuilder.append(' ');
        }
        return strBuilder.toString();
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
        for (final String webBeanId : DUPED_WB_IDS.keySet()) {
            if (DUPED_WB_IDS.get(webBeanId).size() == 1) {
                unique.add(webBeanId);
            }
        }
        for (final String string : unique) {
            DUPED_WB_IDS.remove(string);
        }
    }

    /** List of ReadOnly web bean types. */
    static final List<Class<?>> READONLY_TYPES = Arrays.asList(new Class<?>[] {
            OAMessageStyledTextBean.class,
            OAButtonBean.class,
            OASubmitButtonBean.class,
            OALinkBean.class,
            OAAddTableRowBean.class });

    /**
     * Mock implementation for {@link OAWebBean#findChildRecursive(String)}.
     * 
     * @param webBeanId Web Bean ID.
     * @return mocked state of the given webBeanId.
     */
    public OAWebBean findChildRecursive(final String webBeanId)
    {
        OAWebBean mockBean = null; //NOPMD: Default to null, will be redefined for certain conditions.
        if (DUPED_WB_IDS.containsKey(webBeanId)) {
            Assert.fail("Reference to duplicate ID [" + webBeanId + " - "
                    + DUPED_WB_IDS.get(webBeanId) + "] found.");
        } else {
            if (findElementInMds(this, webBeanId) == null) {
                Assert.fail("WebBeanId [" + webBeanId + "]  was not found in "
                        + this.mdsPath);
            } else {
                final String webBeanMdsPath = getWebBeanMdsPath(this, webBeanId);
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

                final Map<String, OAWebBean> webBeanMap = MDS_TO_ID2WB_MMAP
                    .get(webBeanMdsPath);
                if (webBeanMap.get(webBeanId) == null) {
                    final String elemName = MdsFixtureLight.MDS_TO_ID2EL_MMAP
                        .get(webBeanMdsPath)
                        .get(webBeanId)
                        .getNodeName();
                    final String oaWebBeanType = buildOaWebBeanType(elemName);
                    final Class<? extends OAWebBean> oaClass = OABeanUtil
                        .getOABeanClass(oaWebBeanType);

                    if (READONLY_TYPES.contains(oaClass)) {
                        final Map<String, Boolean> webBeanLockState = MDS2_ID2EDIT_MMAP
                            .get(webBeanMdsPath);
                        webBeanLockState.put(webBeanId, true);
                        PERMA_LOCK_IDS.add(webBeanId);
                    }

                    if (Modifier.isFinal(oaClass.getModifiers())) {
                        mockBean = Mockito.mock(OAWebBean.class);
                    } else {
                        mockBean = Mockito.mock(oaClass);
                    }

                    webBeanMap.put(webBeanId, mockBean);
                    mockWebBeanBehavior(mockBean, webBeanId, webBeanMdsPath);
                }

            }
        }
        return mockBean;
    }

    /**
     * Gets the MDS path of the given web bean id. MDS path can be the PG or an
     * external region reference by the PG.
     * 
     * @param mdsFixture
     * @param webBeanId web bean ID.
     */
    String getWebBeanMdsPath(final MdsFixtureLight mdsFixture,
            final String webBeanId)
    {
        final Map<String, Element> elMap = MdsFixtureLight.MDS_TO_ID2EL_MMAP
            .get(mdsFixture.mdsPath);

        String retval = null;
        Element element = elMap.get(webBeanId);
        if (element == null) {
            final List<MdsFixtureLight> children = MDS2_FIXLIST_MAP
                .get(this.mdsPath);
            childloop: for (final MdsFixtureLight nextFixture : children) {
                element = findElementInMds(nextFixture, webBeanId);
                if (element != null) {
                    retval = nextFixture.mdsPath;
                    break childloop;
                }
            }
        } else {
            retval = mdsFixture.mdsPath;
            //getLogger().debug(webBeanId + " found in " + mdsPath);
        }
        return retval;
    }

    /**
     * 
     * @param mdsFixture
     * @param webBeanId
     * @return
     */
    Element findElementInMds(final MdsFixtureLight mdsFixture,
            final String webBeanId)
    {
        final Map<String, Element> elMap = MdsFixtureLight.MDS_TO_ID2EL_MMAP
            .get(mdsFixture.mdsPath);
        Element retval = elMap.get(webBeanId);
        if (retval == null) {
            final List<MdsFixtureLight> children = MDS2_FIXLIST_MAP
                .get(mdsFixture.mdsPath);
            for (final MdsFixtureLight nextFixture : children) {
                retval = findElementInMds(nextFixture, webBeanId);
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


            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final OAWebBean webBean = (OAWebBean) invocation.getMock();
                final boolean doRender = (Boolean) invocation.getArguments()[0];
                Map<String, Boolean> webBeanRenderState;
                if (CUSTOM_BEANS.contains(webBeanId)) {
                    webBeanRenderState = MdsFixtureLight.MDS2_ID2SHOW_MMAP
                        .get(MdsFixtureLight.this.mdsPath);
                } else {
                    webBeanRenderState = MdsFixtureLight.MDS2_ID2SHOW_MMAP
                        .get(this.mdsPath);
                }
                webBeanRenderState.put(webBean.getID(), doRender);
                return null;
            }

        }.setMdsPath(webBeanMdsPath))
            .when(mockBean)
            .setRendered(Matchers.anyBoolean());

        Mockito.when(mockBean.isRendered()).thenAnswer(new Answer<Boolean>() {

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
        if (CUSTOM_BEANS.contains(webBeanId)) {
            retval = getRenderedState(webBeanId);
        } else {
            if (DUPED_WB_IDS.containsKey(webBeanId)) {
                throw new UnsupportedOperationException(
                    "Duplicate ID detected for: " + webBeanId);
            }
            retval = getRenderedState(webBeanId);
            final String webBeanMdsPath = getWebBeanMdsPath(this, webBeanId);

            final Map<String, String> webBeanParentMap = MDS2_ID2PID_MMAP
                .get(webBeanMdsPath);
            String curreWebBeanId = webBeanParentMap.get(webBeanId);

            if (curreWebBeanId == null
                    && CONTAINER_ID_MAP.get(webBeanId) != null) {
                curreWebBeanId = CONTAINER_ID_MAP.get(webBeanId);
            }

            while (retval && curreWebBeanId != null
                    && !"".equals(curreWebBeanId.trim())) {
                retval = getRenderedState(curreWebBeanId);
                curreWebBeanId = webBeanParentMap.get(curreWebBeanId);
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
        final Map<String, Boolean> webBeanRenderState;

        if (CUSTOM_BEANS.contains(webBeanId)) {
            webBeanRenderState = MDS2_ID2SHOW_MMAP.get(this.mdsPath);
            if (webBeanRenderState.get(webBeanId) == null) {
                webBeanRenderState.put(webBeanId, true);
            }
        } else {
            final String webBeanMdsPath = getWebBeanMdsPath(this, webBeanId);
            webBeanRenderState = MDS2_ID2SHOW_MMAP.get(webBeanMdsPath);
            if (webBeanRenderState.get(webBeanId) == null) {
                final Element elem = MdsFixtureLight.MDS_TO_ID2EL_MMAP.get(
                    webBeanMdsPath).get(webBeanId);
                final String attrRendered = elem.getAttribute("rendered");
                if (attrRendered == null || "".equals(attrRendered)
                        || "true".equalsIgnoreCase(attrRendered)) {
                    webBeanRenderState.put(webBeanId, true);
                } else if (attrRendered != null
                        && attrRendered.startsWith("${oa.")) {
                    if (MdsFixtureLight.amFixture != null) {
                        final boolean spelResult = processSpel(attrRendered);
                        webBeanRenderState.put(webBeanId, spelResult);
                    } else {
                        //IGNORE, default to render.  Unit test using the property VO Row.
                        webBeanRenderState.put(webBeanId, true);
                        //                        throw new UnsupportedOperationException("TODO: Unimplemented scenario render state for: ["
                        //                                + webBeanId + "], see CustomAmFixture");
                    }
                } else {
                    getLogger().warn(
                        "Default Render false for [" + webBeanId + "]");
                    webBeanRenderState.put(webBeanId, false);
                }
            }
        }
        return webBeanRenderState.get(webBeanId);
    }

    /**
     * Based on CustomAmFixture will simulate the result of the SPEL expression.
     * 
     * @param spelExpression rtfc.
     * @return result of SPEL expression based on the CustomAmFixture.
     */
    boolean processSpel(final String spelExpression)
    {
        final OAApplicationModuleImpl appModule = MdsFixtureLight.amFixture
            .getMockAppModule();
        final String inside = spelExpression.substring(
            2,
            spelExpression.length() - 1);
        final String[] spelArr = inside.split("\\.");
        final String voInst = spelArr[1];

        final ViewObject viewObject = appModule.findViewObject(voInst);
        Row currRow = viewObject.getCurrentRow();
        if (currRow == null) {
            currRow = viewObject.getRowAtRangeIndex(0);
        }

        boolean retval;
        if (currRow == null) {
            retval = false;
        } else {
            final String attr = spelArr[2];
            retval = currRow.getAttribute(attr) == null ? false
                    : (Boolean) currRow.getAttribute(attr);
        }
        return retval;
    }

    /**
     * Check lock state using the mock implementation.
     * 
     * @param webBeanId Web Bean ID.
     * @return Mocked lock state.
     */
    public boolean getLockedState(final String webBeanId)
    {
        final String webBeanMdsPath = getWebBeanMdsPath(this, webBeanId);
        final Map<String, Boolean> webBeanLockState = MDS2_ID2EDIT_MMAP
            .get(webBeanMdsPath == null ? this.mdsPath : webBeanMdsPath);
        final Boolean lockState = webBeanLockState.get(webBeanId);
        if (PERMA_LOCK_IDS.contains(webBeanId)) {
            return true;
        } else {
            if (lockState == null) {
                final Map<String, Element> wbId2ElMap = MDS_TO_ID2EL_MMAP
                    .get(webBeanMdsPath);
                final Element elem = wbId2ElMap.get(webBeanId);
                final String attrReadOnly = elem.getAttribute("readOnly");
                return Boolean.valueOf(attrReadOnly);
            } else {
                return lockState;
            }
        }
    }

    /**
     * @param webBeanId web bean ID.
     * @return
     */
    public boolean getRequiredState(final String webBeanId)
    {
        final String webBeanMdsPath = getWebBeanMdsPath(this, webBeanId);
        final Map<String, Boolean> wbRequiredState = MDS2_ID2REQR_MMAP
            .get(webBeanMdsPath == null ? this.mdsPath : webBeanMdsPath);
        final Boolean requiredState = wbRequiredState.get(webBeanId);
        if (requiredState == null) {
            return false;
        } else {
            return requiredState;
        }
    }


    /**
     * Mocked implementation of web bean locking.
     * 
     * @param webBeanId Web Bean ID.
     * @param value new lock state.
     */
    public void setLockedState(final String webBeanId, final boolean value)
    {
        final String webBeanMdsPath = getWebBeanMdsPath(this, webBeanId);
        final Map<String, Boolean> webBeanLockState = MDS2_ID2EDIT_MMAP
            .get(webBeanMdsPath);
        webBeanLockState.put(webBeanId, value);
    }

    /**
     * Mocked implementation of web bean Mandatory.
     * 
     * @param webBeanId Web Bean ID.
     * @param value new lock state.
     */
    public void setRequiredState(final String webBeanId, final boolean flag)
    {
        final String webBeanMdsPath = getWebBeanMdsPath(this, webBeanId);
        final Map<String, Boolean> webBeanRequiredState = MDS2_ID2REQR_MMAP
            .get(webBeanMdsPath);
        webBeanRequiredState.put(webBeanId, flag);
    }

    /**
     * 
     * @param origWebBeanId
     * @param webBeanId
     * @param value
     */
    public void setLockedStateRecurse(final String origWebBeanId,
            final String webBeanId, final boolean value)
    {
        if (webBeanId == null) {
            MDS2_ID2EDIT_MMAP.get(getWebBeanMdsPath(this, origWebBeanId)).put(
                origWebBeanId,
                value);
        } else {
            final String extMdsPath = WBID_TO_EXTMDS_MAP.get(origWebBeanId);
            MDS2_ID2EDIT_MMAP.get(
                extMdsPath == null ? getWebBeanMdsPath(this, webBeanId)
                        : extMdsPath).put(webBeanId, value);
        }

        if (webBeanId == null) {
            final String webBeanMdsPath = getWebBeanMdsPath(this, origWebBeanId);
            final Map<String, List<String>> webBeanChildrenMap = MDS2_ID2CHILDREN_MMAP
                .get(webBeanMdsPath);
            if (webBeanChildrenMap.get(origWebBeanId) != null) {
                for (final String child : webBeanChildrenMap.get(origWebBeanId)) {
                    setLockedStateRecurse(origWebBeanId, child, value);
                }
            }
        } else {
            final String extMdsPath = WBID_TO_EXTMDS_MAP.get(origWebBeanId);
            final Map<String, List<String>> webBeanChildrenMap = MDS2_ID2CHILDREN_MMAP
                .get(extMdsPath == null ? getWebBeanMdsPath(this, webBeanId)
                        : extMdsPath);
            final List<String> children = webBeanChildrenMap.get(webBeanId);
            if (children != null) {
                for (final String child : children) {
                    setLockedStateRecurse(origWebBeanId, child, value);
                }
            }

        }
    }

    /**
     * For quick testing only.
     * 
     * @param args unused.
     */
    public static void main(final String[] args)
    {
        getLogger().debug("Start.");
        new MdsFixtureLight("/xxx/oracle/apps/xx/somemodule/webui/SomepagePG");
        getLogger().debug("Duplicates: " + MdsFixtureLight.DUPED_WB_IDS.size());
        //getLogger().debug("Map size: " + MdsFixture.elementMap.size());
        getLogger().debug("Duplicates: " + MdsFixtureLight.DUPED_WB_IDS);
    }

    /** @return Class logger instance. */
    static OafLogger getLogger()
    {
        return OafLogger.getInstance();
    }

    /**
     * @see {@link Object#toString()}.
     * @return MDS path to easily identify child MDS.
     */

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + " : " + this.mdsPath;
    }

    /** @return CustomAmFixture if present. */
    public CustomAmFixture getAmFixture()
    {
        return MdsFixtureLight.amFixture;
    }

    /**
     * NOTE: You do not need to call setUp on the fixture from your test class.
     * TODO: @NOTE: Why?
     * 
     * @param pAmFixture new CustomAmFixture instance.
     */
    public void setAmFixture(final CustomAmFixture pAmFixture)
    {
        MdsFixtureLight.amFixture = pAmFixture;
    }

    /**
     * @param webBean
     */
    public void setRequiredState(final OAWebBean webBean)
    {
        // TODO Auto-generated method stub

    }

}