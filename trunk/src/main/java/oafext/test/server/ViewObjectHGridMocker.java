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

/**
 * @author royce
 */
public class ViewObjectHGridMocker extends BaseViewObjectMocker {


    /** Simple view linking for HGrid. */
    private final transient int childAttrIdx;


    /**
     * Commented are the mocked methods.
     *
     * @param appModule mock application module instance.
     * @param viewObjectClass view object class.
     * @param pChildAttrIdx attribute index of children.
     */
    ViewObjectHGridMocker(final AppModuleFixture<?> pAmFixture,
            final String pViewObjectName, final int pChildAttrIdx) {

        super(
            pAmFixture,
            pViewObjectName,
            BaseViewObjectMocker.ViewObjectType.HGrid,
            new ViewObjectHGridResponder());

        this.childAttrIdx = pChildAttrIdx;
    }

    int getChildAttrIdx()
    {
        return this.childAttrIdx;
    }

}
