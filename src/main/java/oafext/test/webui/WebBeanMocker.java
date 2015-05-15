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
import oafext.test.webui.responder.PageLayoutBeanResponder;
import oafext.test.webui.responder.WebBeanResponder;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.apps.fnd.framework.webui.beans.layout.OAPageLayoutBean;

import org.mockito.Mockito;

/**
 * <ul>
 * <li>Two approach, recursive Web Bean or,
 * <li>MDS (root type) Web Bean containing Map of Web Bean Mockers.
 * </ul>
 *
 * Will try recursive first.
 *
 * @author $Author: $
 * @version $Date: $
 *
 */
public class WebBeanMocker<W extends OAWebBean> implements Mocker<W> {


    /** */
    private final transient W mock;

    //    /** */
    //    private transient Element element;

    /** */
    private final transient String webBeanId;

    /** Web Bean Attributes. */
    private final Map<String, Object> attrMap = new HashMap<String, Object>();
    /** Child Mockers */
    private final transient Map<String, WebBeanMocker<? extends OAWebBean>> childMockers =
            new HashMap<>();

    /**
     * Temporary place holder for created Web Bean. TODO: Move to
     * WebBeanFactoryMocker.
     */
    private final transient Map<String, WebBeanMocker<? extends OAWebBean>> createdWebBeanMap =
            new HashMap<>();


    /** Identifies this mocker as top level or child. */
    private final transient boolean topLevel;


    /** */
    static final Map<Class<? extends OAWebBean>, WebBeanResponder> BEAN_RESP_MAP =
            new HashMap<>();
    static {
        BEAN_RESP_MAP
            .put(OAPageLayoutBean.class, new PageLayoutBeanResponder());
        BEAN_RESP_MAP.put(OAWebBean.class, new WebBeanResponder());
    }

    /**
     * Instantiate a top level(PG or RN) WebBeanMocker.
     *
     * @param mdsFixture PG fixture where the web bean belongs.
     * @param webBeanClass type of web bean.
     */
    WebBeanMocker(final MdsFixture mdsFixture, final Class<W> webBeanClass) {
        this(mdsFixture, null, webBeanClass, true);
    }

    /**
     * Instantiate a WebBeanMocker.
     *
     * @param mdsFixture PG or RN fixture where the web bean belongs.
     * @param webBeanClass type of web bean.
     * @param pTopLevel true of top level, otherwise child Web Bean Mocker.
     */
    WebBeanMocker(final MdsFixture mdsFixture, final String pWebBeanId,
            final Class<W> webBeanClass, final boolean pTopLevel) {
        this.topLevel = pTopLevel;
        this.mock = Mockito.mock(webBeanClass);
        this.webBeanId = pWebBeanId;

        WebBeanResponder responder;
        if (BEAN_RESP_MAP.get(webBeanClass) == null) {
            responder = BEAN_RESP_MAP.values().iterator().next();
        } else {
            responder = BEAN_RESP_MAP.get(webBeanClass);
        }
        responder.mockMethods(mdsFixture, this);
    }


    @Override
    public W getMock()
    {
        return this.mock;
    }

    /**
     * @return the topLevel
     */
    public boolean isTopLevel()
    {
        return this.topLevel;
    }

    /**
     * @return the attrMap
     */
    public Map<String, Object> getAttrMap()
    {
        return this.attrMap;
    }

    /**
     * Add a child WebBeanMocker.
     */
    public void addChildMocker(final String webBeanId,
                               final WebBeanMocker<? extends OAWebBean> childMocker)
    {
        this.childMockers.put(webBeanId, childMocker);
    }

    /**
     * @return the childMockers
     */
    public WebBeanMocker<? extends OAWebBean> getChildMocker(final String webBeanId)
    {
        return this.childMockers.get(webBeanId);
    }

    public WebBeanMocker<? extends OAWebBean> findMockerRecursive(final String webBeanId)
    {
        //TODO:
        return null;
    }

    /**
     * @return the webBeanId
     */
    public String getWebBeanId()
    {
        return this.webBeanId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        final StringBuilder strBuilder =
                new StringBuilder()
                    .append(getClass().getSimpleName())
                    .append("\nType: ")
                    .append(getMock().getClass().getSimpleName())
                    .append("\nID: ")
                    .append(this.webBeanId)
                    .append("\nChildren Size: ")
                    .append(this.childMockers.size())
                    .append('\n');

        if (!this.childMockers.isEmpty()) {
            strBuilder.append(this.childMockers).append('\n').toString();
        }

        return strBuilder.toString();

    }

}
