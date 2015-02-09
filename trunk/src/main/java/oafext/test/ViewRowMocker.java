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
package oafext.test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import oafext.logging.OafLogger;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 * Helper class for CustomAmFixture.
 * 
 * <pre>
 * $Author$ 
 * $Date$ 
 * $HeadURL$
 * </pre>
 * 
 * @author royce.com
 */
public class ViewRowMocker {


    /** Standard Oracle versioning. */
    public static final String RCS_ID = "$Revision$";


    /** Wrapper for the final method getViewObject. */
    public static final String METH_WRAPPED_GET_VO = "getViewObj";

    public void mockRow(final Row mockRow)
    {

        Method getViewObj = null; //NOPMD: null default, conditionally redefine. 
        try {
            getViewObj = mockRow.getClass().getDeclaredMethod(
                METH_WRAPPED_GET_VO,
                new Class<?>[0]);
        } catch (final SecurityException e1) {
            getLogger().error(e1);
        } catch (final NoSuchMethodException e1) {
            getLogger().error(
                "Anti zombie method not found for: "
                        + mockRow.getClass().getSimpleName());
        }

        if (getViewObj != null) {

            try {
                Mockito.doAnswer(new Answer<ViewObject>() {


                    public ViewObject answer(final InvocationOnMock invocation)
                            throws Throwable
                    {
                        final String voInstance = CustomAmFixture.MROW_VOI_MAP
                            .get(invocation.getMock());
                        return CustomAmFixture.VOI_MOCKVO_MAP.get(voInstance);
                    }
                })
                    .when(mockRow)
                    .getClass()
                    .getDeclaredMethod(METH_WRAPPED_GET_VO, new Class<?>[0])
                    .invoke(mockRow, new Object[0]);
            } catch (final Exception e) {
                getLogger().error(e);
            }
        }


        Mockito.doAnswer(new Answer<Key>() {


            public Key answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Key key = Mockito.spy(new Key(new Object[] { mockRow
                    .getAttribute(0) }));
                return key;
            }
        })
            .when(mockRow)
            .getKey();

        Mockito.doAnswer(new Answer<Object>() {


            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String voInstance = CustomAmFixture.MROW_VOI_MAP
                    .get(invocation.getMock());
                final List<Row> voMockRows = CustomAmFixture.VOI_MROWLST_MAP
                    .get(voInstance);
                final int rowIdx = voMockRows.indexOf(invocation.getMock());
                final Map<Integer, Object> rowClone = CustomAmFixture.VOI_RCLN_MAP
                    .get(voInstance)
                    .get(rowIdx);

                final Integer index = (Integer) invocation.getArguments()[0];
                final Object value = invocation.getArguments()[1];
                rowClone.put(index, value);
                return null;
            }
        })
            .when(mockRow)
            .setAttribute(Matchers.anyInt(), Matchers.any());

        Mockito.when(mockRow.getAttribute(Matchers.anyString())).thenAnswer(
            new Answer<Object>() {


                public Object answer(final InvocationOnMock invocation)
                        throws Throwable
                {
                    final String voInstance = CustomAmFixture.MROW_VOI_MAP
                        .get(invocation.getMock());
                    final String voTypeName = CustomAmFixture.VOI_TYPE_MAP
                        .get(voInstance);

                    final List<Row> voMockRows = CustomAmFixture.VOI_MROWLST_MAP
                        .get(voInstance);
                    final int rowIdx = voMockRows.indexOf(invocation.getMock());

                    final Map<Integer, Object> rowClone = CustomAmFixture.VOI_RCLN_MAP
                        .get(voInstance)
                        .get(rowIdx);
                    final List<String> rowAttrs = CustomAmFixture.VOT_ATTRLST_MAP
                        .get(voTypeName);

                    final String attrParam = (String) invocation.getArguments()[0];
                    final int attrIdx = rowAttrs.indexOf(attrParam);
                    return rowClone.get(attrIdx);
                }
            });

        Mockito.when(mockRow.getAttribute(Matchers.anyInt())).thenAnswer(
            new Answer<Object>() {


                public Object answer(final InvocationOnMock invocation)
                        throws Throwable
                {
                    final String voInstance = CustomAmFixture.MROW_VOI_MAP
                        .get(invocation.getMock());
                    final List<Row> voMockRows = CustomAmFixture.VOI_MROWLST_MAP
                        .get(voInstance);
                    final int rowIdx = voMockRows.indexOf(invocation.getMock());

                    final Map<Integer, Object> rowClone = CustomAmFixture.VOI_RCLN_MAP
                        .get(voInstance)
                        .get(rowIdx);
                    final int attrIdx = (Integer) invocation.getArguments()[0];
                    return rowClone.get(attrIdx);
                }
            });

    }

    /**
     * Helper method to swallow exception from reflection. WET: With
     * CustomAmFixture and another.?
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

    /** @return custom logger instance. */
    public OafLogger getLogger()
    {
        return OafLogger.getInstance();
    }

    /**
     * @see {@link Object#toString()}
     * @return String representation of this instance.
     */

    @Override
    public String toString()
    {
        return super.toString() + " " + RCS_ID;
    }

}