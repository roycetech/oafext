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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oafext.Constant;
import oafext.logging.OafLogger;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ViewObjectImpl;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * TODO: Nested AM.
 * 
 * @author royce
 * 
 * @param <A> application module type.
 */
public class AppModuleFixture<A extends OAApplicationModuleImpl> {


    /** */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(AppModuleFixture.class);


    /** */
    private static final String UNCHECKED = "unchecked";


    /** */
    private final transient AppModuleMocker appModuleMocker;


    /** Platform independent path separator. */
    static final String FILE_SEP = File.separator;


    /** View Object Instance to Type Name. e.g. FinFacilityVO1=FinFacilityVO. */
    private final transient Map<String, String> voNameDefMap;


    /** View Object Instance name to View Object Definition. */
    private final transient Map<String, String> voNameDefFullMap;

    /** View Object Instance name to View Object Class. */
    private final transient Map<String, Class<? extends ViewObjectImpl>> voNameClassMap = new HashMap<String, Class<? extends ViewObjectImpl>>();

    /** View Object Instance name to mock View Object Row type. */
    private final transient Map<String, Class<? extends Row>> voNameRowClsMap = new HashMap<String, Class<? extends Row>>();

    /** View Object Type (e.g. 'SomeVO') to Attribute List map. */
    private final transient Map<String, List<String>> voDefAttrListMap;

    /** Row class to view object Definition. */
    private final transient Map<Class<? extends Row>, String> rowClsVoDefMap = new HashMap<Class<? extends Row>, String>();


    /** rtfc. */
    static final int MAX_PATH_LEN = 128;


    /**
     * @param pAppModuleDef (e.g. "xxx.oracle.apps.xx.module.server.SomeAM" )
     */
    @SuppressWarnings(UNCHECKED)
    public AppModuleFixture(final String pAppModuleDef) {
        LOGGER.info("pAppModuleDef: " + pAppModuleDef);


        this.voDefAttrListMap = new HashMap<String, List<String>>();
        this.voNameDefFullMap = new HashMap<String, String>();
        this.voNameDefMap = new HashMap<String, String>();

        final String amClassName = processAppModuleDef(pAppModuleDef, null);
        try {
            final Class<? extends OAApplicationModuleImpl> klass = (Class<? extends OAApplicationModuleImpl>) Class
                .forName(amClassName);

            this.appModuleMocker = new AppModuleMocker(klass);
        } catch (final ClassNotFoundException e) {
            throw new InitException(e.getMessage() + pAppModuleDef, e);
        }

    }


    /**
     * JUnit 3 compatible setUp place holder.
     */
    @Before
    public void setUp()
    {
        //NOPMD: see javadoc, !
    }

    /** JUnit clean up code. */
    @After
    public void tearDown()
    {
        this.appModuleMocker.tearDown();
    }

    /**
     * Initialize view object row with attribute values.
     * 
     * @param voInstance view object instance name.
     * @param index row index.
     * @param pAttrs attribute to set.
     * @param pValues values to set.
     */
    public void initRowAtIndex(final String voInstance, final int index,
                               final int[] pAttrs, final Object[] pValues)
    {
        this.appModuleMocker.initRowAtIndex(voInstance, index, pAttrs, pValues);
    }

    /**
     * Make calls to ViewObject.isExecuted return true for ALL view objects
     * under the application module.
     * 
     */
    public void setAllViewObjectExecuted()
    {
        this.appModuleMocker.setAllViewObjectExecuted();
    }

    /**
     * Make calls to ViewObject.isExecuted return true for the given view object
     * instance..voType
     * 
     * @param voInstName view object instance.
     */
    public void setViewObjectExecuted(final String voInstName)
    {
        this.appModuleMocker.setViewObjectExecuted(voInstName);
    }


    /**
     * @param voInstName view object instance.
     */
    public void mockViewObject(final String voInstName)
    {
        if (this.voNameClassMap.get(voInstName) == null) {
            LOGGER.info("Initializing view object from xml: " + voInstName);

            final String voDef = this.voNameDefFullMap.get(voInstName);
            assert voDef != null;

            parseVoAndRowType(voInstName, voDef);
        }

        final Class<? extends ViewObject> voClass = this.voNameClassMap
            .get(voInstName);
        assert voClass != null;

        this.appModuleMocker.mockViewObject(this, voInstName);

    }

    /**
     * 
     * @param pAppModuleDef
     * @param parentInstName Parent application module instance name.
     */
    @SuppressWarnings({ "PMD.OnlyOneReturn" })
    private String processAppModuleDef(final String pAppModuleDef,
                                       final String parentInstName)
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final String path = "/" + pAppModuleDef.replaceAll("\\.", "/") + ".xml";
        DocumentBuilder docBuilder;
        try {
            docBuilder = dbf.newDocumentBuilder();
            ignoreDtd(docBuilder);
            getLogger().info("App Def Filename: " + path);
            final Document document = docBuilder.parse(this
                .getClass()
                .getResourceAsStream(path));

            final Element root = document.getDocumentElement();
            final String amClass = root
                .getAttribute(Attribute.OBJECT_IMPL_CLASS);
            processAmRootNode(root, parentInstName);
            return amClass;

        } catch (final ParserConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    final void processAmRootNode(final Node pNode, final String parentInstName)
    {
        final NodeList nodes = pNode.getChildNodes();
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                final Node childNode = nodes.item(i);
                final String nodeName = childNode.getNodeName();
                if (NodeName.VIEW_OBJECT.equals(nodeName)) {
                    final Element elem = (Element) childNode;
                    final String voInstName = elem.getAttribute(Attribute.NAME);
                    final String voDef = elem.getAttribute(Attribute.VO_DEF);

                    this.voNameDefFullMap.put(voInstName, voDef);
                }
            }
        }
    }


    /**
     * Parses the view object definition XML file.
     * 
     * @param voInstName view object instance name.
     * @param voDef view object definition name.
     */
    @SuppressWarnings({
            UNCHECKED,
            "PMD.AvoidCatchingGenericException" })
    void parseVoAndRowType(final String voInstName, final String voDef)
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final String appDefFilename = "/" + voDef.replaceAll("\\.", "/")
                + ".xml";
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            ignoreDtd(docBuilder);

            final Document document = docBuilder.parse(this
                .getClass()
                .getResourceAsStream(appDefFilename));
            final Element root = document.getDocumentElement();

            final String voDefName = root.getAttribute(Attribute.NAME);
            this.voNameDefMap.put(voInstName, voDefName);

            final String implClassName = root
                .getAttribute(Attribute.OBJECT_IMPL_CLASS);

            this.voNameClassMap.put(
                voInstName,
                (Class<? extends ViewObjectImpl>) Class.forName(implClassName));


            final String rowClassName = root.getAttribute(Attribute.VOROW_IMPL);
            final Class<? extends Row> rowClass = (Class<? extends Row>) Class
                .forName(rowClassName);

            this.voNameRowClsMap.put(voInstName, rowClass);
            this.rowClsVoDefMap.put(rowClass, voDefName);

            processVoRootNode(voDefName, root);

        } catch (final Exception exception) {
            LOGGER.error("Error on voInstName[" + voInstName
                    + "], definition: " + voDef, exception);
        }
    }

    /**
     * To use later when reading attribute list from VO.xml.
     * 
     * @param root
     */
    private void processVoRootNode(final String voTypeName, final Element root)
    {
        final NodeList nodes = root.getChildNodes();
        if (nodes != null) {

            final List<String> attrNames = new ArrayList<String>();
            for (int i = 0; i < nodes.getLength(); i++) {
                final Node childNode = nodes.item(i);
                final String nodeName = childNode.getNodeName();
                if (NodeName.ROW_ATTR.equals(nodeName)) {
                    final Element elem = (Element) childNode;
                    final String attrName = elem.getAttribute(Attribute.NAME);
                    attrNames.add(attrName);

                }
            }
            this.voDefAttrListMap.put(voTypeName, attrNames);
        }
    }

    /**
     * This will prevent the DocumentBuilder from validating the DTD. Saves us
     * the trouble of dependence to online DTD resource.
     * 
     * @param docBuilder DocumentBuilder instance.
     */
    private void ignoreDtd(final DocumentBuilder docBuilder)
    {
        docBuilder.setEntityResolver(new EntityResolver() {


            @Override
            public InputSource resolveEntity(final String publicId,
                                             final String systemId)
                    throws SAXException, IOException
            {
                InputSource retval = null; //NOPMD: null default, conditionally redefine.
                if (systemId.contains("jbo_03_01.dtd")) {
                    retval = new InputSource(new StringReader(""));
                }
                return retval;
            }
        });
    }


    /** Known node names. */
    public static class NodeName {

        /** */
        static final String VIEW_OBJECT = "ViewUsage";
        /** */
        static final String VIEW_LINK = "ViewLinkUsage";
        /** */
        static final String APPMODULE = "AppModuleUsage";
        /** */
        static final String ROW_ATTR = "ViewAttribute";
    }


    /** Known attributes. */
    public static class Attribute {


        /** */
        static final String NAME = "Name";

        /** */
        static final String VO_DEF = "ViewObjectName";

        /** View Object class. */
        static final String OBJECT_IMPL_CLASS = "ComponentClass";

        /** Row class. */
        static final String VOROW_IMPL = "RowClass";
    }


    /** @return custom logger instance. */
    public OafLogger getLogger()
    {
        return OafLogger.getInstance();
    }

    /**
     * @return the voNameClassMap
     */
    Map<String, Class<? extends ViewObjectImpl>> getVoNameClassMap()
    {
        return this.voNameClassMap;
    }

    /**
     * @return the voNameRowClsMap
     */
    Map<String, Class<? extends Row>> getVoNameRowClsMap()
    {
        return this.voNameRowClsMap;
    }

    /**
     * @return the voClsAttrListMap
     */
    Map<String, List<String>> getVoDefAttrListMap()
    {
        return this.voDefAttrListMap;
    }

    @SuppressWarnings(Constant.UNCHECKED)
    public A getMockAppModule()
    {
        return (A) this.appModuleMocker.getMockAm();
    }

    /**
     * @return the voNameDefFullMap
     */
    Map<String, String> getVoNameDefMap()
    {
        return this.voNameDefFullMap;
    }

    /**
     * @return the appModuleMocker
     */
    AppModuleMocker getAppModuleMocker()
    {
        return this.appModuleMocker;
    }


    /**
     * @return the rowClsVoDefMap
     */
    Map<Class<? extends Row>, String> getRowClsVoDefMap()
    {
        return this.rowClsVoDefMap;
    }


}


/** */
class InitException extends RuntimeException {
    InitException(final String msg, final Throwable thrw) {
        super(msg, thrw);
    }
}
