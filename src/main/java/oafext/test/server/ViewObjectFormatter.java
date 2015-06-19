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
 * @param <R> Row type.
 */
public class ViewObjectFormatter<R extends Row> {

    private final transient int[] attributes;
    private final transient int[] lengths;
    private final transient String rowSeparator;
    private final transient String attrSep;

    /**
     * @param pAttrs row attributes to include.
     * @param pLens format length of attribute value. Minimum of 5 to
     *            accommodate null.
     * @param rowSep row separator, can be null.
     * @param attrSep attribute separator.
     */
    public ViewObjectFormatter(final int[] pAttrs, final int[] pLens,
            final String rowSep, final String attrSep) {

        assert pAttrs != null;
        assert pLens != null;


        this.attributes = new int[pAttrs.length];
        System.arraycopy(pAttrs, 0, this.attributes, 0, pAttrs.length);

        this.lengths = new int[pLens.length];
        System.arraycopy(pLens, 0, this.lengths, 0, pLens.length);

        this.rowSeparator = rowSep;
        this.attrSep = attrSep;
    }

    /**
     * Override to control which rows are included.
     *
     * @param row iterated row, will never be null.
     */
    public boolean accept(final R row)
    {
        return true;
    }

    @SuppressWarnings("unchecked")
    public String format(final ViewObject viewObject)
    {
        final StringBuilder strBuilder = new StringBuilder();
        for (final Row nextRow : viewObject.getAllRowsInRange()) {
            final R row = (R) nextRow;
            if (accept(row)) {
                formatRow(strBuilder, row);
            }
        }
        return strBuilder.toString().trim();
    }

    /**
     * @param strBuilder
     * @param nextRow
     */
    protected void formatRow(final StringBuilder strBuilder, final R nextRow)
    {
        for (int i = 0; i < this.attributes.length; i++) {
            if (this.attrSep != null && i > 0) {
                strBuilder.append(this.attrSep);
            }
            if (this.lengths != null
                    && this.lengths.length == this.attributes.length) {
                strBuilder.append(String.format(
                    "%-" + this.lengths[i] + "s",
                    nextRow.getAttribute(this.attributes[i])));
            } else {
                strBuilder.append(nextRow.getAttribute(this.attributes[i]));
            }
        }

        if (this.rowSeparator != null) {
            strBuilder.append(this.rowSeparator);
        }

    }
}
