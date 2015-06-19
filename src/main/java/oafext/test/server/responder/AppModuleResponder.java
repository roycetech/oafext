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

import oafext.OafExtException;
import oafext.test.server.AppModuleMocker;
import oafext.test.server.BaseViewObjectMocker;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.jbo.ViewObject;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author royce
 * @param <A> specific application module type.
 */
public final class AppModuleResponder<A extends OAApplicationModuleImpl> {


    /**
     * @param mockAppModule mock application module.
     * @param amMocker application module mocker.
     * @return
     */
    public A mockFindViewObject(final A mockAppModule,
                                final AppModuleMocker<?> amMocker)
    {
        return Mockito.doAnswer(new Answer<ViewObject>() {

            @Override
            public ViewObject answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                final String voInstName = (String) invocation.getArguments()[0];
                final BaseViewObjectMocker<?, ?> voMocker =
                        amMocker.getVoInstMockerMap().get(voInstName);

                if (voMocker == null) {
                    throw new OafExtException(
                        "ViewObject not mocked, make sure you invoked mockViewObject* on your AM Fixture.");
                }

                return voMocker.getMock();
            }
        })
            .when(mockAppModule);

    }

}
