package oafext.test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import oafext.logging.OafLogger;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.apps.fnd.framework.server.OAViewRowImpl;
import oracle.jbo.AttributeDef;
import oracle.jbo.Row;
import oracle.jbo.RowSet;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;

import org.junit.After;
import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Custom Application Module fixture.
 * 
 * Use if you use SPEL in page.
 * 
 * Will mock all method of AM to return an instance assignable to ViewObject.
 * This is to handle varying/non-conforming VO instance name.
 * 
 * NOTE: This is designed to be Class variable (static) because this is HEAVY.
 * 
 * @author royce.com
 */
public class CustomAmFixture {

    /** Platform independent path separator. */
    static final String FILE_SEP = File.separator;

    /** For synchronizing a block. */
    private static Object lock = new Object();

    /** Flag to for single initialization. */
    private static Map<String, Boolean> initialized = new HashMap<String, Boolean>();

    /** View Object Instance name to mock View Objects. */
    static final Map<String, ViewObject> VOI_MOCKVO_MAP = new HashMap<String, ViewObject>();

    /** View Object Instance to Type Name. e.g. FinFacilityVO1=FinFacilityVO. */
    static final Map<String, String> VOI_TYPE_MAP = new HashMap<String, String>();

    /** View Object Instance name to View Object Definition. */
    private static final Map<String, Class<? extends ViewObject>> VOI_VOCLS_MAP = new HashMap<String, Class<? extends ViewObject>>();

    /** View Object Instance name to mock View Object Row type. */
    private static final Map<String, Class<? extends Row>> VOI_ROWCLS_MAP = new HashMap<String, Class<? extends Row>>();

    /** Row is represented in parallel as Map<String, Object>. */
    static final Map<String, List<Map<Integer, Object>>> VOI_RCLN_MAP = new HashMap<String, List<Map<Integer, Object>>>();

    /** View Object Instance name to mock View Object Row type. */
    private static final Map<String, RowSetIterator> ROWSET_ITER_MAP = new HashMap<String, RowSetIterator>();


    /** View Object Instance name to List of mock Rows. Used for indexing. */
    static final Map<String, List<Row>> VOI_MROWLST_MAP = new HashMap<String, List<Row>>();

    /** Application Module Implementation definition to class Map. */
    private static final Map<String, Class<? extends OAApplicationModuleImpl>> AMDEF_CLS_MAP = new HashMap<String, Class<? extends OAApplicationModuleImpl>>();

    /** View Object Type (e.g. 'SomeVO') to Attribute List map. */
    static final Map<String, List<String>> VOT_ATTRLST_MAP = new HashMap<String, List<String>>();

    /** rtfc. */
    static final int MAX_PATH_LEN = 128;

    /** Application Module Implementation class. */
    private static Class<? extends OAApplicationModuleImpl> appModuleClass;

    /**
     * Mock Row to View Object Instance Name Map. (Not sure if this is a good
     * idea.)
     */
    static final Map<Row, String> MROW_VOI_MAP = new HashMap<Row, String>();

    /** View Object Instance name to mock current row map. */
    private static final Map<String, Row> VOI_CURROW_MAP = new HashMap<String, Row>();

    /** Mock Application module. */
    private transient OAApplicationModuleImpl mockAppModule;

    static {
        // Eclipse compatibility mode. 
        OafLogger.getInstance().setDeployedMode(true);
    }

    /**
     * @param pAppModuleDef (e.g. "xxx.oracle.apps.xx.module.server.SomeAM" )
     */
    public CustomAmFixture(final String pAppModuleDef) {
        synchronized (lock) {
            if (initialized.get(pAppModuleDef) == null) {
                processAppModule(pAppModuleDef, null);
                initialized.put(pAppModuleDef, Boolean.TRUE);
                CustomAmFixture.appModuleClass = AMDEF_CLS_MAP
                    .get(pAppModuleDef);
                this.mockAppModule = Mockito
                    .mock(CustomAmFixture.appModuleClass);
            }
        }

    }

    /**
     * JUnit 3 compatible setUp place holder.
     */
    @Before
    public void setUp()
    {
        //NOPMD: see javadoc, duh!        
    }

    /** JUnit clean up code. */
    @After
    public void tearDown()
    {
        CustomAmFixture.VOI_CURROW_MAP.clear();
        CustomAmFixture.VOI_MROWLST_MAP.clear();
        CustomAmFixture.VOI_RCLN_MAP.clear();

        //new
        for (final Object o : CustomAmFixture.VOI_MOCKVO_MAP.values()) {
            Mockito.reset(o);
        }
        CustomAmFixture.MROW_VOI_MAP.clear();
        CustomAmFixture.VOI_MOCKVO_MAP.clear();
        CustomAmFixture.ROWSET_ITER_MAP.clear();
    }

    /**
     * 
     * 
     * 
     * @param viewObject mock view object coming from mock application module.
     * @param index row index to modify. This can be a non existent index as
     *            long as it is number of rows + 1.
     * @param pAttr attribute index to modify. Use VORowImpl constant as
     *            paramter. Avoid using magic numbers.
     * @param pValue Value of the attribute.
     */
    public void initVORowAtIndex(final ViewObject viewObject, final int index,
            final int pAttr, final Object pValue)
    {
        initVORowAtIndex(
            viewObject,
            index,
            new int[] { pAttr },
            new Object[] { pValue });
    }

    /**
     * TODO: Missing Row impl, will use
     * {@link #initVORow(String, String[], List)} for the meantime in fixture.
     * 
     * @param viewObject mock view object coming from mock application module.
     * @param index row index to modify.
     * @param pAttrs array of attribute indeces.
     * @param pValues array of values to set.
     */
    public void initVORowAtIndex(final ViewObject viewObject, final int index,
            final int[] pAttrs, final Object[] pValues)
    {
        final String voInstance = viewObject.getName();

        if (CustomAmFixture.VOI_RCLN_MAP.get(voInstance) == null) {
            CustomAmFixture.VOI_RCLN_MAP.put(
                voInstance,
                new ArrayList<Map<Integer, Object>>());
            CustomAmFixture.VOI_MROWLST_MAP.put(
                voInstance,
                new ArrayList<Row>());
        }

        final List<Map<Integer, Object>> clonedRows = CustomAmFixture.VOI_RCLN_MAP
            .get(voInstance);
        final List<Row> rowsMock = CustomAmFixture.VOI_MROWLST_MAP
            .get(voInstance);
        if (clonedRows.size() == index) {
            clonedRows.add(new LinkedHashMap<Integer, Object>());
            final Class<? extends Row> voRowType = VOI_ROWCLS_MAP
                .get(voInstance);
            final Row mockRow = Mockito.mock(voRowType);
            rowsMock.add(mockRow);
            MROW_VOI_MAP.put(mockRow, voInstance);
        }

        final Row mockRow = rowsMock.get(index);
        final Map<Integer, Object> clonedRow = clonedRows.get(index);

        final String voTypeName = CustomAmFixture.VOI_TYPE_MAP.get(voInstance);
        final List<String> rowAttrs = CustomAmFixture.VOT_ATTRLST_MAP
            .get(voTypeName);

        final List<String> targetAttrs = new ArrayList<String>();
        for (final int nextInt : pAttrs) {
            targetAttrs.add(rowAttrs.get(nextInt));
        }

        final List<Object> nextValObject = Arrays.asList(pValues);

        mockGetAttribute(
            voInstance,
            mockRow,
            clonedRow,
            rowAttrs,
            targetAttrs,
            nextValObject);

        final ViewRowMocker mocker = new ViewRowMocker();
        mocker.mockRow(mockRow);

    }

    /**
     * Mocks the generated getters e.g. getProjectId().
     * 
     * TODO: Currently custom getCustom().
     * 
     * @param voInstance
     * @param mockRow
     * @param clonedRow
     * @param rowAttrs
     * @param targetAttrs
     * @param nextValObject
     */
    void mockGetAttribute(final String voInstance, final Row mockRow,
            final Map<Integer, Object> clonedRow, final List<String> rowAttrs,
            final List<String> targetAttrs, final List<Object> nextValObject)
    {
        int counter = 0;
        for (final String nextAttr : rowAttrs) {
            if (targetAttrs.contains(nextAttr)) {
                final int attrIdx = targetAttrs.indexOf(nextAttr);
                clonedRow.put(counter, nextValObject.get(attrIdx));
            }

            final Class<? extends Row> voRowType = VOI_ROWCLS_MAP
                .get(voInstance);
            if (!OAViewRowImpl.class.equals(voRowType)) {
                final String methName = "get"
                        + nextAttr.substring(0, 1).toUpperCase()
                        + nextAttr.substring(1);
                Mockito.when(invokeMethod(mockRow, methName)).thenAnswer(
                    new Answer<Object>() {

                        @Override
                        public Object answer(final InvocationOnMock invocation)
                                throws Throwable
                        {
                            final String voInstance = MROW_VOI_MAP
                                .get(invocation.getMock());
                            final String voTypeName = CustomAmFixture.VOI_TYPE_MAP
                                .get(voInstance);
                            final List<String> rowAttrs = CustomAmFixture.VOT_ATTRLST_MAP
                                .get(voTypeName);

                            final List<Row> voMockRows = CustomAmFixture.VOI_MROWLST_MAP
                                .get(voInstance);
                            final int rowIdx = voMockRows.indexOf(invocation
                                .getMock());
                            final Map<Integer, Object> rowClone = CustomAmFixture.VOI_RCLN_MAP
                                .get(voInstance)
                                .get(rowIdx);
                            final int attrIdx = rowAttrs.indexOf(nextAttr);
                            return rowClone.get(attrIdx);
                        }
                    });
            }
            counter++;
        }
    }

    /**
     * Please do not use hard coded attribute names.
     * 
     * @param voInstance View Object instance name.
     * @param pAttrs array of attributes.
     * @param pValues List of array values. List size determines the number of
     *            rows to initialize/insert when necessary.
     */
    public void initVORow(final String voInstance, final String[] pAttrs,
            final List<Object[]> pValues)
    {

        final ViewObject viewObject = CustomAmFixture.VOI_MOCKVO_MAP
            .get(voInstance);
        for (final Object[] nextValues : pValues) {
            int counter = 0;
            initVORowAtIndex(
                viewObject,
                counter++,
                stringToIntAttributes(voInstance, pAttrs),
                nextValues);
        }
    }

    int[] stringToIntAttributes(final String voInstance, final String[] attrs)
    {

        final String voTypeName = CustomAmFixture.VOI_TYPE_MAP.get(voInstance);
        final List<String> rowAttrs = CustomAmFixture.VOT_ATTRLST_MAP
            .get(voTypeName);
        final int[] retval = new int[attrs.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = rowAttrs.indexOf(attrs[i]);
        }
        return retval;
    }

    /**
     * Make calls to ViewObject.isExecuted return true for ALL view objects
     * under the application module.
     * 
     */
    public void setAllViewObjectExecuted()
    {
        for (final String nextVoInst : VOI_MOCKVO_MAP.keySet()) {
            setViewObjectExecuted(nextVoInst);
        }
    }

    /**
     * Make calls to ViewObject.isExecuted return true for the given view object
     * instance..
     */
    public void setViewObjectExecuted(final String voInstance)
    {
        final ViewObject mockVo = CustomAmFixture.VOI_MOCKVO_MAP
            .get(voInstance);
        Mockito.when(mockVo.isExecuted()).thenReturn(true);
    }


    /**
     * Note this can be very slow. 250~ ms. Mock selectively using
     * #mockVoInstance(OAApplicationModuleImpl, String) for better performance.
     * 
     * @param appModule Mock application module, not spy.
     */
    public void mockViewObjects(final OAApplicationModuleImpl appModule)
    {
        getLogger()
            .warn(
                "Mocking all view objects.  Consider mocking only the needed view objects.");
        for (final String nextVoInst : VOI_VOCLS_MAP.keySet()) {
            mockVoInstance(appModule, nextVoInst);
        }
    }

    /**
     * @param appModule Mock application module, not spy.
     * @param voInstance view object instance.
     */
    public void mockVoInstance(final OAApplicationModuleImpl appModule,
            final String voInstance)
    {
        final Class<? extends ViewObject> voType = VOI_VOCLS_MAP
            .get(voInstance);
        if (voType != null) {
            final ViewObject mockVo = Mockito.mock(voType);
            Mockito.when(mockVo.getName()).thenReturn(voInstance);
            Mockito.when(mockVo.getFullName()).thenReturn(
                "Mock Full Name" + voInstance);


            Mockito.doReturn(appModule).when(mockVo).getApplicationModule();

            // Mock setCurrentRow(Row).
            Mockito.doAnswer(new Answer<Object>() {
                @Override
                public Object answer(final InvocationOnMock invocation)
                        throws Throwable
                {
                    final Row row = (Row) invocation.getArguments()[0];
                    CustomAmFixture.VOI_CURROW_MAP.put(voInstance, row);
                    return null;
                }
            })
                .when(mockVo)
                .setCurrentRow((Row) Matchers.any());

            // Mock getCurrentRow().
            Mockito.doAnswer(new Answer<Row>() {
                @Override
                public Row answer(final InvocationOnMock invocation)
                        throws Throwable
                {
                    return CustomAmFixture.VOI_CURROW_MAP.get(voInstance);
                }
            })
                .when(mockVo)
                .getCurrentRow();


            CustomAmFixture.VOI_MOCKVO_MAP.put(voInstance, mockVo);
            //Mock getFinFacilityVO1.
            final String methName = "get" + voInstance;
            Mockito.when(invokeMethod(appModule, methName)).thenReturn(mockVo);

            //Mock findViewObject(String).            
            Mockito
                .when(appModule.findViewObject(Matchers.anyString()))
                .thenAnswer(new Answer<ViewObject>() {
                    @Override
                    public ViewObject answer(final InvocationOnMock invocation)
                            throws Throwable
                    {
                        final String voInstance = (String) invocation
                            .getArguments()[0];
                        return CustomAmFixture.VOI_MOCKVO_MAP.get(voInstance);
                    }
                });

            Mockito
                .when(mockVo.createRowSetIterator(Matchers.anyString()))
                .thenAnswer(new Answer<RowSetIterator>() {

                    @Override
                    public RowSetIterator answer(
                            final InvocationOnMock invocation) throws Throwable
                    {
                        //final String arg = (String) invocation.getArguments()[0];
                        final RowSetIterator mockIter = Mockito
                            .mock(RowSetIterator.class);

                        Mockito
                            .when(
                                mockIter.getRowAtRangeIndex(Matchers.anyInt()))
                            .thenAnswer(new Answer<Row>() {

                                @Override
                                public Row answer(
                                        final InvocationOnMock invocation)
                                        throws Throwable
                                {
                                    final int index = (Integer) invocation
                                        .getArguments()[0];
                                    final List<Row> mockRowList = CustomAmFixture.VOI_MROWLST_MAP
                                        .get(voInstance);
                                    Row retval = null; //NOPMD: null default, conditionally redefine.
                                    if (index < mockRowList.size()) {
                                        retval = mockRowList.get(index);
                                    }
                                    return retval;
                                }
                            });


                        return mockIter;
                    }
                });

            //Mock first().
            Mockito.when(mockVo.first()).thenAnswer(new Answer<Row>() {
                @Override
                public Row answer(final InvocationOnMock invocation)
                        throws Throwable
                {
                    final ViewObject viewObject = (ViewObject) invocation
                        .getMock();
                    final String voInstance = viewObject.getName();
                    final List<Row> mockRowList = CustomAmFixture.VOI_MROWLST_MAP
                        .get(voInstance);
                    if (mockRowList == null) {
                        return null;
                    } else if (mockRowList.size() > 0) {
                        return mockRowList.get(0);
                    } else {
                        return null;
                    }
                }

            });

            // Mock getRowAtRangeIndex(int).
            Mockito
                .when(mockVo.getRowAtRangeIndex(Matchers.anyInt()))
                .thenAnswer(new Answer<Row>() {
                    @Override
                    public Row answer(final InvocationOnMock invocation)
                            throws Throwable
                    {
                        final ViewObject viewObject = (ViewObject) invocation
                            .getMock();
                        final String voInstance = viewObject.getName();
                        final int rowIndex = (Integer) invocation
                            .getArguments()[0];
                        final List<Row> mockRowList = CustomAmFixture.VOI_MROWLST_MAP
                            .get(voInstance);
                        return mockRowList.get(rowIndex);
                    }
                });

            final Answer<Row[]> ansRetAllRow = new Answer<Row[]>() {
                @Override
                public Row[] answer(final InvocationOnMock invocation)
                        throws Throwable
                {
                    final RowSet mockViewObject = (RowSet) invocation.getMock();
                    final String voInstance = mockViewObject.getName();
                    final List<Row> rowList = CustomAmFixture.VOI_MROWLST_MAP
                        .get(voInstance);
                    Row[] retval;
                    if (rowList == null) {
                        retval = new Row[0];
                    } else {
                        retval = rowList.toArray(new Row[rowList.size()]);
                    }
                    return retval;
                }
            };

            final RowSet mockRowSet = Mockito.mock(RowSet.class);
            Mockito.when(mockVo.getRowSet()).thenReturn(mockRowSet);
            Mockito.when(mockRowSet.getAllRowsInRange()).thenAnswer(
                ansRetAllRow);
            Mockito.when(mockRowSet.getName()).thenReturn(voInstance);

            // Mock getAllRowsInRange().
            Mockito.when(mockVo.getAllRowsInRange()).thenAnswer(ansRetAllRow);

            Mockito.when(mockVo.getRowCount()).then(new Answer<Integer>() {

                @Override
                public Integer answer(final InvocationOnMock invocation)
                        throws Throwable
                {
                    final List<Row> rowList = CustomAmFixture.VOI_MROWLST_MAP
                        .get(voInstance);
                    int retval;
                    if (rowList == null) {
                        retval = 0;
                    } else {
                        retval = rowList.size();
                    }
                    return retval;

                }
            });

            //Mockito.when(mockVo.getFilteredRows(Matchers.eq("NameXXX"), Matchers.any())).thenAnswer(ansRetAllRow);

            Mockito.doAnswer(new Answer<Row[]>() {
                @Override
                public Row[] answer(final InvocationOnMock invocation)
                        throws Throwable
                {
                    final String paramOne = (String) invocation.getArguments()[0];
                    if ("NameXXX".equals(paramOne)) {
                        //WET
                        final ViewObject mockViewObject = (ViewObject) invocation
                            .getMock();
                        final String voInstance = mockViewObject.getName();
                        final List<Row> rowList = CustomAmFixture.VOI_MROWLST_MAP
                            .get(voInstance);
                        Row[] retval = new Row[0];
                        if (rowList != null) {
                            retval = rowList.toArray(new Row[0]);
                        }
                        return retval;
                    } else {
                        final Object paramTwo = invocation.getArguments()[1];
                        final List<Row> retval = new ArrayList<Row>();

                        final ViewObject mockViewObject = (ViewObject) invocation
                            .getMock();
                        final String voInstance = mockViewObject.getName();
                        final List<Row> rowList = CustomAmFixture.VOI_MROWLST_MAP
                            .get(voInstance);
                        for (final Row row : rowList) {
                            if (paramTwo != null
                                    && paramTwo.equals(row
                                        .getAttribute(paramOne))) {
                                retval.add(row);
                            }
                        }
                        return retval.toArray(new Row[retval.size()]);
                    }
                }
            })
                .when(mockVo)
                .getFilteredRows(Matchers.anyString(), Matchers.any());


            Mockito.when(mockVo.getAttributeDef(Matchers.anyInt())).thenAnswer(
                new Answer<AttributeDef>() {
                    @Override
                    public AttributeDef answer(final InvocationOnMock invocation)
                            throws Throwable
                    {
                        final int attrIdx = (Integer) invocation.getArguments()[0];
                        final String voTypeName = CustomAmFixture.VOI_TYPE_MAP
                            .get(voInstance);
                        final List<String> attrList = CustomAmFixture.VOT_ATTRLST_MAP
                            .get(voTypeName);
                        final AttributeDef mockAttrDef = Mockito
                            .mock(AttributeDef.class);
                        Mockito.when(mockAttrDef.getName()).thenReturn(
                            attrList.get(attrIdx));
                        return mockAttrDef;
                    }
                });


            Mockito.when(mockVo.getAttributeCount()).thenAnswer(
                new Answer<Integer>() {
                    @Override
                    public Integer answer(final InvocationOnMock invocation)
                            throws Throwable
                    {
                        final String voTypeName = CustomAmFixture.VOI_TYPE_MAP
                            .get(voInstance);
                        final List<String> attrList = CustomAmFixture.VOT_ATTRLST_MAP
                            .get(voTypeName);
                        return attrList.size();
                    }
                });

            Mockito
                .when(mockVo.getAttributeIndexOf(Matchers.anyString()))
                .thenAnswer(new Answer<Integer>() {
                    @Override
                    public Integer answer(final InvocationOnMock invocation)
                            throws Throwable
                    {
                        final String attrName = (String) invocation
                            .getArguments()[0];
                        final String voTypeName = CustomAmFixture.VOI_TYPE_MAP
                            .get(voInstance);
                        final List<String> attrList = CustomAmFixture.VOT_ATTRLST_MAP
                            .get(voTypeName);
                        return attrList.indexOf(attrName);
                    }
                });

        }
    }

    /**
     * 
     * @param pAppModuleDef
     * @param parentInstName Parent application module instance name.
     */
    @SuppressWarnings("unchecked")
    private final void processAppModule(final String pAppModuleDef,
            final String parentInstName)
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final String path = "/" + pAppModuleDef.replaceAll("\\.", "/") + ".xml";
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            ignoreDtd(docBuilder);
            getLogger().info("App Def Filename: " + path);

            final Document document = docBuilder.parse(this
                .getClass()
                .getResourceAsStream(path));
            final Element root = document.getDocumentElement();
            final String amType = root
                .getAttribute(Attribute.OBJECT_IMPL_CLASS);
            AMDEF_CLS_MAP.put(
                pAppModuleDef,
                (Class<? extends OAApplicationModuleImpl>) Class
                    .forName(amType));
            processAmRootNode(root, parentInstName);
        } catch (final Exception exception) {
            getLogger().error(exception);
        }

    }

    @SuppressWarnings("unchecked")
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

                    final Class<?>[] voAndRowType = getVoAndRowType(
                        voInstName,
                        voDef);

                    //TODO: Unconventional ViewObject implementation through inheritance is not supported.
                    if (voAndRowType != null) {
                        CustomAmFixture.VOI_VOCLS_MAP.put(
                            voInstName,
                            (Class<? extends ViewObject>) voAndRowType[0]);
                        CustomAmFixture.VOI_ROWCLS_MAP.put(
                            voInstName,
                            (Class<? extends Row>) voAndRowType[1]);

                    }


                }
            }
        }
    }

    Class<?>[] getVoAndRowType(final String voInstName, final String voDef)
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Class<?>[] retval = null; //NOPMD: null default, conditionally redefine. 
        final String appDefFilename = "/" + voDef.replaceAll("\\.", "/")
                + ".xml";
        String implClass = null; //NOPMD: null default, conditionally redefine.
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            ignoreDtd(docBuilder);

            final Document document = docBuilder.parse(this
                .getClass()
                .getResourceAsStream(appDefFilename));
            final Element root = document.getDocumentElement();

            @SuppressWarnings("unchecked")
            final Class<? extends Row> rowClass = (Class<? extends Row>) Class
                .forName(root.getAttribute(Attribute.VOROW_IMPL));

            implClass = root.getAttribute(Attribute.OBJECT_IMPL_CLASS);
            retval = new Class[] {
                    Class.forName(implClass),
                    rowClass };

            final String voTypeName = root.getAttribute(Attribute.NAME);
            CustomAmFixture.VOI_TYPE_MAP.put(voInstName, voTypeName);
            processVoRootNode(voTypeName, root);
        } catch (final Exception exception) {
            getLogger().error(
                "Error on voInstName[" + voInstName + "], impl class: "
                        + implClass,
                exception);
        }
        return retval;
    }

    /**
     * To use later when reading attribute list from VO.xml.
     * 
     * @param root
     * @return
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
            CustomAmFixture.VOT_ATTRLST_MAP.put(voTypeName, attrNames);
        }
    }

    /**
     * This will prevent the DocumentBuilder from validating the DTD. Saves us
     * the trouble of dependence to online DTD resource.
     * 
     * @param docBuilder DocumentBuilder isntance.
     */
    private void ignoreDtd(final DocumentBuilder docBuilder)
    {
        docBuilder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(final String publicId,
                    final String systemId) throws SAXException, IOException
            {
                InputSource retval = null;
                // redefine.
                if (systemId.contains("jbo_03_01.dtd")) {
                    retval = new InputSource(new StringReader(""));
                }
                return retval;
            }
        });
    }

    /**
     * Helper method to swallow exception from reflection. WET: With who!?
     * 
     * @param object
     */
    protected Object invokeMethod(final Object object, final String methName)
    {
        Object retval = null; //NOPMD: null default, conditionally redefine.
        try {
            retval = object
                .getClass()
                .getMethod(methName, new Class[0])
                .invoke(object, new Object[0]);
        } catch (final Exception e) {
            getLogger().error(e);
        }
        return retval;
    }


    public OAApplicationModuleImpl getMockAppModule()
    {
        return this.mockAppModule;
    }

    /** Known node names. */
    public static class NodeName {

        final static String VIEW_OBJECT = "ViewUsage";
        final static String VIEW_LINK = "ViewLinkUsage";
        final static String APPMODULE = "AppModuleUsage";
        final static String ROW_ATTR = "ViewAttribute";
    }


    /** Known attributes. */
    public static class Attribute {

        final static String NAME = "Name";
        final static String VO_DEF = "ViewObjectName";
        final static String OBJECT_IMPL_CLASS = "ComponentClass";

        // ViewObject specific.
        final static String VOROW_IMPL = "RowClass";

    }

    /**
     * @param voInstance
     */
    public ViewObject getMockViewObject(final String voInstance)
    {
        return VOI_MOCKVO_MAP.get(voInstance);
    }

    /** @return custom logger instance. */
    public OafLogger getLogger()
    {
        return OafLogger.getInstance();
    }

}
