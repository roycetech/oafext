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
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

/**
 * @author royce
 *
 * @param <M> specific row implementation type.
 * @param <V> View Object type.
 */
@SuppressWarnings("PMD.TooManyMethods")
/* Dependent on existing Oracle design. */
public interface RowResponder<M extends ViewRowImpl, V extends ViewObjectImpl> {


    /**
     * rtfc.
     *
     * @param amFixture application module code fixture.
     * @param rowMocker row mocker.
     * @param pRowClass row class.
     */
    void mockMethods(final AppModuleFixture<?> amFixture,
                     final RowMocker<M, V> rowMocker, final Class<M> pRowClass);


    /**
     * Circumvent final method.
     *
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     * @return
     */
    void mockGetAttributeCount(final List<String> attrList,
                               final RowMocker<M, V> rowMocker);

    /**
     * Circumvent final method.
     *
     * @param rowMocker row mocker.
     */
    void mockGetViewObj(final RowMocker<M, V> rowMocker);


    /**
     * rtfc.
     *
     * @param rowMocker row mocker.
     */
    M mockRemove(final RowMocker<M, V> rowMocker);

    /**
     * rtfc.
     *
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     * @return
     */
    M mockGetAttributeInt(final List<String> attrList,
                          final RowMocker<M, V> rowMocker);

    /**
     * rtfc.
     *
     * @param rowMocker row mocker.
     */
    M mockGetAttributeString(final RowMocker<M, V> rowMocker);


    /**
     * rtfc.
     *
     * @param rowMocker row mocker.
     */
    M mockGetKey(final RowMocker<M, V> rowMocker);

    /**
     * rtfc.
     *
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     */
    M mockSetAttributeInt(final List<String> attrList,
                          final RowMocker<M, V> rowMocker);

    /**
     * rtfc.
     *
     * @param rowMocker row mocker.
     */
    M mockSetAttributeString(final RowMocker<M, V> rowMocker);

    /**
     * rtfc.
     *
     * @param rowMocker row mocker.
     */
    M mockIsDead(final RowMocker<M, V> rowMocker);

    /**
     * rtfc.
     *
     * @param rowClass row class.
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     */
    void mockSetter(Class<M> rowClass, final List<String> attrList,
                    final RowMocker<M, V> rowMocker);

    /**
     * rtfc.
     *
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     */
    void mockGetter(final List<String> attrList, final RowMocker<M, V> rowMocker);

    /**
     * rtfc.
     *
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     */
    M mockGetAttributeNames(final List<String> attrList,
                            final RowMocker<M, V> rowMocker);


    /**
     * rtfc.
     *
     * @param attrList row attribute list.
     * @param rowMocker row mocker.
     */
    M mockToString(final List<String> attrList, final RowMocker<M, V> rowMocker);


}
