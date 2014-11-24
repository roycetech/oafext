package oafext.test.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author royce
 */
public class MockHelper {


    /** */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(MockHelper.class);


    /**
     * Find a method matching only the name.
     * 
     * @param klass class to find method from.
     * @param methodName method name.
     */
    @SuppressWarnings("PMD.OnlyOneReturn")
    public Method findMethod(final Class klass, final String methodName)
    {

        for (final Method method : klass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }


    /**
     * Helper method to swallow exception from reflection. Zero parameter only.
     * 
     * @param mock mock to invoke method from.
     * @param methodName method name.
     * 
     */
    public Object invokeMethod(final Object mock, final String methodName)
    {
        Object retval = null; //NOPMD: null default, conditionally redefine.
        try {
            retval = mock
                .getClass()
                .getMethod(methodName, new Class[0])
                .invoke(mock, new Object[0]);
        } catch (final IllegalAccessException e) {
            LOGGER.error(e.getMessage() + ':' + methodName, e);
        } catch (final IllegalArgumentException e) {
            LOGGER.error(e.getMessage() + ':' + methodName, e);
        } catch (final InvocationTargetException e) {
            LOGGER.error(e.getMessage() + ':' + methodName, e);
        } catch (final NoSuchMethodException e) {
            LOGGER.error(e.getMessage() + ':' + methodName, e);
        } catch (final SecurityException e) {
            LOGGER.error(e.getMessage() + ':' + methodName, e);
        }
        return retval;
    }


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
            final Method method = object.getClass().getMethod(
                methName,
                paramType);
            //method.setAccessible(true);
            retval = method.invoke(object, args);
        } catch (final Exception e) { //NOPMD: too many exceptions.
            LOGGER.error(e.getMessage(), e);
        }
        return retval;
    }

}
