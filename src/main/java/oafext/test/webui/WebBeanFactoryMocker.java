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

import oafext.test.mock.Mocker;
import oracle.apps.fnd.framework.webui.OAPageContext;
import oracle.apps.fnd.framework.webui.OAWebBeanFactory;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;

import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author $Author: $
 * @version $Date: $
 *
 */
public class WebBeanFactoryMocker implements Mocker<OAWebBeanFactory> {


    /** */
    private final transient OAWebBeanFactory mock;

    private final transient Map<OAWebBean, WebBeanMocker<? extends OAWebBean>> createdBeans =
            new HashMap<>();

    /** Path to Fixture. */
    private final transient Map<String, MdsFixture> mdsMap =
            new HashMap<String, MdsFixture>();


    WebBeanFactoryMocker() {
        this.mock = Mockito.mock(OAWebBeanFactory.class);

        Mockito
            .doAnswer(
                invocation -> {

                    final String mdsPath =
                            invocation.getArguments()[1].toString();

                    final String beanId =
                            invocation.getArguments()[2].toString();

                    if (this.mdsMap.get(beanId) == null) {
                        this.mdsMap.put(beanId, new MdsFixture(
                            mdsPath,
                            null,
                            beanId));
                    }

                    final MdsFixture extMds = this.mdsMap.get(beanId);
                    this.createdBeans.put(
                        extMds.getRootWbMocker().getMock(),
                        extMds.getRootWbMocker());

                    return extMds.getRootWbMocker().getMock();
                })
            .when(getMock())
            .createWebBean(
                (OAPageContext) Matchers.any(),
                Matchers.anyString(),
                Matchers.anyString(),
                Matchers.anyBoolean());
    }


    public WebBeanMocker<? extends OAWebBean> remove(final OAWebBean webBean)
    {
        return this.createdBeans.remove(webBean);
    }

    /**
     * @return the createdBeans
     */
    public Map<? extends OAWebBean, WebBeanMocker<? extends OAWebBean>> getCreatedBeans()
    {
        return this.createdBeans;
    }

    @Override
    public final OAWebBeanFactory getMock()
    {
        return this.mock;
    }

    /**
     * @return the mdsMap
     */
    public Map<String, MdsFixture> getMdsMap()
    {
        return this.mdsMap;
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new StringBuilder()
            .append(getClass().getSimpleName())
            .append('\n')
            .append("Created: ")
            .append(getCreatedBeans())
            .append('\n')
            .append("MDS Map: ")
            .append(this.mdsMap)
            .append('\n')
            .toString();
    }

}
