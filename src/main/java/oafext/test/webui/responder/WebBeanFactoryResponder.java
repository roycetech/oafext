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

import java.util.HashMap;
import java.util.Map;

import oafext.OafExtException;
import oafext.test.webui.MdsFixture;
import oafext.test.webui.WbMockerFactory;
import oafext.test.webui.WebBeanFactoryMocker;
import oafext.test.webui.WebBeanMocker;
import oracle.apps.fnd.framework.webui.OAPageContext;
import oracle.apps.fnd.framework.webui.OAWebBeanConstants;
import oracle.apps.fnd.framework.webui.OAWebBeanFactory;
import oracle.apps.fnd.framework.webui.beans.OAFormattedTextBean;
import oracle.apps.fnd.framework.webui.beans.form.OASubmitButtonBean;
import oracle.apps.fnd.framework.webui.beans.layout.OAFlowLayoutBean;
import oracle.apps.fnd.framework.webui.beans.layout.OASpacerBean;

import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Note: Check style turned of on magic number because it is difficult to define
 * a constant for parameter index when they can be different for an overloaded
 * method mocking.
 *
 * @author $Author: $
 * @version $Date: $
 *
 */
public class WebBeanFactoryResponder {


    /** Style constant to bean class. */
    private static final Map<String, Class<?>> STYLE_TYPE_MAP = new HashMap<>();
    static {
        STYLE_TYPE_MAP.put(
            OAWebBeanConstants.BUTTON_SUBMIT_BEAN,
            OASubmitButtonBean.class);

        STYLE_TYPE_MAP.put(
            OAWebBeanConstants.FLOW_LAYOUT_BEAN,
            OAFlowLayoutBean.class);

        STYLE_TYPE_MAP.put(
            OAWebBeanConstants.FORMATTED_TEXT_BEAN,
            OAFormattedTextBean.class);

        STYLE_TYPE_MAP.put(OAWebBeanConstants.SPACER_BEAN, OASpacerBean.class);

    }

    /**
     * @param wbFacker web bean factory mocker instance.
     */
    public void mockMethods(final WebBeanFactoryMocker wbFacker)
    {
        mockCreateWebBeanMds(wbFacker).createWebBean(
            (OAPageContext) Matchers.any(),
            Matchers.anyString(),
            Matchers.anyString(),
            Matchers.anyBoolean());

        mockCreateWebBeanStyle(wbFacker).createWebBean(
            (OAPageContext) Matchers.any(),
            Matchers.anyString(),
            Matchers.anyString(),
            Matchers.anyString());
    }


    @SuppressWarnings("PMD.TooFewBranchesForASwitchStatement")
    /**
     * Mock createWebBean(OAPageContext pageContext, String style, String
     * dataType, String itemName)
     *
     * {@link oracle.apps.fnd.framework.webui.OAWebBeanFactory#createWebBean(oracle.apps.fnd.framework.webui.OAPageContext, java.lang.String, java.lang.String, java.lang.String)}
     * .
     *
     * @param wbFacker web bean factory mocker.
     */
    OAWebBeanFactory mockCreateWebBeanStyle(final WebBeanFactoryMocker wbFacker)
    {
        return Mockito
            .doAnswer(
                p -> {

                    //CHECKSTYLE:OFF
                    final String beanId = p.getArguments()[3].toString();
                    //CHECKSTYLE:ON
                    final String style = p.getArguments()[1].toString();
                    final Class<?> beanClass = STYLE_TYPE_MAP.get(style);
                    assert beanClass != null : "Please define class type for: "
                            + style;

                    WebBeanMocker<?> webBeanMocker;
                    switch (style) {

                        case OAWebBeanConstants.BUTTON_SUBMIT_BEAN:
                            webBeanMocker =
                                    WbMockerFactory.SubmitButton.newInstance(
                                        null,
                                        beanId);
                            break;

                        case OAWebBeanConstants.FLOW_LAYOUT_BEAN:
                            webBeanMocker =
                                    WbMockerFactory.FlowLayout.newInstance(
                                        null,
                                        beanId);
                            break;

                        case OAWebBeanConstants.FORMATTED_TEXT_BEAN:
                            webBeanMocker =
                                    WbMockerFactory.FormattedText.newInstance(
                                        null,
                                        beanId);
                            break;

                        case OAWebBeanConstants.SPACER_BEAN:
                            webBeanMocker =
                                    WbMockerFactory.Spacer.newInstance(
                                        null,
                                        beanId);
                            break;

                        default:
                            throw new OafExtException(
                                "Define mapping/implementation for style: "
                                        + style);
                    }

                    wbFacker.getPgLayoutMocker().registerMocker(webBeanMocker);


                    //                    wbFacker.addTransient(webBeanMocker);
                    return webBeanMocker.getMock();
                }).when(wbFacker.getMock());
    }

    /**
     * Mock createWebBean(OAPageContext pageContext, String mdsReference, String
     * itemName, boolean MDSFlag)
     *
     * {@link oracle.apps.fnd.framework.webui.OAWebBeanFactory#createWebBean(oracle.apps.fnd.framework.webui.OAPageContext, java.lang.String, java.lang.String, boolean)}
     * .
     *
     * @param wbFacker web bean factory mocker.
     */
    OAWebBeanFactory mockCreateWebBeanMds(final WebBeanFactoryMocker wbFacker)
    {
        return Mockito.doAnswer(
            invocation -> {

                final String beanId = invocation.getArguments()[2].toString();

                if (wbFacker.getMdsMap().get(beanId) == null) {

                    final String mdsPath =
                            invocation.getArguments()[1].toString();

                    wbFacker.getMdsMap().put(
                        beanId,
                        new MdsFixture(mdsPath, null, beanId));
                }

                final MdsFixture extMds = wbFacker.getMdsMap().get(beanId);
                //wbFacker.addTransient(extMds.getTopWbMocker());

                wbFacker.getPgLayoutMocker().registerMocker(
                    extMds.getTopWbMocker());

                return extMds.getTopWbMocker().getMock();
            }).when(wbFacker.getMock());
    }

}
