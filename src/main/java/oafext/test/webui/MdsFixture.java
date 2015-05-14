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
import oracle.apps.fnd.framework.webui.beans.layout.OAPageLayoutBean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
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

    /** */
    private transient WebBeanMocker<OAPageLayoutBean> webBeanMocker;
    /** */
    private transient PageContextMocker pageContextMocker;

    /** */
    private transient Element rootElement;


    /** @param pMdsPath (e.g. "/xxx/oracle/apps/xx/module/webui/SomePG". */
    public MdsFixture(final String pMdsPath) {
        this(pMdsPath, null);
    }

    MdsFixture(final String pMdsPath, final MdsFixture pParent) {
        assert StringUtil.hasValue(pMdsPath);
        this.parent = pParent;
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
            LOGGER.info("mdsFilename: " + mdsFilename);

            final URL resource = getClass().getResource(mdsFilename);

            final Document document =
                    docBuilder.parse(new File(resource.getFile().replaceAll(
                        "%20",
                        " ")));
            this.rootElement = document.getDocumentElement();

            debugDisplayNodes(this.rootElement, 1);

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
    final void debugDisplayNodes(final Node pNode, final int level)
    {

        if (pNode.getNodeName().startsWith("oa:")) {

            if (pNode.getNodeName().startsWith("oa:pageLayout")) {
                initPageContextWebBean();
            }

            final Element element = (Element) pNode;
            LOGGER.debug(String.format("%-" + level + "s", " ") + "Name: "
                    + pNode.getNodeName() + " "
                    + element.getAttribute("extends"));
        }

        final NodeList nodes = pNode.getChildNodes();
        if (nodes != null) {

            for (int i = 0; i < nodes.getLength(); i++) {
                debugDisplayNodes(nodes.item(i), level + 2);
            }

        }
    }

    void initPageContextWebBean()
    {
        this.webBeanMocker =
                new WebBeanMocker<OAPageLayoutBean>(OAPageLayoutBean.class);
        this.pageContextMocker = new PageContextMocker(this.webBeanMocker);
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
        if (nodes != null) {

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

    /**
     * @return the pageContextMocker
     */
    public PageContextMocker getPageContextMocker()
    {
        return this.pageContextMocker;
    }

    /**
     * @return the webBeanMocker
     */
    public WebBeanMocker<OAPageLayoutBean> getWebBeanMocker()
    {
        return this.webBeanMocker;
    }

}