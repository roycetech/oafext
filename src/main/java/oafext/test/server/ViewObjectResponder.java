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

import oracle.jbo.Row;
import oracle.jbo.server.ViewObjectImpl;

/**
 * This mocks more complex behavior using Answer class.<br/>
 * Convention: Mock View object is always the first parameter if present.
 * 
 * @author royce
 */
@SuppressWarnings("PMD.TooManyMethods")
interface ViewObjectResponder<M extends ViewObjectImpl> {


    void mockMethods(final M mockVo, final AppModuleFixture<?> amFixture,
                  final BaseViewObjectMocker voMocker);

    M mockCreateRow(final M mockVo, final AppModuleFixture<?> amFixture,
                    final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     * @param BaseViewObjectMocker
     * @return
     */
    M mockCreateRowSetIterator(final M mockVo,
                               final BaseViewObjectMocker voMocker);


    /**
     * @param mockVo
     * @param BaseViewObjectMocker
     */
    M mockExecuteQuery(final M mockVo, final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     */
    M mockFirst(final M mockVo, final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     * @param BaseViewObjectMocker
     */
    M mockGetAllRowsInRange(final M mockVo,
                            final BaseViewObjectMocker voMocker);

    M mockGetAttributeCount(final M mockVo, final AppModuleFixture<?> amFixture);

    /**
     * @param mockVo
     * @param BaseViewObjectMocker
     */
    M mockGetAttributeDef(final M mockVo, final AppModuleFixture<?> amFixture);

    M mockGetAttributeIndexOf(final M mockVo,
                              final AppModuleFixture<?> amFixture);

    M mockGetCurrentRow(final M mockVo, final Row currentRow);

    /**
     * @param mockVo
     * @param BaseViewObjectMocker
     */
    M mockGetRowAtRangeIndex(final M mockVo,
                             final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     */
    M mockGetRowCount(final M mockVo, final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     * @param BaseViewObjectMocker
     */
    M mockIsExecuted(final M mockVo, final BaseViewObjectMocker voMocker);

    /**
     * @param mockVo
     */
    M mockSetCurrentRow(final M mockVo, final BaseViewObjectMocker voMocker);

    /** */
    M mockInsertRow(final M mockVo, final BaseViewObjectMocker voMocker);

    M mockInsertRowAtRangeIndex(final M mockVo,
                                final BaseViewObjectMocker voMocker);

    M mockSetRangeSize(final M mockVo, final BaseViewObjectMocker voMocker);
}
