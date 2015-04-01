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
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

/**
 * This mocks more complex behavior using Answer class.<br/>
 * Convention: Mock View object is always the first parameter if present.
 *
 * @author royce
 *
 * @param <M> View Object type.
 * @param <R> Row type.
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface ViewObjectResponder<M extends ViewObjectImpl, R extends ViewRowImpl>
        extends RowSetResponder<M, R> {


    /**
     * @param amFixture application module fixture.
     * @param voMocker view object mocker.
     */
    void mockMethods(final AppModuleFixture<?> amFixture,
                     final BaseViewObjectMocker<M, R> voMocker);

    /**
     * @param voMocker view object mocker.
     */
    M mockCreateViewCriteria(final BaseViewObjectMocker<M, R> voMocker);

    /**
     * @param amFixture application module fixture.
     * @param voMocker view object mocker.
     */
    M mockGetAttributeCount(final AppModuleFixture<?> amFixture,
                            final BaseViewObjectMocker<M, R> voMocker);

    /**
     * @param amFixture application module fixture.
     * @param voMocker view object mocker.
     */
    M mockGetAttributeDef(final AppModuleFixture<?> amFixture,
                          final BaseViewObjectMocker<M, R> voMocker);


    /**
     * @param amFixture application module fixture.
     * @param voMocker view object mocker.
     */
    M mockGetAttributeIndexOf(final AppModuleFixture<?> amFixture,
                              final BaseViewObjectMocker<M, R> voMocker);


    //    /**
    //     * @param amFixture application module fixture.
    //     * @param voMocker view object mocker.
    //     */
    //    M mockToString(final AppModuleFixture<?> amFixture,
    //                   final BaseViewObjectMocker<M, R> voMocker);


}
