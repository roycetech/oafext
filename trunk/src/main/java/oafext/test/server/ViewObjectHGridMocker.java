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
package oafext.test.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oafext.test.server.responder.ViewObjectHGridResponder;
import oracle.jbo.Row;
import oracle.jbo.domain.Number;

/**
 * @author royce
 */
public class ViewObjectHGridMocker extends BaseViewObjectMocker {


    /** Simple view linking for HGrid. */
    private final transient int childAttrIdx;

    /** Parent ID attribute index. */
    private final transient int parentAttrIdx;


    /** */
    private final transient Map<Number, List<Row>> parentChildMap = new HashMap<Number, List<Row>>();


    /**
     * Commented are the mocked methods.
     *
     * @param appModule mock application module instance.
     * @param viewObjectClass view object class.
     * @param pParentAttrIdx attribute index of parent ID.
     * @param pChildAttrIdx attribute index of children.
     */
    ViewObjectHGridMocker(final AppModuleFixture<?> pAmFixture,
            final String pViewObjectName, final int pParentAttrIdx,
            final int pChildAttrIdx) {

        super(
            pAmFixture,
            pViewObjectName,
            BaseViewObjectMocker.ViewObjectType.HGrid,
            new ViewObjectHGridResponder());

        this.parentAttrIdx = pParentAttrIdx;
        this.childAttrIdx = pChildAttrIdx;

    }

    /**
     * Invoke this upon insertion of the row to the view object.
     *
     * @param parentId parent ID.
     * @param child child row.
     */
    public void registerChild(final Number parentId, final Row child)
    {
        if (this.parentChildMap.get(parentId) == null) {
            this.parentChildMap.put(parentId, new ArrayList<Row>());
        }
        final List<Row> children = this.parentChildMap.get(parentId);
        children.add(child);
    }

    /**
     * @param parentId parent ID.
     */
    public Row[] getChildren(final Number parentId)
    {
        final List<Row> children = this.parentChildMap.get(parentId);
        Row[] childrenArr;
        if (children == null || children.isEmpty()) {
            childrenArr = new Row[0];
        } else {
            childrenArr = children.toArray(new Row[children.size()]);
        }
        return childrenArr;

    }


    /**
     * Return true if attribute is a child.
     *
     * @param attrIdx attribute index to check.
     */
    public boolean isChildAttribute(final int attrIdx)
    {
        return attrIdx == this.childAttrIdx;
    }

    /**
     * @return the parentAttrIdx
     */
    public int getParentAttrIdx()
    {
        return this.parentAttrIdx;
    }

}
