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
package oafext.lang;

import java.util.Map;


/**
 * Common object utility.
 *
 * <pre>
 * $Author: $
 * $Date: $
 * $HeadURL: $
 * </pre>
 *
 * @author Royce.
 */
public class ObjectUtil {


    /**
     * Get value from map and initialize when empty.
     *
     * @param map target map.
     * @param key key.
     * @param nullVal value to initialize map key with in case it is null.
     *
     * @param <K> map key generic type.
     * @param <V> map value generic type.
     */
    public static <K, V> V mapGetInit(final Map<K, V> map, final K key,
                                      final V nullVal)
    {
        if (map.get(key) == null) {
            map.put(key, nullVal);
        }
        return map.get(key);
    }

    /**
     * Compares two objects for equality.
     *
     * @param object1 first object.
     * @param object2 second object.
     */
    public static boolean isEqual(final Object object1, final Object object2)
    {

        boolean retval;
        if (object1 == null && object2 == null) {
            retval = true;
        } else if (object2 == null) {
            retval = object1 != null;
        } else if (object1 == null) {
            retval = object2 != null;
        } else {
            retval = object1.equals(object2);
        }
        return retval;
    }

    /**
     * Return first parameter if non-null, otherwise return the second
     * parameter.
     *
     * @param ifObj return if non-null;
     * @param elseObj return if null.
     * @param <T> generic method, any type of object.
     */
    public <T> T nvl(final T ifObj, final T elseObj)
    {
        T retval;

        if (ifObj == null) {
            retval = elseObj;
        } else {
            retval = ifObj;
        }
        return retval;
    }

    /**
     * PLSQL decode function. NOTE: Use traditional if-then-elseif or switch
     * statement for performance consideration.
     *
     * @param expression is the value to compare. S
     * @param search is the value that is compared against expression. S
     * @param result is the value returned, if expression is equal to search.
     *            default is optional. T
     *
     * @param <S> Search parameter type.
     * @param <T> Result type.
     *
     * @see Unit Test: PrsUtil_decodeTest.
     */
    @SuppressWarnings({
            "PMD.CyclomaticComplexity",
            "unchecked",
            "PMD.CompareObjectsWithEquals" //Required to compare nulls.
    })
    public static <S, T> T decode(final S expression, final S search,
                                  final T... result)
    {

        final java.util.List<S> ifList = new java.util.ArrayList<S>();
        ifList.add(search);

        final java.util.List<T> thenList = new java.util.ArrayList<T>();

        T defaultResult = null; //NOPMD: null default, conditionally redefine.
        if (result == null || result.length == 0) {
            thenList.add(null);

        } else {
            if (result.length % 2 == 0) {
                defaultResult = result[result.length - 1];
            }

            for (int i = 0; i < result.length; i++) {
                if (i % 2 == 1 && i != result.length - 1) {
                    ifList.add((S) result[i]);
                } else if (i % 2 == 0) {
                    thenList.add(result[i]);
                }

            }
        }
        T retval = defaultResult; //NOPMD: initialize by default, conditionally redefine.
        for (int i = 0; i < ifList.size(); i++) {
            final S nextIf = ifList.get(i);
            if (expression == nextIf || nextIf != null
                    && nextIf.equals(expression)) {
                retval = thenList.get(i);
                break;
            }
        }

        return retval;
    }

}
