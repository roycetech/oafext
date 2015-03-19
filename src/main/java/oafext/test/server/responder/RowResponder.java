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

import java.util.List;

import oafext.test.server.AppModuleFixture;
import oafext.test.server.RowMocker;
import oracle.jbo.Row;

/**
 * @author royce
 *
 * @param <M> specific row implementation type.
 */
public interface RowResponder<M extends Row> {


    /**
     * rtfc.
     *
     * @param amFixture application module code fixture.
     * @param rowMocker row mocker.
     * @param pRowClass row class.
     */
    void mockMethods(final AppModuleFixture<?> amFixture,
                     final RowMocker rowMocker,
                     final Class<? extends Row> pRowClass);


    /**
     * Circumvent final method.
     *
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     * @return
     */
    void mockGetAttributeCount(final List<String> attrList,
                               final RowMocker rowMocker);

    /**
     * Circumvent final method.
     *
     * @param rowMocker row mocker.
     */
    void mockGetViewObj(final RowMocker rowMocker);


    /**
     * rtfc.
     *
     * @param rowMocker row mocker.
     */
    M mockRemove(final RowMocker rowMocker);

    /**
     * rtfc.
     *
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     * @return
     */
    M mockGetAttributeInt(final List<String> attrList, final RowMocker rowMocker);

    /**
     * rtfc.
     *
     * @param rowMocker row mocker.
     */
    M mockGetAttributeString(final RowMocker rowMocker);


    /**
     * rtfc.
     *
     * @param rowMocker row mocker.
     */
    M mockGetKey(final RowMocker rowMocker);

    /**
     * rtfc.
     *
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     */
    M mockSetAttributeInt(final List<String> attrList, final RowMocker rowMocker);

    /**
     * rtfc.
     *
     * @param rowMocker row mocker.
     */
    M mockSetAttributeString(final RowMocker rowMocker);

    /**
     * rtfc.
     *
     * @param rowClass row class.
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     */
    void mockSetter(Class<? extends Row> rowClass, final List<String> attrList,
                    final RowMocker rowMocker);

    /**
     * rtfc.
     *
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     */
    void mockGetter(final List<String> attrList, final RowMocker rowMocker);

}
