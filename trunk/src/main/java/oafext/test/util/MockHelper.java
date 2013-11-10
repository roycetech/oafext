package oafext.test.util;

import java.lang.reflect.Method;

public class MockHelper {

    /**
     * Helper method to swallow exception from reflection. WET: With who!?
     * 
     * @param object
     */
    public Object invokeMethod(final Object object, final String methName,
            final Class<?>[] paramType, final Object[] args)
    {
        Object retval = null; //NOPMD: null default, conditionally redefine.
        try {
            final Method method = object.getClass().getDeclaredMethod(methName,
                    paramType);
            method.setAccessible(true);
            retval = method.invoke(object, args);
        } catch (final Exception e) { //NOPMD: too many exceptions.
            //TODO: Log4j2.
            e.printStackTrace();
        }
        return retval;
    }

}
