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

import java.util.HashMap;
import java.util.Map;

import oafext.test.mock.Mocker;

/**
 * Base class for Iterable mockers.
 *
 * @version $Date$
 * @param <M> mock type.
 */
public abstract class AbstractIteratorMocker<M> implements Mocker<M> {


    /** */
    private final transient Map<String, RowSetIteratorMocker> rowSetIterMap = new HashMap<>();


    public Map<String, RowSetIteratorMocker> getRowSetIterMap()
    {
        return this.rowSetIterMap;
    }

}
