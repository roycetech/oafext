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

import oracle.jbo.AttributeDef;

import org.mockito.Mockito;

/**
 * @author royce
 *
 */
public class AttrDefMocker {


    /** */
    private final transient AttributeDef mockAttrDef;


    AttrDefMocker(final String name) {
        this.mockAttrDef = Mockito.mock(AttributeDef.class);

        Mockito.doReturn(name).when(this.mockAttrDef).getName();
    }

    /**
     * @return the mockAttrDef
     */
    AttributeDef getMockAttrDef()
    {
        return this.mockAttrDef;
    }

}
