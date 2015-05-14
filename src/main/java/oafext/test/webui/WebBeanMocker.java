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
import oracle.apps.fnd.framework.webui.beans.OAWebBean;

import org.mockito.Mockito;

/**
 * Two approach, recursive Web Bean or MDS (root type) Web Bean containing Map
 * of Web Bean Mockers.
 *
 * Will try recursive first.
 *
 * @author $Author: $
 * @version $Date: $
 *
 */
public class WebBeanMocker<W extends OAWebBean> implements Mocker<W> {


    private final W mock;


    private final Map<String, Object> attrMap = new HashMap<String, Object>();


    WebBeanMocker(final Class<W> webBeanClass) {
        this.mock = Mockito.mock(webBeanClass);

    }

    @Override
    public W getMock()
    {
        return this.mock;
    }

}
