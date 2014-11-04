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
package oafext.test.mock;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.Action;

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
public class ActionWebBeanMocker {


    /** Internal source control version. */
    public static final String RCS_ID = "$Revision$";


    /** sl4j logger instance. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(ActionWebBeanMocker.class);


    /** WebBeanID to Element map. */
    private final transient BeanElementMap beanIdElementMap;

    /** MDS Path to (WebBeanID-Mock Web Bean) map or map. */
    private final transient MdsBeanMap mdsBeanMap;

    /** MDS Path to (WebBeanID-Rendered State) map of map. */
    private final transient MdsShowMap mdsShowMap;

    /** MDS Path to (WebBeanID-Editable State) map of map. */
    private final transient MdsLockMap mdsLockMap;

    /** MDS Path to (WebBeanID-required State) map of map. */
    private final transient MdsRequiredMap mdsRequiredMap;

    /** MDS Path to (WebBeanID-Parent WebBeanID) map of map. */
    private final transient MdsParentMap mdsParentMap;

    /** MDS Path to (WebBeanID-Children WebBeanID) map of map of List. */
    private final transient MdsChildrenMap mdsChildrenMap;


    /** MDS Path to Children MDS Fixtures. */
    private final transient MdsActionMockMap mdsActionMockersMap;

    /** Reference parent ActionWebBeanMocker. */
    private final transient ActionWebBeanMocker parentActWbMocker;


    /** External MDS path to parent WebBeanID Map. */
    private final transient ExtMdsParentIdMap extMdsParentIdMap;

    /** External MDS path to parent WebBeanID Map. */
    private final transient BeanExtMdsMap beanExtMdsMap;


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
    private final transient StringUtil strUtil = new StringUtil();

    /** */
    private final transient WbMockerHelper helper = new WbMockerHelper();


    /** @param pMdsPath (e.g. "/xxx/oracle/apps/xx/module/webui/SomePG". */
    public ActionWebBeanMocker(final String pMdsPath) {
        this(pMdsPath, null);
    }


    ActionWebBeanMocker(final String pMdsPath, final ActionWebBeanMocker parent) {

        assert getStrUtil().hasValue(pMdsPath);

        this.mdsPath = pMdsPath;

        this.beanIdElementMap = new BeanElementMap();

        this.mdsBeanMap = new MdsBeanMap();
        this.mdsBeanMap.put(pMdsPath, new BeanMockMap());

        this.mdsShowMap = new MdsShowMap();
        this.mdsShowMap.put(pMdsPath, new BeanShowMap());

        this.mdsLockMap = new MdsLockMap();
        this.mdsLockMap.put(pMdsPath, new BeanLockMap());

        this.mdsRequiredMap = new MdsRequiredMap();
        this.mdsRequiredMap.put(pMdsPath, new BeanRequiredMap());

        this.mdsParentMap = new MdsParentMap();
        this.mdsParentMap.put(pMdsPath, new BeanParentMap());

        this.mdsChildrenMap = new MdsChildrenMap();
        this.mdsChildrenMap.put(pMdsPath, new BeanChildrenMap());

        this.mdsActionMockersMap = new MdsActionMockMap();
        this.mdsActionMockersMap.put(pMdsPath, new ActionMockList());

        this.dupedBeanMap = new DupedBeanMap();

        this.contIdToChildMap = new HashMap<String, String>();

        this.extMdsParentIdMap = new ExtMdsParentIdMap();
        this.beanExtMdsMap = new BeanExtMdsMap();

        this.parentActWbMocker = parent;

        if (parent == null) {
            processMds(pMdsPath, null);
            processDuplicates();
        }
    }

    /**
     * Internal constructor for extended regions.
     * 
     * @param pMdsPath MDS path of the extended region.
     * @param containerId Parent region ID using extended region implementation.
     * @param parent Parent MDS Fixture instance.
     */
    private ActionWebBeanMocker(final String pMdsPath,
            final String containerId, final ActionWebBeanMocker parent) {

        this(pMdsPath, parent);

        //        MDS_TO_ID2EL_MMAP.put(pMdsPath, new HashMap<String, Element>());
        //        MDS_TO_ID2WB_MMAP.put(pMdsPath, new HashMap<String, OAWebBean>());
        //        MDS2_ID2SHOW_MMAP.put(pMdsPath, new HashMap<String, Boolean>());
        //        MDS2_ID2EDIT_MMAP.put(pMdsPath, new HashMap<String, Boolean>());
        //        MDS2_ID2REQR_MMAP.put(pMdsPath, new HashMap<String, Boolean>());
        //        MDS2_ID2PID_MMAP.put(pMdsPath, new HashMap<String, String>());
        //        MDS2_ID2CHILDREN_MMAP
        //            .put(pMdsPath, new HashMap<String, List<String>>());
        //        MDS2_FIXLIST_MAP.put(pMdsPath, new ArrayList<MdsFixture>());

        processMds(pMdsPath, containerId);
    }


    /**
     * Should be invoked on unit test tearDown().
     */
    public void tearDown()
    {
        //Not Cleared
        //beanIdElementMap

        //Cleared
        this.mdsBeanMap.clear();
        this.mdsShowMap.clear();
        this.mdsLockMap.clear();
        this.mdsRequiredMap.clear();
        this.mdsParentMap.clear();
        this.mdsChildrenMap.clear();
        this.mdsActionMockersMap.clear();
        this.dupedBeanMap.clear();
        this.contIdToChildMap.clear();
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

            final Document document = docBuilder.parse(new File(resource
                .getFile()));
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

                final BeanChildrenMap wbChildrenMap = this.mdsChildrenMap
                    .get(getWebBeanMdsPath(this.parentActWbMocker, extRegionId));

                wbChildrenMap.put(extRegionId, children);
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
                    if (this.mdsParentMap.get(this.mdsPath) == null
                            && this.mdsPath != null) {
                        this.mdsParentMap
                            .put(this.mdsPath, new BeanParentMap());
                    }
                    final Map<String, String> webBeanParentMap = this.mdsParentMap
                        .get(this.mdsPath);

                    webBeanParentMap.put(childElemId, containerId);
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
        LOGGER.info("*** " + nodeName + " -> "
                + elem.getParentNode().getParentNode().getNodeName());

        final String webBeanId = elem.getAttribute("id");
        final String parentWebBeanId = ((Element) elem
            .getParentNode()
            .getParentNode()).getAttribute("id");

        LOGGER.info(webBeanId + " -> " + parentWebBeanId);
        if ("".equals(parentWebBeanId)) {

            final String mdsParentId = this.extMdsParentIdMap.get(this.mdsPath);
            if (mdsParentId != null) {
                registerChild(mdsParentId, webBeanId);
            }
        } else {

            final Map<String, String> webBeanParentMap = this.mdsParentMap
                .get(this.mdsPath);
            webBeanParentMap.put(webBeanId, parentWebBeanId);
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
        LOGGER.info(getStrUtil().space(level) + nodeName + " - " + webBeanId);

        output
            .append(getStrUtil().space(level))
            .append(nodeName)
            .append(" - ")
            .append(webBeanId);

        final String attrShow = elem.getAttribute("rendered");
        final String attrReadOnly = elem.getAttribute("readOnly");
        output.append(", show=" + attrShow + ", locked=" + attrReadOnly);
        final String attrExtends = elem.getAttribute("extends");

        if (getStrUtil().hasValue(attrExtends)) {

            if (getHelper().getPackage(attrExtends) != null
                    && getHelper().getPackage(attrExtends).equals(
                        getHelper().getPackage(this.mdsPath))) {

                output.append(" - ").append(attrExtends).append('\n');
                final String extElementId = elem.getAttribute("id");
                final ActionMockList children = this.mdsActionMockersMap
                    .get(this.mdsPath);
                this.beanExtMdsMap.put(extElementId, attrExtends);
                this.extMdsParentIdMap.put(attrExtends, extElementId);
                //webBeanIdWithExtChild.add(extElementId);
                children.add(new ActionWebBeanMocker(
                    attrExtends,
                    extElementId,
                    this));
                //processMds(attrExtends, extElementId);

            } else {
                output.append(" - external");
            }
        }
        LOGGER.info(output.toString()); //NOTE: important for checking MDS content.
    }

    /**
     * @param parentBeanId parent web bean id.
     * @param childBeanId web bean id to add to children list of web bean id to
     *            parent.
     */
    void registerChild(final String parentBeanId, final String childBeanId)
    {


        if (this.mdsChildrenMap.get(this.mdsPath) == null) {
            this.mdsChildrenMap.put(this.mdsPath, new BeanChildrenMap());
        }

        Map<String, List<String>> wbChildrenMap;
        if (this.beanExtMdsMap.get(parentBeanId) == null) {

            wbChildrenMap = this.mdsChildrenMap.get(this.mdsPath);
        } else {

            wbChildrenMap = this.mdsChildrenMap.get(this.beanExtMdsMap
                .get(parentBeanId));
        }

        if (wbChildrenMap.get(parentBeanId) == null) {
            wbChildrenMap.put(parentBeanId, new ArrayList<String>());
        }
        final List<String> children = wbChildrenMap.get(parentBeanId);
        children.add(childBeanId);
    }


    /**
     * Gets the MDS path of the given web bean id. MDS path can be the PG or an
     * external region reference by the PG.
     * 
     * @param actWbMocker Action Web Bean Mocker instance.
     * @param webBeanId web bean ID.
     */
    String getWebBeanMdsPath(final ActionWebBeanMocker actWbMocker,
                             final String webBeanId)
    {
        String retval = null; //NOPMD: null default, conditionally redefine.
        Element element = this.beanIdElementMap.get(webBeanId);
        if (element == null) {
            final ActionMockList children = this.mdsActionMockersMap
                .get(this.mdsPath);

            for (final ActionWebBeanMocker nextMocker : children) {
                element = findElementInMds(nextMocker, webBeanId);
                if (element != null) {
                    retval = nextMocker.mdsPath;
                    break;
                }
            }
        } else {
            retval = actWbMocker.mdsPath;
            //getLogger().debug(webBeanId + " found in " + mdsPath);
        }
        return retval;
    }

    /**
     * 
     * @param actionMocker
     * @param webBeanId
     */
    Element findElementInMds(final ActionWebBeanMocker actionMocker,
                             final String webBeanId)
    {
        Element retval = actionMocker.beanIdElementMap.get(webBeanId);
        if (retval == null) {
            final ActionMockList actionMockList = this.mdsActionMockersMap
                .get(actionMocker.mdsPath);
            for (final ActionWebBeanMocker nextMocker : actionMockList) {
                retval = findElementInMds(nextMocker, webBeanId);
                if (retval != null) {
                    break;
                }
            }
        }
        return retval;
    }

    public void mock(final Action action)
    {

    }


    /**
     * @return the strUtil
     */
    public StringUtil getStrUtil()
    {
        return this.strUtil;
    }


    /**
     * @return the helper
     */
    public WbMockerHelper getHelper()
    {
        return this.helper;
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return super.toString() + " " + RCS_ID;
    }

}