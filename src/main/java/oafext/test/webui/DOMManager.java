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
import oafext.lang.ObjectUtil;
import oafext.lang.Return;
import oafext.logging.OafLogger;
import oafext.util.StringUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author $Author: $
 * @version $Date: $
 */
public class DOMManager {

    /** */
    private transient Element rootElement;

    /** */
    private static final OafLogger LOGGER = OafLogger.getInstance();

    /**
     * @param pMdsPath
     */
    DOMManager(final String pMdsPath) {
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
        } catch (final SAXException e) {
            throw new OafExtException(e);
        } catch (final IOException e) {
            throw new OafExtException(e);
        } catch (final ParserConfigurationException e) {
            throw new OafExtException(e);
        }

    }


    /**
     * Invoked by MDS fixture after constructor to initialize MDS with this DOM.
     *
     * @param pMdsFixture MDS fixture instance.
     */
    void initializeDOM(final MdsFixture pMdsFixture)
    {
        recurseNode(pMdsFixture, this.rootElement, 1);
    }

    /**
     * @param pMdsFixture MDS fixture instance.
     * @param pNode XML node to process.
     * @param level track recursive level.
     * @param extRegionId extended regions web bean ID.
     */
    final void recurseNode(final MdsFixture pMdsFixture, final Node pNode,
                           final int level)
    {

        if (pNode.getNodeName().startsWith("oa:")) {

            processOaNode(pMdsFixture, pNode, level);
        }

        final NodeList nodes = pNode.getChildNodes();
        if (nodes != null) {

            for (int i = 0; i < nodes.getLength(); i++) {
                recurseNode(pMdsFixture, nodes.item(i), level + 2);
            }

        }
    }

    /**
     * @param pNode
     */
    void processOaNode(final MdsFixture pMdsFixture, final Node pNode,
                       final int level)
    {
        final Element element = (Element) pNode;
        if (pMdsFixture.getTopWbMocker() == null) {

            final String oaWebBeanType =
                    OABeanUtil.buildOaWebBeanType(element.getNodeName());
            pMdsFixture.initRootMocker(oaWebBeanType);
        }

        final String elemId = element.getAttribute("id");
        final String extension = element.getAttribute("extends");

        LOGGER.debug(String.format("%-" + level + "s", " ") + "Name: "
                + pNode.getNodeName() + "(" + elemId + ") " + extension);


        if (StringUtil.hasValue(extension)) {
            final String beanId = element.getAttribute("id");
            pMdsFixture.giveBirth(extension, beanId);
        }
    }

    /**
     * @param webBeanId null for top level children Elements.
     */
    String[] getChildrenIDS(final String webBeanId)
    {
        final Element parentEl = findElement(webBeanId);
        assert parentEl != null;
        final List<String> childrenIDs = new ArrayList<>();

        final NodeList nodes = getUIContents(parentEl);
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeName().startsWith("oa:")) {
                final Element childEl = (Element) nodes.item(i);
                childrenIDs.add(childEl.getAttribute("id"));
            }
        }
        return childrenIDs.toArray(new String[childrenIDs.size()]);
    }

    /**
     * Return UI contents which allegedly contains the indexed child nodes.
     *
     * @param parentEl parent element.
     */
    NodeList getUIContents(final Element parentEl)
    {
        final NodeList nodes = parentEl.getChildNodes();
        final Return<NodeList> retval = new Return<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeName().startsWith("ui:contents")) {
                retval.set(nodes.item(i).getChildNodes());
                break;
            }
        }
        return retval.get();
    }


    Element findElement(final Node pNode, final String id)
    {
        final Return<Element> retval = new Return<Element>();
        if (pNode.getNodeName().startsWith("oa:")) {

            final Element element = (Element) pNode;
            if (ObjectUtil.isEqual(id, element.getAttribute("id"))) {
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


}
