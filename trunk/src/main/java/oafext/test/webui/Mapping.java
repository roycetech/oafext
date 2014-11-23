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
package oafext.test.webui;

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

class BeanMockMap extends HashMap<String, OAWebBean> {}

class BeanLockMap extends HashMap<String, Boolean> {}

class BeanShowMap extends HashMap<String, Boolean> {}

class BeanRequiredMap extends HashMap<String, Boolean> {}

class BeanParentMap extends HashMap<String, String> {}

class BeanChildrenMap extends HashMap<String, List<String>> {}

class MdsFixtureList extends ArrayList<MdsFixture2> {}


/** Map of Bean ID to List of MDS Path that contained the same ID. */
class DupedBeanMap extends HashMap<String, List<String>> {}


/** External MDS path to parent WebBeanID Map. */
class ExtMdsParentIdMap extends HashMap<String, String> {};

/** External MDS path to MdsFixture2 Map. */
class MdsToFixtureMap extends HashMap<String, MdsFixture2> {};

/** WebBeanID to external MDS path Map. */
class BeanExtMdsMap extends HashMap<String, String> {};
