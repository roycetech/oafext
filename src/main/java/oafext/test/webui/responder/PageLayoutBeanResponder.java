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
import oafext.test.webui.WebBeanMocker;
import oracle.apps.fnd.framework.webui.beans.layout.OAPageLayoutBean;

/**
 * @author $Author: $
 * @version $Date: $
 *
 */
public class PageLayoutBeanResponder extends WebBeanResponder<OAPageLayoutBean> {

    @Override
    public void mockMethods(final MdsFixture mdsFixture,
                            final WebBeanMocker<OAPageLayoutBean> pMocker)
    {
        super.mockMethods(mdsFixture, pMocker);

        //OAPageLayoutBean#setPageButtons is final, use wrapper.

    }
}
