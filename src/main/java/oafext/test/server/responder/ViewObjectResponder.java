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
package oafext.test.server.responder;

import oafext.test.server.AppModuleFixture;
import oafext.test.server.BaseViewObjectMocker;
import oracle.jbo.Row;
import oracle.jbo.server.ViewObjectImpl;

/**
 * This mocks more complex behavior using Answer class.<br/>
 * Convention: Mock View object is always the first parameter if present.
 *
 * @author royce
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface ViewObjectResponder<M extends ViewObjectImpl> {


    void mockMethods(final AppModuleFixture<?> amFixture,
                     final BaseViewObjectMocker voMocker);

    M mockCreateRow(final AppModuleFixture<?> amFixture,
                    final BaseViewObjectMocker voMocker);


    /**
     * @param voMocker
     */
    M mockCreateRowSetIterator(final BaseViewObjectMocker voMocker);


    /**
     * @param voMocker
     */
    M mockExecuteQuery(final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     */
    M mockFirst(final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     * @param BaseViewObjectMocker
     */
    M mockGetAllRowsInRange(final BaseViewObjectMocker voMocker);

    /** */
    M mockGetAttributeCount(final AppModuleFixture<?> amFixture,
                            final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     * @param BaseViewObjectMocker
     */
    M mockGetAttributeDef(final AppModuleFixture<?> amFixture,
                          final BaseViewObjectMocker voMocker);


    /** */
    M mockGetAttributeIndexOf(final AppModuleFixture<?> amFixture,
                              final BaseViewObjectMocker voMocker);


    /**
     * @param voMocker view object mocker.
     * @param currentRow row to set as current.
     * @return
     */
    M mockGetCurrentRow(final BaseViewObjectMocker voMocker,
                        final Row currentRow);

    /**
     * @param mockVo
     * @param BaseViewObjectMocker
     */
    M mockGetRowAtRangeIndex(final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     */
    M mockGetRowCount(final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     * @param BaseViewObjectMocker
     */
    M mockIsExecuted(final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     */
    M mockSetCurrentRow(final BaseViewObjectMocker voMocker);

    /** */
    M mockInsertRow(final BaseViewObjectMocker voMocker);

    /** */
    M mockInsertRowAtRangeIndex(final BaseViewObjectMocker voMocker);

    /** */
    M mockSetRangeSize(final BaseViewObjectMocker voMocker);
}
