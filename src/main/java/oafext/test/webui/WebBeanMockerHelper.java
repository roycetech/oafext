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

/**
 * <ul>
 * <li>Root is top level web bean mocker. Top level of MDS.
 * <li>Top is top level web bean mocker. Top level of an MDS. Top level
 * references member mockers. Can have children mockers as well which are also
 * members.
 * </ul>
 *
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
public final class WebBeanMockerHelper {


    /** */
    private WebBeanMockerHelper() {}

    /**
     * Helper method to represent the WebBeanMocker.
     *
     * @param pWbMocker mocker of interest.
     */
    public static String toString(final WebBeanMocker<? extends OAWebBean> pWbMocker)
    {
        final StringBuilder strBuilder = new StringBuilder();

        final String paddedNewline = getPadding(pWbMocker);

        strBuilder
            .append(pWbMocker.getClass().getSimpleName())
            .append(paddedNewline)
            .append("Type: ")
            .append(pWbMocker.getMock().getClass().getSimpleName())
            .append(paddedNewline)
            .append("ID: ")
            .append(pWbMocker.getWebBeanId())
            .append(paddedNewline)
            .append("MDS: ")
            .append(
                pWbMocker.getMdsFixture() == null ? null : pWbMocker
                    .getMdsFixture()
                    .getMdsPath())
            .append(paddedNewline)
            .append("ICP: ")
            .append(pWbMocker.idxChildPrepared)
            .append(paddedNewline)
            .append("TOP: ")
            .append(pWbMocker.isTopLevel())
            .append(paddedNewline);

        parentToString(pWbMocker, strBuilder);
        childrenToString(pWbMocker, strBuilder);
        memberToString(pWbMocker, strBuilder);

        return strBuilder.toString();
    }

    /**
     * @param pWbMocker
     * @param strBuilder
     * @param paddedNewline
     */
    static void memberToString(final WebBeanMocker<? extends OAWebBean> pWbMocker,
                               final StringBuilder strBuilder)
    {
        final String paddedNewline = getPadding(pWbMocker);
        strBuilder
            .append("Members Size: ")
            .append(pWbMocker.memberMockers.size())
            .append(paddedNewline);

        if (!pWbMocker.memberMockers.isEmpty()) {
            strBuilder.append(pWbMocker.memberMockers);
        }
    }

    /**
     * @param pWbMocker
     * @param strBuilder
     * @param paddedNewline
     */
    static void parentToString(final WebBeanMocker<? extends OAWebBean> pWbMocker,
                               final StringBuilder strBuilder)
    {
        final String paddedNewline = getPadding(pWbMocker);
        strBuilder
            .append(paddedNewline)
            .append("Parent: ")
            .append(
                pWbMocker.parent == null ? null : pWbMocker.parent
                    .getMdsFixture()
                    .getMdsPath());
    }

    static void childrenToString(final WebBeanMocker<? extends OAWebBean> pWbMocker,
                                 final StringBuilder strBuilder)
    {
        final String paddedNewline = getPadding(pWbMocker);
        strBuilder
            .append(paddedNewline)
            .append("Children Size: ")
            .append(pWbMocker.idxChildMockers.size())
            .append(paddedNewline);

        if (!pWbMocker.idxChildMockers.isEmpty()) {
            strBuilder.append(pWbMocker.idxChildMockers);
        }
    }

    /**
     * @param pWbMocker
     * @return
     */
    static String getPadding(final WebBeanMocker<? extends OAWebBean> pWbMocker)
    {
        String paddedNewline;
        if (pWbMocker.isTopLevel()) {
            paddedNewline = "\n";
        } else {
            paddedNewline = "\n" + String.format("%4s", "");
        }
        return paddedNewline;
    }

}
