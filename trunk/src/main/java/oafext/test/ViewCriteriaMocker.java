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
package oafext.test;

import oafext.test.mock.Mocker;
import oafext.test.server.BaseViewObjectMocker;
import oracle.jbo.ViewCriteria;

import org.mockito.Mockito;


/**
 * Supports only one ViewCriteriaRow for now.
 *
 * @version $Date$
 */
public class ViewCriteriaMocker implements Mocker<ViewCriteria> {


    /** */
    private final transient BaseViewObjectMocker voMocker;


    /** */
    private final transient ViewCriteria mockViewCrit;

    /** */
    //    private final transient ViewCriteriaRowMocker vcRowMocker;


    /**
     * @param pVoMocker view object mocker instance.
     */
    public ViewCriteriaMocker(final BaseViewObjectMocker pVoMocker) {
        this.voMocker = pVoMocker;
        this.voMocker.setViewCritMocker(this);
        this.mockViewCrit = Mockito.mock(ViewCriteria.class);

        //        Mockito.doAnswer(new Answer<Object>).when()
        //        
        //        ViewCriteriaR

    }


    @Override
    public ViewCriteria getMock()
    {
        return this.mockViewCrit;
    }

}
