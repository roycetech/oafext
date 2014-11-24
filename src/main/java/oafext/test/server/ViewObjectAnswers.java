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

import java.util.ArrayList;
import java.util.List;

import oafext.Constant;
import oracle.jbo.AttributeDef;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ViewObjectImpl;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mock View object is always the first parameter.
 * 
 * @author royce
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class ViewObjectAnswers {


    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops" /* FP: */)
    private static List<AttrDefMocker> initAttributeList(final String voInstName,
                                                         final AppModuleFixture<?> amFixture)
    {
        final String voType = amFixture.getVoNameDefMap().get(voInstName);
        assert voType != null;

        final AppModuleMocker appModuleMocker = amFixture.getAppModuleMocker();
        final List<AttrDefMocker> attrDefMockerList = appModuleMocker
            .getAttrDefMockerMap()
            .get(voType);
        assert attrDefMockerList != null;

        if (attrDefMockerList.isEmpty()) {
            final List<String> attrList = amFixture.getVoDefAttrListMap().get(
                voInstName);
            for (final String string : attrList) {
                attrDefMockerList.add(new AttrDefMocker(string));
            }
        }
        return attrDefMockerList;

    }

    static <M extends ViewObjectImpl> M mockCreateRow(final M mockVo,
                                                      final AppModuleFixture<?> amFixture,
                                                      final ViewObjectMocker viewObjectMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                @SuppressWarnings(Constant.UNCHECKED)
                final Class<? extends Row> rowClass = mockVo.getRowClass();
                final RowMocker rowMocker = new RowMocker(
                    mockVo,
                    rowClass,
                    amFixture);

                viewObjectMocker.getNewRowsMap().put(
                    rowMocker.getMockRow(),
                    rowMocker);

                return rowMocker.getMockRow();
            }
        }).when(mockVo);
    }

    /**
     * @param mockVo
     * @param viewObjectMocker
     * @return
     */
    static <M extends ViewObjectImpl> M mockCreateRowSetIterator(final M mockVo,
                                                                 final ViewObjectMocker viewObjectMocker)
    {
        return Mockito.doAnswer(new Answer<RowSetIterator>() {

            @Override
            public RowSetIterator answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String iterName = invocation.getArguments()[0].toString();
                assert iterName != null;
                assert viewObjectMocker.getRowSetIterMap().get(iterName) == null;

                final RowSetIteratorMocker rsIterMocker = new RowSetIteratorMocker(
                    iterName,
                    viewObjectMocker);
                viewObjectMocker.getRowSetIterMap().put(iterName, rsIterMocker);

                return rsIterMocker.getMockRsIter();
            }
        })
            .when(mockVo);
    }


    /**
     * @param mockVo
     * @param viewObjectMocker
     */
    static <M> M mockExecuteQuery(final M mockVo,
                                  final ViewObjectMocker viewObjectMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                viewObjectMocker.setExecuted(true);
                return null;
            }
        }).when(mockVo);
    }

    /**
     * @param mockVo
     */
    static <M> M mockFirst(final M mockVo, final ViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<RowMocker> rowMockerList = voMocker
                    .getRowMockerList();

                if (rowMockerList.isEmpty()) {
                    return null;
                } else {
                    final Row firstRow = rowMockerList.get(0).getMockRow();
                    voMocker.setRowPointer(firstRow);
                    return firstRow;
                }
            }
        }).when(mockVo);
    }


    /**
     * @param mockVo
     * @param viewObjectMocker
     */
    static <M> M mockGetAllRowsInRange(final M mockVo,
                                       final ViewObjectMocker viewObjectMocker)
    {
        return Mockito.doAnswer(new Answer<Row[]>() {

            @Override
            public Row[] answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<Row> retval = new ArrayList<Row>();
                for (int i = viewObjectMocker.getRangeStart(); i < viewObjectMocker
                    .getRangeSize(); i++) {
                    retval.add(viewObjectMocker
                        .getRowMockerList()
                        .get(i)
                        .getMockRow());
                }
                return retval.toArray(new Row[retval.size()]);
            }
        })
            .when(mockVo);
    }


    static <M extends ViewObject> M mockGetAttributeCount(final M mockVo,
                                                          final AppModuleFixture<?> amFixture)
    {

        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<AttrDefMocker> attrDefMockerList = initAttributeList(
                    mockVo.getName(),
                    amFixture);

                return attrDefMockerList.size();
            }
        })
            .when(mockVo);
    }

    /**
     * @param mockVo
     * @param viewObjectMocker
     */
    static <M extends ViewObject> M mockGetAttributeDef(final M mockVo,
                                                        final AppModuleFixture<?> amFixture)
    {

        return Mockito.doAnswer(new Answer<AttributeDef>() {

            @Override
            public AttributeDef answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final List<AttrDefMocker> attrDefMockerList = initAttributeList(
                    mockVo.getName(),
                    amFixture);

                final Integer index = (Integer) invocation.getArguments()[0];
                assert index > -1;
                assert index < attrDefMockerList.size();
                return attrDefMockerList.get(index).getMockAttrDef();
            }
        })
            .when(mockVo);
    }

    static <M extends ViewObject> M mockGetAttributeIndexOf(final M mockVo,
                                                            final AppModuleFixture<?> amFixture)
    {
        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String attrName = (String) invocation.getArguments()[0];
                return amFixture
                    .getVoDefAttrListMap()
                    .get(mockVo.getName())
                    .indexOf(attrName);
            }
        }).when(mockVo);
    }


    static <M> M mockGetCurrentRow(final M mockVo, final Row currentRow)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return currentRow;
            }
        }).when(mockVo);
    }


    /**
     * @param mockVo
     * @param viewObjectMocker
     */
    static <M> M mockGetRowAtRangeIndex(final M mockVo,
                                        final ViewObjectMocker viewObjectMocker)
    {
        return Mockito.doAnswer(new Answer<Row>() {

            @Override
            public Row answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer index = (Integer) invocation.getArguments()[0];
                return viewObjectMocker
                    .getRowMockerList()
                    .get(index)
                    .getMockRow();
            }
        }).when(mockVo);

    }

    /**
     * @param mockVo
     */
    static <M> M mockGetRowCount(final M mockVo, final ViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return voMocker.getRowMockerList().size();
            }
        }).when(mockVo);
    }

    /**
     * @param mockVo
     * @param viewObjectMocker
     */
    static <M> M mockIsExecuted(final M mockVo,
                                final ViewObjectMocker viewObjectMocker)
    {
        return Mockito.doAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return viewObjectMocker.isExecuted();
            }
        }).when(mockVo);
    }


    /**
     * @param mockVo
     */
    static <M> M mockSetCurrentRow(final M mockVo,
                                   final ViewObjectMocker voMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                voMocker.setCurrentRow((Row) invocation.getArguments()[0]);
                return null;
            }
        }).when(mockVo);
    }


    /** */
    static <M> M mockInsertRow(final M mockVo,
                               final ViewObjectMocker viewObjectMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Row mockRow = (Row) invocation.getArguments()[0];
                final RowMocker rowMocker = viewObjectMocker
                    .getNewRowsMap()
                    .remove(mockRow);

                viewObjectMocker.getRowMockerList().add(rowMocker);
                return null;
            }
        }).when(mockVo);
    }


    static <M> M mockInsertRowAtRangeIndex(final M mockVo,
                                           final ViewObjectMocker viewObjectMocker)
    {
        return Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final Integer index = (Integer) invocation.getArguments()[0];
                assert index <= viewObjectMocker.getRowMockerList().size();
                final Row mockRow = (Row) invocation.getArguments()[1];

                final RowMocker rowMocker = viewObjectMocker
                    .getNewRowsMap()
                    .remove(mockRow);

                final List<RowMocker> rowMockerList = viewObjectMocker
                    .getRowMockerList();
                rowMockerList.add(index, rowMocker);

                //                if (index == rowMockerList.size()) {
                //                    rowMockerList.add(rowMocker);
                //                } else {
                //                    rowMockerList.add(index, rowMocker);
                //                }
                return null;
            }
        }).when(mockVo);
    }


    /** */
    private ViewObjectAnswers() {}


}
