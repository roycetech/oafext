package oafext.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import oafext.logging.OafLogger;
import oafext.test.util.MockHelper;
import oracle.apps.fnd.framework.webui.OAInnerDataObjectEnumerator;
import oracle.jbo.Row;
import oracle.jbo.RowSet;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ViewRowImpl;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ph.rye.common.lang.ObjectUtil;

/**
 * Fixture for Advanced table in detail of another advanced table.
 * 
 * TODO: Coupling with CustomAmFixture class.
 * 
 * @author royce.com
 */
public class InnerTableFixture {


    /** Common object utility. */
    private final transient ObjectUtil objUtil = new ObjectUtil();

    /** */
    private final transient MockHelper mockHelper = new MockHelper();

    private final Class<?> rowType;

    public InnerTableFixture(final Class<?> pRowType) {
        rowType = pRowType;

    }

    /**
     * OAInnerDataObjectEnumerator is represented in parallel as Map<String,
     * Object>.
     */
    static final Map<String, InnerEnumClone> INN_ENUM_CLN = new HashMap<String, InnerEnumClone>();

    static final Map<String, Integer> ENUM_IDX_MAP = new HashMap<String, Integer>();

    static final Map<String, String> INNERWEBID_VONAME = new HashMap<String, String>();

    public void setUp()
    {
        INN_ENUM_CLN.clear();
        ENUM_IDX_MAP.clear();
        INNERWEBID_VONAME.clear();
    }

    class InnerEnumClone extends HashMap<Integer, RowSetClone> {

        private final OAInnerDataObjectEnumerator mock = Mockito
                .mock(OAInnerDataObjectEnumerator.class);

        InnerEnumClone(final String webBeanId) {

            Mockito.doAnswer(new Answer<Boolean>() {


                public Boolean answer(final InvocationOnMock invocation)
                        throws Throwable
                {
                    final int currIndex = getObjUtil().mapGetInit(ENUM_IDX_MAP,
                            webBeanId, -1);

                    final int size = INN_ENUM_CLN.get(webBeanId).size();
                    return currIndex + 1 < size;
                }
            }).when(mock).hasMoreElements();

            Mockito.doAnswer(new Answer<RowSet>() {


                public RowSet answer(final InvocationOnMock invocation)
                        throws Throwable
                {
                    final int index = ENUM_IDX_MAP.get(webBeanId) + 1;
                    ENUM_IDX_MAP.put(webBeanId, index);
                    return get(index).getMock();
                }
            }).when(mock).nextElement();

        }

        public OAInnerDataObjectEnumerator getMock()
        {
            return mock;
        }

    }

    class RowSetClone extends LinkedHashMap<Integer, RowClone> {

        private final RowSet mock = Mockito.mock(RowSet.class);

        RowSetClone() {
            Mockito.doAnswer(new Answer<Row[]>() {


                public Row[] answer(final InvocationOnMock invocation)
                        throws Throwable
                {
                    final List<Row> retval = new ArrayList<Row>();
                    for (final RowClone nextRow : values()) {
                        retval.add(nextRow.getMock());
                    }
                    return retval.toArray(new Row[retval.size()]);
                }
            }).when(mock).getAllRowsInRange();
        }


        public RowSet getMock()
        {
            return mock;
        }
    }


    class RowClone extends HashMap<Integer, Object> {


        private final ViewRowImpl mock = (ViewRowImpl) Mockito.mock(rowType);

        RowClone(final String innerWebId) {


            Mockito.when(
                    getMockHelper().invokeMethod(mock, "getViewObj",
                            new Class<?>[0], new Object[0])).thenAnswer(
                    new Answer<ViewObject>() {


                        public ViewObject answer(
                                final InvocationOnMock invocation)
                                throws Throwable
                        {
                            final String voName = INNERWEBID_VONAME
                                    .get(innerWebId);
                            return CustomAmFixture.VOI_MOCKVO_MAP.get(voName);
                        }
                    });


            Mockito.when(
                    getMockHelper().invokeMethod(mock, "setAttributeInternal",
                            new Class[] {
                                    Integer.TYPE,
                                    Object.class }, new Object[] {
                                    Matchers.anyInt(),
                                    Matchers.any() })).thenAnswer(
                    new Answer<Object>() {


                        public Object answer(final InvocationOnMock invocation)
                                throws Throwable
                        {
                            final Integer attribute = (Integer) invocation
                                    .getArguments()[0];
                            final Object value = invocation.getArguments()[1];
                            put(attribute, value);
                            return null;
                        }
                    });

            Mockito.when(
                    getMockHelper().invokeMethod(mock, "getAttributeInternal",
                            new Class[] { Integer.TYPE },
                            new Object[] { Matchers.anyInt() })).thenAnswer(
                    new Answer<Object>() {


                        public Object answer(final InvocationOnMock invocation)
                                throws Throwable
                        {
                            final Integer attribute = (Integer) invocation
                                    .getArguments()[0];
                            get(attribute);
                            return null;
                        }
                    });


            Mockito.when(
                    getMockHelper().invokeMethod(mock, "getAttribute",
                            new Class[] { String.class },
                            new Object[] { Matchers.anyString() })).thenAnswer(
                    new Answer<Object>() {


                        public Object answer(final InvocationOnMock invocation)
                                throws Throwable
                        {
                            final String attribute = (String) invocation
                                    .getArguments()[0];


                            final String voTypeName = CustomAmFixture.VOI_TYPE_MAP
                                    .get(INNERWEBID_VONAME.get(innerWebId));
                            final List<String> rowAttrs = CustomAmFixture.VOT_ATTRLST_MAP
                                    .get(voTypeName);
                            final int index = rowAttrs.indexOf(attribute);
                            return get(index);
                        }
                    });


        }
        public Row getMock()
        {
            return mock;
        }

    }


    /**
     * 
     * 
     * 
     * @param viewObject mock view object coming from mock application module.
     * @param index row index to modify. This can be a non existent index as
     *            long as it is number of rows + 1.
     * @param pAttr attribute index to modify. Use VORowImpl constant as
     *            Parameter. Avoid using magic numbers.
     * @param pValue Value of the attribute.
     */
    public void initVORowAtIndex(final String innerTable,
            final int rowsetIndex, final int index, final int pAttr,
            final Object pValue)
    {
        initVORowAtIndex(innerTable, rowsetIndex, index, new int[] { pAttr },
                new Object[] { pValue });
    }


    /**
     * TODO: Missing Row impl, will use
     * {@link #initVORow(String, String[], List)} for the meantime in fixture.
     * 
     * @param viewObject mock view object coming from mock application module.
     * @param index row index to modify.
     * @param pAttrs array of attribute indexes.
     * @param pValues array of values to set.
     */
    public void initVORowAtIndex(final String innerTable,
            final int rowSetIndex, final int index, final int[] pAttrs,
            final Object[] pValues)
    {
        final InnerEnumClone innerEnum = getObjUtil().mapGetInit(INN_ENUM_CLN,
                innerTable, new InnerEnumClone(innerTable));

        final RowSetClone rowSetClone = getObjUtil().mapGetInit(innerEnum,
                rowSetIndex, new RowSetClone());

        final RowClone rowClone = getObjUtil().mapGetInit(rowSetClone, index,
                new RowClone(innerTable));
        rowSetClone.put(index, rowClone);

        for (int i = 0; i < pValues.length; i++) {
            rowClone.put(pAttrs[i], pValues[i]);
        }
    }

    public OAInnerDataObjectEnumerator getDOE(final String webBeanId,
            final String voName)
    {
        INNERWEBID_VONAME.put(webBeanId, voName);

        ENUM_IDX_MAP.put(webBeanId, -1);
        return INN_ENUM_CLN.get(webBeanId).getMock();
    }


    /** @return custom logger instance. */
    public OafLogger getLogger()
    {
        return OafLogger.getInstance();
    }

    public ObjectUtil getObjUtil()
    {
        return objUtil;
    }


    public MockHelper getMockHelper()
    {
        return mockHelper;
    }


}
