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
package oafext.test.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import oracle.apps.fnd.framework.webui.beans.OAWebBean;

import org.w3c.dom.Element;


/**
 * 
 * <pre>
 * @author $Author$ 
 * @version $Date$
 * </pre>
 */

class BeanElementMap extends HashMap<String, Element> {}

class MdsElementMap extends HashMap<String, BeanElementMap> {}


class BeanMockMap extends HashMap<String, OAWebBean> {}

class MdsBeanMap extends HashMap<String, BeanMockMap> {}


class BeanLockMap extends HashMap<String, Boolean> {}

class MdsLockMap extends HashMap<String, BeanLockMap> {}


class BeanShowMap extends HashMap<String, Boolean> {}

class MdsShowMap extends HashMap<String, BeanShowMap> {}


class BeanRequiredMap extends HashMap<String, Boolean> {}

class MdsRequiredMap extends HashMap<String, BeanRequiredMap> {}


class BeanParentMap extends HashMap<String, String> {}

class MdsParentMap extends HashMap<String, BeanParentMap> {}


class BeanChildrenMap extends HashMap<String, List<String>> {}

class MdsChildrenMap extends HashMap<String, BeanChildrenMap> {}


class ActionMockList extends ArrayList<ActionWebBeanMocker> {}

class MdsActionMockMap extends HashMap<String, ActionMockList> {}


/** Map of Bean ID to List of MDS Path that contained the same ID. */
class DupedBeanMap extends HashMap<String, List<String>> {}


/** External MDS path to parent WebBeanID Map. */
class ExtMdsParentIdMap extends HashMap<String, String> {};

/** WebBeanID to external MDS path Map. */
class BeanExtMdsMap extends HashMap<String, String> {};
