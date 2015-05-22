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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import oafext.ann.Revision;
import oafext.lang.Return;
import oafext.logging.OafLogger;
import oafext.util.StringUtil;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;

import org.junit.After;
import org.w3c.dom.Element;

/**
 * Root MDS is single, while external MDS can be multiple.
 *
 * Interfaces Mocker with the DOM via DOMManager field.
 *
 * <pre>
 * @author $Author$
 * @version $Date$
 * </pre>
 */
@Revision("$Revision: $")
public class MdsFixture {


    /** */
    //private static final OafLogger LOGGER = OafLogger.getInstance();


    /** */
    private final MdsFixture parent;
    /** Children MDS Fixtures. */
    private final transient List<MdsFixture> childFixtures =
            new ArrayList<MdsFixture>();


    /** Temporary placeholder for external MDS' web bean Id. */
    private final transient String extWbId;
    /** For display only. */
    private transient String mdsPath;


    /** Helper class instance for DOM retrieval operations. */
    private final transient DOMManager domManager;

    /** */
    private transient WebBeanMocker<? extends OAWebBean> topWbMocker;
    /** */
    private transient PageContextMocker pageContextMocker;

    //    /** */
    //    private transient Element rootElement;


    /** @param pMdsPath (e.g. "/xxx/oracle/apps/xx/module/webui/SomePG". */
    public MdsFixture(final String pMdsPath) {
        this(pMdsPath, null, null);
        assert pMdsPath.endsWith("PG") : "Root MDS constructor can be used for PG only.";
    }

    /**
     * @param pMdsPath MDS path.
     * @param pParent parent MDS fixture.
     * @param pWebBeanId web bean ID.
     */
    public MdsFixture(final String pMdsPath, final MdsFixture pParent,
            final String pWebBeanId) {

        assert StringUtil.hasValue(pMdsPath);

        this.mdsPath = pMdsPath;
        this.parent = pParent;
        this.extWbId = pWebBeanId;
        this.domManager = new DOMManager(pMdsPath);
        this.domManager.initializeDOM(this);
    }


    @After
    public void tearDown()
    {
        getTopWbMocker().tearDown();
    }


    /**
     * @return the pageContextMocker
     */
    public PageContextMocker getPageContextMocker()
    {
        return this.pageContextMocker;
    }

    /**
     * @return the rootWbMocker
     */
    public WebBeanMocker<? extends OAWebBean> getTopWbMocker()
    {
        return this.topWbMocker;
    }

    /**
     * @return the parent
     */
    public MdsFixture getParent()
    {
        return this.parent;
    }

    /**
     * Creates a top web bean mocker. Mapped to MDS fixture.
     *
     * @param webBeanId can be null or the ID assigned by the extending MDS. //
     *            * @param element DOM element instance.
     */
    @SuppressWarnings({
            "unchecked",
            "rawtypes",
            "PMD.NullAssignment" })
    private WebBeanMocker<? extends OAWebBean> createMocker(final String webBeanId,
                                                            final String pOaWbType,
                                                            final boolean isRoot,
                                                            final WebBeanMocker<? extends OAWebBean> pParent)
    {
        //assert isRoot && topOrChild || !isRoot : "Impossible to be non-top, root";

        final Class<? extends OAWebBean> oaClass =
                OABeanUtil.getOABeanClass(pOaWbType);

        final boolean isMockableClass =
                !Modifier.isFinal(oaClass.getModifiers());

        return new WebBeanMocker(this, webBeanId, isMockableClass ? oaClass
                : OAWebBean.class, isRoot, isRoot ? null : pParent);
    }


    /**
     * This is on MDS domain because MDS has the handle for the DOM needed to
     * initialize the mocker. Determine if this element should be passed to the
     * mocker to have it perform the initialization there.
     *
     * @param webBeanId web bean ID to mock on this MDS, not on dependent MDS.
     */
    public WebBeanMocker<? extends OAWebBean> mockWebBean(final String webBeanId)
    {
        final Element element = findElRecursive(webBeanId);
        final Return<WebBeanMocker<? extends OAWebBean>> retval =
                new Return<>();
        if (element == null) {
            OafLogger.getInstance().warn("Element not found for: " + webBeanId);


        } else {
            final String oaWebBeanType =
                    OABeanUtil.buildOaWebBeanType(element.getNodeName());
            retval.set(createMocker(
                webBeanId,
                oaWebBeanType,
                false,
                this.topWbMocker));
            //getTopWbMocker().addMember(webBeanId, retval.get());
            getTopWbMocker().addMember(retval.get());
        }

        return retval.get();
    }

    /**
     * Add new child MDS.
     *
     * @param childMds add child MDS via created web bean factory or through
     *            extended regions.
     */
    public void addChild(final MdsFixture createdMds)
    {
        createdMds.getTopWbMocker().setParent(getTopWbMocker());
        this.childFixtures.add(createdMds);
    }

    /**
     * Add new child MDS.
     *
     */
    public void giveBirth(final String extension, final String beanId)
    {
        this.childFixtures.add(new MdsFixture(extension, this, beanId));
    }

    /**
     * @return the mdsPath
     */
    String getMdsPath()
    {
        return this.mdsPath;
    }

    void initRootMocker(final String pOaWbType)
    {
        if ("OAPageLayoutBean".equals(pOaWbType)) {

            this.topWbMocker =
                    createMocker(this.extWbId, pOaWbType, true, null);
            this.pageContextMocker = new PageContextMocker(this);

        } else {

            final boolean transientMds = this.parent == null; //Created by WebBeanFactory/
            this.topWbMocker =
                    createMocker(
                        this.extWbId,
                        pOaWbType,
                        false,
                        transientMds ? null : this.parent.getTopWbMocker());
        }
    }


    /**
     * @return the domManager
     */
    public DOMManager getDomManager()
    {
        return this.domManager;
    }

    public Element findElRecursive(final String id)
    {
        final Return<Element> retval = new Return<>();
        //final Element currentEl = getDomManager().findElement(this.rootElement, id);
        final Element currentEl = getDomManager().findElement(id);
        if (currentEl == null) {
            for (final MdsFixture mdsFixture : this.childFixtures) {
                final Element childEl = mdsFixture.findElRecursive(id);
                if (childEl != null) {
                    retval.set(childEl);
                    retval.toString();
                    break;
                }
            }
        } else {
            retval.set(currentEl);
        }
        return retval.get();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new StringBuilder()
            .append(getClass().getSimpleName())
            .append('\n')
            .append("Bean Id: ")
            .append(this.extWbId)
            .append('\n')
            .append("Path: ")
            .append(this.mdsPath)
            .append('\n')
            .append("PCM: ")
            .append(this.pageContextMocker)
            .append('\n')
            .append("TWM: ")
            .append(this.topWbMocker)
            .append('\n')
            .append(this.childFixtures)
            .toString();
    }

}
