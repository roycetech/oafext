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
import oafext.test.webui.MdsFixture;
import oafext.test.webui.WebBeanMocker;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.cabo.ui.UINode;

import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author $Author: $
 * @version $Date: $
 *
 * @param <T> web bean type.
 */
public class WebBeanResponder<T extends OAWebBean> {


    /**
     * @param mdsFixture owner MDS fixture of the web bean mocker.
     * @param pMocker web bean mocker instance.
     */
    public void mockMethods(final MdsFixture mdsFixture,
                            final WebBeanMocker<T> pMocker)
    {
        mockAddIndexedChild(pMocker).addIndexedChild(
            Matchers.anyInt(),
            (UINode) Matchers.any());

        mockGetNodeID(pMocker).getNodeID();
        mockGetNodeID(pMocker).getID();

        mockFindChildRecursive(mdsFixture, pMocker).findChildRecursive(
            Matchers.anyString());
    }

    OAWebBean mockAddIndexedChild(final WebBeanMocker<T> pMocker)
    {
        return Mockito.doAnswer(p -> {
            final int index = (Integer) p.getArguments()[0];
            final OAWebBean webBeanParam = (OAWebBean) p.getArguments()[1];
            pMocker.prepareIdxChildren();
            pMocker.addIndexedChild(index, webBeanParam);
            return null;
        }).when(pMocker.getMock());
    }


    OAWebBean mockGetNodeID(final WebBeanMocker<T> pMocker)
    {
        return Mockito.doAnswer(invocation -> pMocker.getWebBeanId()).when(
            pMocker.getMock());
    }


    /**
     * @param mdsFixture
     * @param pMocker
     */
    OAWebBean mockFindChildRecursive(final MdsFixture mdsFixture,
                                     final WebBeanMocker<T> pMocker)
    {
        return Mockito.doAnswer(p -> {

            final String webBeanId = (String) p.getArguments()[0];
            assert webBeanId != null;
            //                OafLogger.getInstance().debug(
            //                    "findChildRecursive(" + webBeanId + ')');

            final WebBeanMocker<? extends OAWebBean> existingMocker =
                    pMocker.findMockerRecursive(webBeanId);

            final Return<WebBeanMocker<? extends OAWebBean>> retval =
                    new Return<>();

            final WebBeanMocker<T> lMocker = pMocker; //for debug.

            if (existingMocker == null) {
                final WebBeanMocker<? extends OAWebBean> wbMocker =
                        mdsFixture.mockWebBean(webBeanId);
                retval.set(wbMocker);
            } else {
                retval.set(existingMocker);
            }

            return retval.get() == null ? null : retval.get().getMock();

        }).when(pMocker.getMock());
    }
}
