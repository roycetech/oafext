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
 * Mocked state of RowSet.
 *
 * @author Royce Remulla
 * @version $Date$
 *
 */
public class RowSetMockState extends IteratorMockState {


    /** isExecuted flag. */
    private transient boolean executed;


    /**
     * @return the executed
     */
    public boolean isExecuted()
    {
        return this.executed;
    }


    /**
     * @param executed the executed to set
     */
    public void setExecuted(final boolean executed)
    {
        this.executed = executed;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return super.toString() + "\nExecuted: " + this.executed;
    }
}
