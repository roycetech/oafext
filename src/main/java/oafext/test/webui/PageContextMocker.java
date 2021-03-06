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
package oafext.test.webui;

import java.util.HashMap;
import java.util.Map;

import oafext.ann.Revision;
import oafext.test.mock.Mocker;
import oafext.test.webui.responder.PageContextResponder;
import oracle.apps.fnd.framework.webui.OAPageContext;
import oracle.apps.fnd.framework.webui.beans.layout.OAPageLayoutBean;

import org.junit.After;
import org.mockito.Mockito;

/**
 * Initially to support parameter and session values only.
 *
 * @author $Author: $
 * @version $Date: $
 */
@Revision("$Revision: $")
public class PageContextMocker implements Mocker<OAPageContext> {


    /** */
    private final transient OAPageContext mockPageContext;


    /** Page Parameters. */
    private final transient Map<String, Object> params =
            new HashMap<String, Object>();

    /** Session Values. Date, Number, and String only. */
    private final transient Map<String, Object> sessionValues =
            new HashMap<String, Object>();


    /** */
    private final transient WebBeanFactoryMocker wbFocker;


    @SuppressWarnings("unchecked")
    PageContextMocker(final MdsFixture mdsFixture) {
        this.mockPageContext = Mockito.mock(OAPageContext.class);

        this.wbFocker =
                new WebBeanFactoryMocker(
                    (WebBeanMocker<OAPageLayoutBean>) mdsFixture
                        .getTopWbMocker());
        new PageContextResponder().mockMethods(mdsFixture, this);
    }

    @After
    void tearDown()
    {
        this.params.clear();
        this.sessionValues.clear();
    }


    @Override
    public OAPageContext getMock()
    {
        return this.mockPageContext;
    }

    /**
     * @return the wbFocker
     */
    public WebBeanFactoryMocker getWbFocker()
    {
        return this.wbFocker;
    }

}
