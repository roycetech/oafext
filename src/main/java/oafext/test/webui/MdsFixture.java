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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oafext.OafExtException;
import oafext.ann.Revision;
import oafext.lang.Return;
import oafext.logging.OafLogger;
import oafext.util.StringUtil;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Root MDS is single, while external MDS can be multiple.
 *
 * <pre>
 * @author $Author$
 * @version $Date$
 * </pre>
 */
@Revision("$Revision: $")
public class MdsFixture {


    /** */
    private static final OafLogger LOGGER = OafLogger.getInstance();


    /** */
    private final MdsFixture parent;
    /** Children MDS Fixtures. */
    private final transient List<MdsFixture> childFixtures =
            new ArrayList<MdsFixture>();


    /** Temporary placeholder for external MDS' web bean Id. */
    private final transient String extWbId;
    /** For display only. */
    private transient String mdsPath;


    /** */
    private transient WebBeanMocker<? extends OAWebBean> rootWbMocker;
    /** */
    private transient PageContextMocker pageContextMocker;


    /** */
    private transient Element rootElement;


    /** @param pMdsPath (e.g. "/xxx/oracle/apps/xx/module/webui/SomePG". */
    public MdsFixture(final String pMdsPath) {
        this(pMdsPath, null, null);
        assert pMdsPath.endsWith("PG") : "Root MDS constructor can be used for PG only.";
    }

    /**
     * @param pMdsPath MDS path.
     * @param pParent parent MDS fixture.
     * @param pWebBeanId web bean ID.
     */
    MdsFixture(final String pMdsPath, final MdsFixture pParent,
            final String pWebBeanId) {
        assert StringUtil.hasValue(pMdsPath);
        this.mdsPath = pMdsPath;
        this.parent = pParent;
        this.extWbId = pWebBeanId;
        processMds(pMdsPath, pParent);
    }

    /**
     * @param pMdsPath MDS path.
     * @param extRegionId extended regions web bean ID.
     */
    final void processMds(final String pMdsPath, final MdsFixture parent)
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final String mdsFilename = pMdsPath + ".xml";
            //LOGGER.info("mdsFilename: " + mdsFilename);

            final URL resource = getClass().getResource(mdsFilename);

            final Document document =
                    docBuilder.parse(new File(resource.getFile().replaceAll(
                        "%20",
                        " ")));
            this.rootElement = document.getDocumentElement();

            recurseNode(this.rootElement, 1);

        } catch (final SAXException e) {
            throw new OafExtException(e);
        } catch (final IOException e) {
            throw new OafExtException(e);
        } catch (final ParserConfigurationException e) {
            throw new OafExtException(e);
        }

    }

    /**
     * @param pNode XML node to process.
     * @param level track recursive level.
     * @param extRegionId extended regions web bean ID.
     */
    final void recurseNode(final Node pNode, final int level)
    {

        if (pNode.getNodeName().startsWith("oa:")) {

            processOaNode(pNode);
        }

        final NodeList nodes = pNode.getChildNodes();
        if (nodes != null) {

            for (int i = 0; i < nodes.getLength(); i++) {
                recurseNode(nodes.item(i), level + 2);
            }

        }
    }

    /**
     * @param pNode
     */
    void processOaNode(final Node pNode)
    {
        final Element element = (Element) pNode;
        if (getRootWbMocker() == null) {
            this.rootWbMocker = createTopMocker(this.extWbId, element);

            if (pNode.getNodeName().startsWith("oa:pageLayout")) {
                this.pageContextMocker = new PageContextMocker(this);
            }
        }

        final String extension = element.getAttribute("extends");

        if (StringUtil.hasValue(extension)) {
            final String beanId = element.getAttribute("id");
            final MdsFixture extMds = new MdsFixture(extension, this, beanId);
            this.childFixtures.add(extMds);
        }
    }

    Element findElement(final Node pNode, final String id)
    {
        assert id != null;
        final Return<Element> retval = new Return<Element>();
        if (pNode.getNodeName().startsWith("oa:")) {

            final Element element = (Element) pNode;
            if (id.equals(element.getAttribute("id"))) {
                retval.set(element);
            }
        }

        final NodeList nodes = pNode.getChildNodes();
        if (retval.get() == null && nodes != null) {

            for (int i = 0; i < nodes.getLength(); i++) {
                final Element childEl = findElement(nodes.item(i), id);
                retval.set(childEl);
                if (retval.get() != null) {
                    break;
                }
            }
        }
        return retval.get();
    }


    public Element findElement(final String id)
    {
        return findElement(this.rootElement, id);
    }

    public Element findElRecursive(final String id)
    {
        final Return<Element> retval = new Return<>();
        final Element currentEl = findElement(this.rootElement, id);
        if (currentEl == null) {
            for (final MdsFixture mdsFixture : this.childFixtures) {
                final Element childEl = mdsFixture.findElRecursive(id);
                if (childEl != null) {
                    retval.set(childEl);
                    retval.toString();
                    break;
                }
            }
        } else {
            retval.set(currentEl);
        }
        return retval.get();
    }


    /**
     * @return the pageContextMocker
     */
    public PageContextMocker getPageContextMocker()
    {
        return this.pageContextMocker;
    }

    /**
     * @return the rootWbMocker
     */
    public WebBeanMocker<? extends OAWebBean> getRootWbMocker()
    {
        return this.rootWbMocker;
    }

    /**
     * @return the parent
     */
    public MdsFixture getParent()
    {
        return this.parent;
    }

    WebBeanMocker<? extends OAWebBean> createTopMocker(final String webBeanId,
                                                       final Element element)
    {
        final String elemId = element.getAttribute("id");

        LOGGER.debug("WBID: " + webBeanId + ", ELID: " + elemId + ", "
                + element.getNodeName());

        final String oaWebBeanType =
                OABeanUtil.buildOaWebBeanType(element.getNodeName());

        final Class<? extends OAWebBean> oaClass =
                OABeanUtil.getOABeanClass(oaWebBeanType);

        WebBeanMocker<? extends OAWebBean> retval;
        if (Modifier.isFinal(oaClass.getModifiers())) {
            retval =
                    new WebBeanMocker<>(this, webBeanId, OAWebBean.class, false);
        } else {
            retval = new WebBeanMocker<>(this, webBeanId, oaClass, false);
        }
        return retval;
    }

    public WebBeanMocker<? extends OAWebBean> mockWebBean(final String webBeanId)
    {
        final Element element = findElRecursive(webBeanId);

        final Return<WebBeanMocker<? extends OAWebBean>> retval =
                new Return<>();
        if (element == null) {
            OafLogger.getInstance().warn("Element not found for: " + webBeanId);
        } else {
            retval.set(createTopMocker(webBeanId, element));
            getRootWbMocker().addChildMocker(webBeanId, retval.get());
        }

        return retval.get();
    }


    public void addChild(final MdsFixture createdMds)
    {
        this.childFixtures.add(createdMds);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new StringBuilder()
            .append(getClass().getSimpleName())
            .append('\n')
            .append("Bean Id: ")
            .append(this.extWbId)
            .append('\n')
            .append("Path: ")
            .append(this.mdsPath)
            .append('\n')
            .append("PCM: ")
            .append(this.pageContextMocker)
            .append('\n')
            .append("RWM: ")
            .append(this.rootWbMocker)
            .append('\n')
            .append(this.childFixtures)
            .toString();
    }

}
