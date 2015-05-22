/**
 *   Copyright 2015 Royce Remulla
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
package oafext.test.webui.responder;

import oafext.test.webui.MdsFixture;
import oafext.test.webui.PageContextMocker;

import org.mockito.Mockito;

/**
 * Responder for PageContext AND OAWebBeanFactory.
 *
 * @author $Author: $
 * @version $Date: $
 *
 */
public class PageContextResponder {


    /**
     * Missing java doc comments.
     *
     * @param mdsFixture root MDS Fixture.
     * @param pcMocker page context mocker instance.
     */
    public void mockMethods(final MdsFixture mdsFixture,
                            final PageContextMocker pcMocker)
    {
        Mockito
            .doReturn(mdsFixture.getTopWbMocker().getMock())
            .when(pcMocker.getMock())
            .getPageLayoutBean();

        Mockito
            .doReturn(pcMocker.getWbFocker().getMock())
            .when(pcMocker.getMock())
            .getWebBeanFactory();
    }
}
