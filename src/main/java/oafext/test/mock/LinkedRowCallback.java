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
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;


/**
 * @version $Date$
 *
 * @param <R> specific Row type.
 * @param <V> View Object type.
 */
public abstract class LinkedRowCallback<R extends ViewRowImpl, V extends ViewObjectImpl>
        implements MockRowCallback<R, V> {

    /** */
    private transient LinkedRowCallback<R, V> nextCallback;

    /** {@inheritDoc} */
    @Override
    public final void callback(final RowMocker<R, V> rowMocker,
                               final boolean setUp)
    {
        executeCallback(rowMocker);
        if (this.nextCallback != null) {
            this.nextCallback.callback(rowMocker, setUp);
        }
    }

    protected abstract void executeCallback(final RowMocker<R, V> rowMocker);


    /**
     * Add new call back to the chain.
     *
     * @param pNextCallback next RowCallBack.
     */
    public LinkedRowCallback<R, V> add(final LinkedRowCallback<R, V> pNextCallback)
    {
        LinkedRowCallback<R, V> tail = this;
        while (tail.hasNext()) {
            tail = next();
        }
        tail.setNext(pNextCallback);
        return this;
    }

    LinkedRowCallback<R, V> next()
    {
        return this.nextCallback;
    }

    boolean hasNext()
    {
        return this.nextCallback != null;
    }

    void setNext(final LinkedRowCallback<R, V> pNextCallback)
    {
        this.nextCallback = pNextCallback;
    }


}
