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
package oafext.test.server;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

/**
 * Creates a String representation of the view object based on its rows and
 * their chosen attributes.
 *
 * @author $Author: $
 * @version $Date: $
 *
 */
public class ViewObjectFormatter {

    private final transient int[] attributes;
    private final transient int[] lengths;
    private final transient String rowSeparator;
    private final transient String attrSep;

    /**
     * @param pAttrs row attributes to include.
     * @param pLens format length of attribute value.
     * @param rowSep row separator.
     * @param attrSep attribute separator.
     */
    public ViewObjectFormatter(final int[] pAttrs, final int[] pLens,
            final String rowSep, final String attrSep) {
        this.attributes = pAttrs;
        this.lengths = pLens;
        this.rowSeparator = rowSep;
        this.attrSep = attrSep;
    }

    public String format(final ViewObject viewObject)
    {
        final StringBuilder strBuilder = new StringBuilder();
        for (final Row nextRow : viewObject.getAllRowsInRange()) {
            final boolean isEmpty = strBuilder.length() == 0;
            if (this.rowSeparator != null && !isEmpty) {
                strBuilder.append(this.rowSeparator);
            }
            for (int i = 0; i < this.attributes.length; i++) {
                if (this.attrSep != null && i > 0) {
                    strBuilder.append(this.attrSep);
                }
                if (this.lengths != null
                        && this.lengths.length == this.attributes.length) {
                    strBuilder.append(String.format("%-" + this.lengths[i]
                            + "s", nextRow.getAttribute(this.attributes[i])));
                } else {
                    strBuilder.append(nextRow.getAttribute(this.attributes[i]));
                }
            }
        }
        return strBuilder.toString().trim();
    }
}
