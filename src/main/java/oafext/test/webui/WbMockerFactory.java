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

import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.apps.fnd.framework.webui.beans.form.OASubmitButtonBean;

/**
 * Factory for creating mocker. May decide later to create more granular mocker,
 * so we will have central place to alter them.
 *
 * @author $Author: $
 * @version $Date: $
 *
 */
public interface WbMockerFactory {

    /** */
    class WebBean {

        private WebBean() {}

        public static WebBeanMocker<OAWebBean> newInstance(final MdsFixture mdsFixture,
                                                           final String pWebBeanId)
        {
            return new WebBeanMocker<OAWebBean>(
                mdsFixture,
                pWebBeanId,
                OAWebBean.class,
                false,
                mdsFixture.getTopWbMocker());
        }

    }

    /** */
    class SubmitButton {

        private SubmitButton() {}

        /**
         * @param mdsFixture MDS fixture to which this instance will belong.
         *            This can be null for created web beans via
         *            OAWebBeanFactory.
         * 
         * @param pWebBeanId web bean ID.
         */
        public static WebBeanMocker<OASubmitButtonBean> newInstance(final MdsFixture mdsFixture,
                                                                    final String pWebBeanId)
        {
            return new WebBeanMocker<OASubmitButtonBean>(
                mdsFixture,
                pWebBeanId,
                OASubmitButtonBean.class,
                false,
                mdsFixture == null ? null : mdsFixture.getTopWbMocker());
        }

    }

}
