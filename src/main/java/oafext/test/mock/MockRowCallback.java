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
package oafext.test.mock;

import oafext.test.server.RowMocker;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;


/**
 * @version $Date$
 *
 * @param <M> specific Row type.
 * @param <V> View Object type.
 */
public interface MockRowCallback<M extends ViewRowImpl, V extends ViewObjectImpl> {


    /**
     * @param rowMocker row mocker instance.
     * @param setUp distinguish call back to test setup or actual execution.
     */
    void callback(RowMocker<M, V> rowMocker, boolean setUp);


}
