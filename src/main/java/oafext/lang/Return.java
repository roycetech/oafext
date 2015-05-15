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
package oafext.lang;

import oafext.ann.Revision;


/**
 * Experimental. Idea is to have a single container to track the return value.
 *
 * @author $Author: $
 * @version $Date: $
 *
 */
@Revision("$Revision: $")
public class Return<T> {


    /** */
    private transient T value;


    public Return() {
        this(null);
    }

    public Return(final T defaultValue) {
        this.value = defaultValue;
    }

    /**
     * @return the value
     */
    public T get()
    {
        return this.value;
    }

    /**
     * @param value the value to set
     */
    public void set(final T value)
    {
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + '('
                + (this.value == null ? null : this.value) + ')';
    }

}
