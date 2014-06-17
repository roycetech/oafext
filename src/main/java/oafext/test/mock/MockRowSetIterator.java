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

import oracle.jbo.RowSetIterator;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * 
 * <pre>
 * $Author: $ 
 * $Date: $
 * </pre>
 */

public class MockRowSetIterator implements CustomMock<RowSetIterator> {


    /** Internal source control version. */
    public static final String RCS_ID = "$Revision: $";


    private transient int size;

    private final RowSetIterator rowSetIterator;

    /**
     * @param pSize Must be greater than or equal to 0;
     */
    public MockRowSetIterator(final int pSize) {
        assert this.size >= 0;
        this.size = pSize;

        this.rowSetIterator = Mockito.mock(RowSetIterator.class);

        Mockito.doAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                return MockRowSetIterator.this.size > 0;

            }
        })
            .when(this.rowSetIterator)
            .hasNext();

        Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation)
                    throws Throwable
            {
                MockRowSetIterator.this.size--;
                return null;
            }
        })
            .when(this.rowSetIterator)
            .next();
    }


    /** {@inheritDoc} */
    @Override
    public RowSetIterator getMock()
    {
        return this.rowSetIterator;
    }

}