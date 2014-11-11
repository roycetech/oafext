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
package oafext.test;

/**
 * 
 * <pre>
 * @author $Author$ 
 * @version $Date$
 * </pre>
 */

public class WbMockerHelper {


    /** Internal source control version. */
    public static final String RCS_ID = "$Revision$";

    /**
     * Used to determine package of path. Used to identify if MDS path is
     * outside the package, that it can be by passed.
     * 
     * @param pMdsPath MDS Path.
     * @return package of the MDS path.
     */
    final String getPackage(final String pMdsPath)
    {
        String retval = null; //NOPMD: Reviewed.
        if (pMdsPath != null) {
            retval = pMdsPath.substring(0, pMdsPath.lastIndexOf('/'));
        }
        return retval;
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return super.toString() + " " + RCS_ID;
    }

}