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

import oafext.test.RowSetMocker;
import oafext.test.server.AppModuleFixture;
import oracle.jbo.RowSet;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

/**
 * This mocks more complex behavior using Answer class.<br/>
 * Convention: Mock View object is always the first parameter if present.
 *
 * @author royce
 *
 * @param <V> View Object type.
 * @param <R> Row type.
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface RowSetResponder<V extends ViewObjectImpl, R extends ViewRowImpl> {


    /**
     * @param amFixture application module fixture.
     * @param rowSetMocker view object mocker.
     */
    void mockMethods(final AppModuleFixture<?> amFixture,
                     final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param amFixture application module fixture.
     * @param rowSetMocker view object mocker.
     */
    RowSet mockCreateRow(final AppModuleFixture<?> amFixture,
                         final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockCreateRowSetIterator(final RowSetMocker<V, R> rowSetMocker);

    //    /**
    //     * @param rowSetMocker view object mocker.
    //     */
    //    RowSet mockCreateViewCriteria(final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockExecuteQuery(final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockFirst(final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockGetAllRowsInRange(final RowSetMocker<V, R> rowSetMocker);


    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockGetCurrentRow(final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockGetRowAtRangeIndex(final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockGetRowCount(final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockIsExecuted(final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockSetCurrentRow(final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockInsertRow(final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockInsertRowAtRangeIndex(final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param rowSetMocker view object mocker.
     */
    RowSet mockSetRangeSize(final RowSetMocker<V, R> rowSetMocker);

    /**
     * @param amFixture application module fixture.
     * @param rowSetMocker view object mocker.
     */
    RowSet mockToString(final AppModuleFixture<?> amFixture,
                        final RowSetMocker<V, R> rowSetMocker);

}
