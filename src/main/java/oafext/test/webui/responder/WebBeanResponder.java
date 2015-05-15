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

import oafext.lang.Return;
import oafext.logging.OafLogger;
import oafext.test.webui.MdsFixture;
import oafext.test.webui.WebBeanMocker;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;

import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author $Author: $
 * @version $Date: $
 *
 */
public class WebBeanResponder<T extends OAWebBean> {

    public void mockMethods(final MdsFixture mdsFixture,
                            final WebBeanMocker<T> pMocker)
    {
        Mockito
            .doAnswer(invocation -> pMocker.getWebBeanId())
            .when(pMocker.getMock())
            .getNodeID();

        Mockito
            .doAnswer(
                p -> {


                    final String webBeanId = (String) p.getArguments()[0];
                    assert webBeanId != null;
                    OafLogger.getInstance().debug(
                        "findChildRecursive(" + webBeanId + ')');

                    final WebBeanMocker<? extends OAWebBean> existingMocker =
                            pMocker.findMockerRecursive(webBeanId);

                    final Return<WebBeanMocker<? extends OAWebBean>> retval =
                            new Return<>();

                    if (existingMocker == null) {
                        final WebBeanMocker<? extends OAWebBean> wbMocker =
                                mdsFixture.mockWebBean(webBeanId);
                        retval.set(wbMocker);
                    } else {
                        retval.set(existingMocker);
                    }
                    return retval.get().getMock();
                })
            .when(pMocker.getMock())
            .findChildRecursive(Matchers.anyString());
    }
}
