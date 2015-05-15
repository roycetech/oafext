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

/**
 * @author $Author: $
 * @version $Date: $
 *
 */
public class WbMockCreator {

    /**
     * Will construct OAF web bean simple name from element name.
     *
     * e.g. oa:messageStyledText will result into OAMessageStyledTextBean.
     *
     * @param elemName XML Element Name.
     * @return Web Bean class simple name.
     */
    static String buildOaWebBeanType(final String elemName)
    {
        final String[] arr = elemName.split(":");
        return "OA" + arr[1].substring(0, 1).toUpperCase()
                + arr[1].substring(1) + "Bean";
    }

}
