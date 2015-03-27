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

import oafext.test.server.RowMocker;


/**
 * @version $Date$
 */
public abstract class LinkedRowCallback implements MockRowCallback {

    /** */
    private transient LinkedRowCallback nextCallback;

    @Override
    public final void callback(final RowMocker rowMocker)
    {
        executeCallback(rowMocker);
        if (this.nextCallback != null) {
            this.nextCallback.callback(rowMocker);
        }
    }

    protected abstract void executeCallback(final RowMocker rowMocker);


    /**
     * Add new call back to the chain.
     *
     * @param pNextCallback next RowCallBack.
     */
    public LinkedRowCallback add(final LinkedRowCallback pNextCallback)
    {
        LinkedRowCallback tail = this;
        while (tail.hasNext()) {
            tail = next();
        }
        tail.setNext(pNextCallback);
        return this;
    }

    LinkedRowCallback next()
    {
        return this.nextCallback;
    }

    boolean hasNext()
    {
        return this.nextCallback != null;
    }

    void setNext(final LinkedRowCallback pNextCallback)
    {
        this.nextCallback = pNextCallback;
    }


}
