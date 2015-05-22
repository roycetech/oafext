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
import oafext.test.webui.responder.WebBeanFactoryResponder;
import oracle.apps.fnd.framework.webui.OAWebBeanFactory;
import oracle.apps.fnd.framework.webui.beans.layout.OAPageLayoutBean;

import org.mockito.Mockito;

/**
 * @author $Author: $
 * @version $Date: $
 *
 */
public class WebBeanFactoryMocker implements Mocker<OAWebBeanFactory> {


    /** */
    private final transient OAWebBeanFactory mock;

    private final transient WebBeanMocker<OAPageLayoutBean> pgLayoutMocker;

    //    /** Mock webBean to Mocker Map. */
    //    private final transient Map<OAWebBean, WebBeanMocker<? extends OAWebBean>> createdBeans =
    //            new HashMap<>();

    /** Path to Fixture. */
    private final transient Map<String, MdsFixture> mdsMap =
            new HashMap<String, MdsFixture>();


    //    private final transient WebBeanFactoryResponder responder;

    /**
     * Retain reference to pageLayout mocker.
     */
    WebBeanFactoryMocker(final WebBeanMocker<OAPageLayoutBean> pPgLayoutMocker) {
        this.mock = Mockito.mock(OAWebBeanFactory.class);
        this.pgLayoutMocker = pPgLayoutMocker;

        new WebBeanFactoryResponder().mockMethods(this);
    }


    //    /**
    //     * Remove transient created bean.s
    //     *
    //     * @param webBean transient created bean to remove.
    //     * */
    //    public WebBeanMocker<? extends OAWebBean> remove(final OAWebBean webBean)
    //    {
    //        return this.createdBeans.remove(webBean);
    //    }

    //    /**
    //     * Remove transient created beans.
    //     *
    //     * @param pMocker WebBeanMocker instance.
    //     */
    //    public void addTransient(final WebBeanMocker<? extends OAWebBean> pMocker)
    //    {
    //        this.createdBeans.put(pMocker.getMock(), pMocker);
    //    }

    //    /**
    //     * @return the createdBeans
    //     */
    //    public Map<OAWebBean, WebBeanMocker<? extends OAWebBean>> getCreatedBeans()
    //    {
    //        return this.createdBeans;
    //    }

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


    /**
     * Needed by responder.
     *
     * @return the pgLayoutMocker
     */
    public WebBeanMocker<OAPageLayoutBean> getPgLayoutMocker()
    {
        return this.pgLayoutMocker;
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new StringBuilder().append(getClass().getSimpleName())
        //            .append('\n')
        //            .append("Created: ")
        //            .append(this.createdBeans)
            .append('\n')
            .append("MDS Map: ")
            .append(this.mdsMap)
            .append('\n')
            .toString();
    }

}
