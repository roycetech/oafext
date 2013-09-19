package oafext.logging;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import oracle.apps.fnd.framework.OAFwkConstants;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.apps.fnd.framework.server.OADBTransaction;
import oracle.apps.fnd.framework.webui.OAPageContext;
import oracle.jbo.Row;
import oracle.jbo.RowIterator;
import oracle.jbo.RowSet;
import oracle.jbo.ViewObject;

/**
 * Simple logger. Configuration via property file or via runtime. Will look for log4oaf.properties in root classes
 * directory.
 * 
 * WARNING: Not currently compatible with IDE's other than JDEV 10.1.3.3.
 * 
 * Example log4oaf.properties. Copy this to class folder (e.g. U:\JDev\jdevhome\jdev\myclasses)
 * 
 * 
 * #log4oaf.properties #defaults to INFO log4oaf.defaultLevel=INFO
 * 
 * #defaults to yes log4oaf.showMethod=yes
 * 
 * #defaults to yes #log4oaf.showPackage=yes
 * 
 * #defaults to no log4oaf.printToConsole=yes
 * 
 * #defaults to yes log4oaf.isDeployed=no
 * 
 * <pre>
 * $Author$ 
 * $Date$ 
 * $HeadURL$
 * </pre>
 * 
 * @author royce.com.
 */
public class OafLogger {

    /** Standard Oracle versioning. */
    public static final String RCS_ID = "$Revision$";

    /** Singleton instance. */
    private static final OafLogger INSTANCE = new OafLogger();

    /** This is a utility logging class. */
    private OafLogger() {}

    /**
     * This will be the subject of the logger. Set this using getInstance(Class) otherwise the calling class will be the
     * active Class.
     */
    private static String activeClass = null;

    /**
     * Factory method.
     * 
     * @return Singleton instance.
     */
    public static OafLogger getInstance()
    {
        if (!initialized) {
            try {
                final ResourceBundle resBundle = ResourceBundle.getBundle(RESOURCE_NAME);
                final String cfgDefaultLevel = resBundle.getString("log4oaf.defaultLevel");
                final Map<String, Integer> levelToStr = new HashMap<String, Integer>();
                levelToStr.put("INFO", Level.INFO);
                levelToStr.put("DEBUG", Level.DEBUG);
                levelToStr.put("WARN", Level.WARN);
                levelToStr.put("ERROR", Level.ERROR);
                levelToStr.put("OFF", Level.OFF);

                INSTANCE.defaultLevel = levelToStr.get(cfgDefaultLevel) == null ? Level.INFO : levelToStr
                    .get(cfgDefaultLevel);
                INSTANCE.showMethod = getResourceValue(resBundle, "log4oaf.showMethod", INSTANCE.showMethod);
                INSTANCE.showPackage = getResourceValue(resBundle, "log4oaf.showPackage", INSTANCE.showPackage);
                INSTANCE.shortPackage = getResourceValue(resBundle, "log4oaf.shortPackage", INSTANCE.shortPackage);
                INSTANCE.basePackage = resBundle.getString("log4oaf.basepkg");
                INSTANCE.printToConsole = getResourceValue(resBundle, "log4oaf.printToConsole", INSTANCE.printToConsole);
                INSTANCE.deployed = getResourceValue(resBundle, "log4oaf.isDeployed", INSTANCE.deployed);

                for (final Enumeration<String> enu = resBundle.getKeys(); enu.hasMoreElements();) {
                    final String logger = enu.nextElement();
                    if (logger.startsWith("log4oaf.logger.") && !"log4oaf.logger.".equals(logger.trim())) {
                        final String classPrefix = logger.substring(15);
                        INSTANCE.setLevel(classPrefix, levelToStr.get(resBundle.getString(logger).trim()));
                    }
                }
                INSTANCE.print("Completed configuring from " + RESOURCE_NAME + ".properties", OafLogger.Level.INFO);
            } catch (final MissingResourceException mre) {
                INSTANCE.print(
                    "INFO Resource " + RESOURCE_NAME + " was not found. Configure from client calls.",
                    OafLogger.Level.WARN);
            }
            initialized = true;
        }

        activeClass = "";
        return INSTANCE;
    }

    private static final Boolean getResourceValue(final ResourceBundle resBundle, final String resourceKey,
            final boolean defaultValue)
    {
        Boolean retval = null;
        try {
            String resValue = resBundle.getString(resourceKey);
            if (resValue != null) {
                resValue = resValue.trim();
            }

            retval = "yes".equalsIgnoreCase(resValue) || "true".equalsIgnoreCase(resValue);
        } catch (final MissingResourceException mre) { //NOPMD Reviewed.
            retval = defaultValue;
        }
        return retval;
    }

    /**
     * Factory method.
     * 
     * @return Singleton instance.
     */
    public static <T> OafLogger getInstance(final Class<T> clazz)
    {
        if (clazz == null) {
            getInstance();
        } else {
            activeClass = clazz.getName();
        }
        return INSTANCE;
    }

    private static final String RESOURCE_NAME = "log4oaf";

    public void setShortPackage(final boolean shortPackage)
    {
        this.shortPackage = shortPackage;
    }

    public class Level {
        public static final int OFF = 0;
        public static final int IGNORE = 1;
        public static final int DEBUG = 2;
        public static final int INFO = 3;
        public static final int WARN = 4;
        public static final int ERROR = 5;
    }

    private static final String[] LOG_PREFIX = {
            "IGNO",
            "DEBUG",
            "INFO",
            "WARN",
            "ERROR" };

    private final transient Set<String> ignoreSet = new HashSet<String>();
    private boolean printToConsole = false;
    private boolean showPackage = true;
    private boolean shortPackage = true;
    private String basePackage = "";
    private boolean showMethod = true;
    private transient boolean deployed = true;
    private transient int defaultLevel = Level.INFO;
    private final transient Map<String, Integer> classLevel = new LinkedHashMap<String, Integer>();

    /** Flag for initialize from properties file. */
    private static boolean initialized = false;

    static final int IDX_LOCAL_CALL = 3;
    static final int IDX_DEPLOYED_CALL = 2;

    static final String SEP_MSG = " - ";

    /**
     * Add to list of prefix that will not print in the console.
     * 
     * @deprecated Use setLevel(String, Level).
     * @param string prefix of objects to be excluded.
     */
    @Deprecated
    public void addToIgnore(final String string)
    {
        if (string != null) {
            this.ignoreSet.add(string);
        }
    }

    public <T> void setLevel(final Class<T> source, final int level)
    {
        if (source != null && level <= Level.ERROR) {
            setLevel(source.getName(), level);
        }
    }

    public void setLevel(final String source, final int level)
    {
        if (source != null && level <= Level.ERROR) {
            this.classLevel.put(source, level);
        }
    }

    /* 1. Logger method: OAPagecontext */

    public void info(final OAPageContext pageContext, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        if (pageContext == null) {
            log(message, ste, Level.INFO);
        } else {
            log(pageContext, message, ste, Level.INFO);
        }
    }

    public void debug(final OAPageContext pageContext, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        if (pageContext == null) {
            log(message, ste, Level.DEBUG);
        } else {
            log(pageContext, message, ste, Level.DEBUG);
        }
    }

    public void warn(final OAPageContext pageContext, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        if (pageContext == null) {
            log(message, ste, Level.WARN);
        } else {
            log(pageContext, message, ste, Level.WARN);
        }
    }

    public void error(final OAPageContext pageContext, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(pageContext, message, ste, Level.ERROR);
    }

    /**
     * For controller code or object with access to page context.
     * 
     * @param pageContext
     * @param message
     */
    public void log(final OAPageContext pageContext, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(pageContext, message, ste, this.defaultLevel);
    }

    private void log(final OAPageContext pageContext, final Object pMessage, final StackTraceElement ste,
            final int level)
    {
        final String message = pMessage instanceof RowSet ? getVOValues((RowSet) pMessage) : pMessage.toString();
        final String className = getClassNameDisp(ste);
        final String methName = getMethodDisp(ste);
        final int lineNo = ste.getLineNumber();

        if (pageContext.isDiagnosticMode()) {
            pageContext.writeDiagnostics(className, methName + "(" + lineNo + ")" + message, OAFwkConstants.STATEMENT);
        }
        if (pageContext.isDeveloperMode() && getPrintToConsole() && isPrinted(getClassName(ste), level)) {
            print(className + methName + ":" + lineNo + SEP_MSG + message, level);
        }
    }

    /**
     * For controller code or object with access to page context.
     * 
     * @param pageContext
     * @param message
     */
    public void log(final OAPageContext pageContext, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(pageContext, dispMessage, ste, this.defaultLevel);
    }

    public void info(final OAPageContext pageContext, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(pageContext, dispMessage, ste, Level.INFO);
    }

    public void debug(final OAPageContext pageContext, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(pageContext, dispMessage, ste, Level.DEBUG);
    }

    public void warn(final OAPageContext pageContext, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(pageContext, dispMessage, ste, Level.WARN);
    }

    public void error(final OAPageContext pageContext, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(pageContext, dispMessage, ste, Level.ERROR);
    }

    /* 2. Logger method: OAApplicationModule */

    private void log(final OAApplicationModuleImpl appModule, final Object pMessage, final StackTraceElement ste,
            final int level)
    {

        Object message = pMessage;
        if (pMessage instanceof RowSet) {
            message = getVOValues((RowSet) pMessage);
        }
        if (appModule == null) {
            log(message, ste, level);
        } else {
            final String classNameDisp = getClassNameDisp(ste);
            final String methName = getMethodDisp(ste);
            final int lineNo = ste.getLineNumber();
            if (appModule.getOADBTransaction().isDiagnosticMode()) {
                appModule.writeDiagnostics(
                    classNameDisp,
                    methName + "(" + lineNo + ")" + message,
                    OAFwkConstants.STATEMENT);
            }

            if (appModule.getOADBTransaction().isDeveloperMode() && getPrintToConsole()
                    && isPrinted(getClassName(ste), level)) {
                print(classNameDisp + methName + ":" + lineNo + SEP_MSG + message, level);
            }
        }
    }

    /**
     * @param appModule ApplicationModuleImpl
     * @param message
     */
    public void log(final OAApplicationModuleImpl appModule, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (appModule == null) {
            log(message, ste, this.defaultLevel);
        } else {
            log(appModule, message, ste, this.defaultLevel);
        }
    }

    public void info(final OAApplicationModuleImpl appModule, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (appModule == null) {
            log(message, ste, Level.INFO);
        } else {
            log(appModule, message, ste, Level.INFO);
        }
    }

    public void debug(final OAApplicationModuleImpl appModule, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (appModule == null) {
            log(message, ste, Level.DEBUG);
        } else {
            log(appModule, message, ste, Level.DEBUG);
        }
    }

    public void warn(final OAApplicationModuleImpl appModule, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (appModule == null) {
            log(message, ste, Level.WARN);
        } else {
            log(appModule, message, ste, Level.WARN);
        }
    }

    public void error(final OAApplicationModuleImpl appModule, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (appModule == null) {
            log(message, ste, Level.ERROR);
        } else {
            log(appModule, message, ste, Level.ERROR);
        }
    }

    /**
     * @param appModule
     * @param message
     */
    public void log(final OAApplicationModuleImpl appModule, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        final String dispMessage = getDispMessage(message, exception);

        if (appModule == null) {
            log(dispMessage, ste, this.defaultLevel);
        } else {
            log(appModule, dispMessage, ste, this.defaultLevel);
        }
    }

    public void info(final OAApplicationModuleImpl appModule, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        final String dispMessage = getDispMessage(message, exception);
        if (appModule == null) {
            log(dispMessage, ste, Level.INFO);
        } else {
            log(appModule, dispMessage, ste, Level.INFO);
        }
    }

    public void debug(final OAApplicationModuleImpl appModule, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        final String dispMessage = getDispMessage(message, exception);
        if (appModule == null) {
            log(dispMessage, ste, Level.DEBUG);
        } else {
            log(appModule, dispMessage, ste, Level.DEBUG);
        }
    }

    public void warn(final OAApplicationModuleImpl appModule, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        final String dispMessage = getDispMessage(message, exception);
        if (appModule == null) {
            log(dispMessage, ste, Level.WARN);
        } else {
            log(appModule, dispMessage, ste, Level.WARN);
        }
    }

    public void error(final OAApplicationModuleImpl appModule, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        final String dispMessage = getDispMessage(message, exception);
        if (appModule == null) {
            log(dispMessage, ste, Level.ERROR);
        } else {
            log(appModule, dispMessage, ste, Level.ERROR);
        }
    }

    /* 3. Logger method: OADBTransactionImpl */
    //TODO: Complete family of method, and method with throwable parameter.

    public void log(final OADBTransaction trx, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (trx == null) {
            log(message, ste, this.defaultLevel);
        } else {
            log(trx, message, ste, this.defaultLevel);
        }
    }

    public void info(final OADBTransaction trx, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (trx == null) {
            log(message, ste, Level.INFO);
        } else {
            log(trx, message, ste, Level.INFO);
        }
    }

    public void debug(final OADBTransaction trx, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (trx == null) {
            log(message, ste, Level.DEBUG);
        } else {
            log(trx, message, ste, Level.DEBUG);
        }
    }

    public void error(final OADBTransaction trx, final String message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (trx == null) {
            log(message, ste, Level.ERROR);
        } else {
            log(trx, message, ste, Level.ERROR);
        }
    }

    public void debug(final OADBTransaction trx, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(trx, dispMessage, ste, Level.DEBUG);
    }

    public void error(final OADBTransaction trx, final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(trx, dispMessage, ste, Level.ERROR);
    }

    private void log(final OADBTransaction trx, final Object pMessage, final StackTraceElement ste, final int level)
    {
        Object message = pMessage;
        if (pMessage instanceof ViewObject) {
            message = getVOValues((ViewObject) pMessage);
        }
        if (trx == null) {
            log(message, ste, level);
        } else {
            final String classNameDisp = getClassNameDisp(ste);
            final String methName = getMethodDisp(ste);
            final int lineNo = ste.getLineNumber();
            if (trx.isDiagnosticMode()) {
                trx.writeDiagnostics(
                    getClassName(ste),
                    methName + "(" + lineNo + ")" + message,
                    OAFwkConstants.STATEMENT);
            }

            if (trx.isDeveloperMode() && getPrintToConsole() && isPrinted(getClassName(ste), level)) {
                print(classNameDisp + methName + ":" + lineNo + SEP_MSG + message, level);
            }
        }
    }

    /* 4. Logger method: ViewObject */

    private void log(final ViewObject viewObject, final StackTraceElement ste, final int level)
    {
        if (viewObject == null) {
            log((Object) viewObject, ste, level);
        } else {
            final String classNameDisp = getClassNameDisp(ste);
            final String methName = getMethodDisp(ste);
            final int lineNo = ste.getLineNumber();
            final String message = getVOValues(viewObject);

            boolean devMode = false;
            if (viewObject.getApplicationModule() instanceof OAApplicationModuleImpl) {
                final OAApplicationModuleImpl appModule = (OAApplicationModuleImpl) viewObject.getApplicationModule();
                devMode = appModule.getOADBTransaction().isDeveloperMode();
                if (appModule.getOADBTransaction().isDiagnosticMode()) {
                    appModule.writeDiagnostics(
                        classNameDisp,
                        methName + "(" + lineNo + ")" + message,
                        OAFwkConstants.STATEMENT);
                }
            }

            if (devMode && getPrintToConsole() && isPrinted(getClassName(ste), level)) {
                print(classNameDisp + methName + ":" + lineNo + SEP_MSG + message, level);
            }
        }
    }

    public void log(final ViewObject viewObject)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (viewObject == null) {
            log(viewObject, ste, this.defaultLevel);
        } else {
            log(viewObject, ste, this.defaultLevel);
        }
    }

    public void info(final ViewObject viewObject)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (viewObject == null) {
            log((Object) viewObject, ste, Level.INFO);
        } else {
            log(viewObject, ste, Level.INFO);
        }
    }

    public void debug(final ViewObject viewObject)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (viewObject == null) {
            log((Object) viewObject, ste, Level.DEBUG);
        } else {
            log(viewObject, ste, Level.DEBUG);
        }
    }

    /* 5. Logger method: Object */

    private void log(final Object pMessage, final StackTraceElement ste, final int level)
    {
        Object message = pMessage;
        if (pMessage instanceof ViewObject) {
            message = getVOValues((ViewObject) pMessage);
        } else if (pMessage instanceof RowIterator) {
            message = getVOValues((RowIterator) pMessage);
        }

        final String classNameDisp = getClassNameDisp(ste);
        final String methName = getMethodDisp(ste);
        final int lineNo = ste.getLineNumber();
        if (getPrintToConsole() && isPrinted(getClassName(ste), level)) {
            print(classNameDisp + methName + ":" + lineNo + SEP_MSG + message, level);
        }
    }

    public void log(final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(message, ste, this.defaultLevel);
    }

    public void info(final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(message, ste, Level.INFO);
    }

    public void ignore(final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(message, ste, Level.IGNORE);
    }

    /**
     * Note changes are for testing purpose on eclipse only in case I accidentally commit.
     * 
     * @param message
     */
    public void debug(final Object message)
    {
        final int lastIdx = Thread.currentThread().getStackTrace().length - 1;
        StackTraceElement ste = null;
        if (Thread.currentThread().getStackTrace()[lastIdx].getClassName().contains("org.eclipse.jdt")) {//eclipse local.
            ste = Thread.currentThread().getStackTrace()[IDX_DEPLOYED_CALL];
        } else if (IDX_LOCAL_CALL > lastIdx) {
            ste = Thread.currentThread().getStackTrace()[lastIdx];
        } else {
            ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL : IDX_LOCAL_CALL];
        }

        log(message, ste, Level.DEBUG);
    }

    public void warn(final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(message, ste, Level.WARN);
    }

    public void error(final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(message, ste, Level.ERROR);
    }

    public void log(final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(dispMessage, ste, this.defaultLevel);
    }

    public void info(final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(dispMessage, ste, Level.INFO);
    }

    public void debug(final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(dispMessage, ste, Level.DEBUG);
    }

    public void warn(final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(dispMessage, ste, Level.WARN);
    }

    public void error(final Object message, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(dispMessage, ste, Level.ERROR);
    }

    public void log(final Throwable message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(message, ste, this.defaultLevel);
    }

    public void info(final Throwable message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(message, ste, Level.INFO);
    }

    public void debug(final Throwable message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(message, ste, Level.DEBUG);
    }

    public void warn(final Throwable message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(message, ste, Level.WARN);
    }

    public void error(final Throwable message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(message, ste, Level.ERROR);
    }

    private void log(final Throwable message, final StackTraceElement ste, final int level)
    {
        final String classNameDisp = getClassNameDisp(ste);
        final String methName = getMethodDisp(ste);
        final int lineNo = ste.getLineNumber();
        if (getPrintToConsole() && isPrinted(getClassName(ste), level)) {
            print(classNameDisp + methName + ":" + lineNo + SEP_MSG + "\n" + stackTraceToString(message), level);
        }
    }

    /** Test method only. */
    private void error()
    {
        throw new IllegalArgumentException("Testing Exception");
    }

    /**
     * Converts the stack trace to a string object.
     * 
     * @param exception - the throwable instance of which to translate.
     * @return String representation of the stack trace.
     * @exception IllegalArgumentException when the e parameter is null.
     */
    String stackTraceToString(final Throwable exception)
    {
        String retval = null;
        if (exception != null) {
            final StringBuilder strBuilder = new StringBuilder();
            final StackTraceElement[] steArr = exception.getStackTrace();
            for (final StackTraceElement stackTraceElement : steArr) {
                strBuilder.append(stackTraceElement.toString());
                strBuilder.append('\n');
            }
            retval = strBuilder.toString();
        }
        return retval;
    }

    /** Checks the className against the ignore Set; */
    private boolean isPrinted(final String className, final int level)
    {
        boolean retval = true;
        if (className != null) {
            retval ^= isInIgnoreList(className);
            if (retval) {
                retval = isUnIgnoredPrinted(retval, className, level);
            }
        }
        return retval;
    }

    private boolean isUnIgnoredPrinted(final boolean pCurrVal, final String className, final int level)
    {
        boolean retval = pCurrVal;
        boolean isIdentified = false;
        for (final String nextClsLvl : this.classLevel.keySet()) {
            if (className.startsWith(nextClsLvl)) {
                retval = level >= this.classLevel.get(nextClsLvl) && this.classLevel.get(nextClsLvl) != Level.OFF;
                isIdentified = true;
            }
        }
        if (!isIdentified) {
            retval = level >= this.defaultLevel;
        }
        return retval;
    }

    boolean isInIgnoreList(final String className)
    {
        boolean retval = false;
        for (final String nextIgnore : this.ignoreSet) {
            if (className.startsWith(nextIgnore)) {
                retval = true;
                break;
            }
        }
        return retval;
    }

    String getDispMessage(final Object message, final Throwable exception)
    {
        return message == null ? "null\n" : message.toString() + '\n' + stackTraceToString(exception);
    }

    String getMethodDisp(final StackTraceElement ste)
    {
        return isShowMethod() ? '.' + ste.getMethodName() : "";
    }

    String getClassNameDisp(final StackTraceElement ste)
    {
        final String className = "".equals(activeClass) ? ste.getClassName() : activeClass;
        String retval = null;
        if (isShowPackage()) {
            retval = className;
            if (isShortPackage()) {
                retval = className.substring(INSTANCE.basePackage.length());
            }
        } else {
            retval = className.substring(className.lastIndexOf('.') + 1);
        }
        return retval;
    }

    String getClassName(final StackTraceElement ste)
    {
        return "".equals(activeClass) ? ste.getClassName() : activeClass;
    }

    private void print(final String message, final int level)
    {
        if (Level.ERROR == level || Level.WARN == level) {
            System.err.println(padSpace(LOG_PREFIX[level - 1]) + " " + message); //NOPMD No other way for now.            
        } else {
            System.out.println(padSpace(LOG_PREFIX[level - 1]) + " " + message); //NOPMD No other way for now.                        
        }
    }

    private String padSpace(final String level)
    {
        String retval = null;
        if (level != null) {
            switch (level.length()) {
                case 4:
                    retval = level + " ";
                    break;
                case 3:
                    retval = level + "  ";
                    break;
                default:
                    retval = level;
            }
        }
        return retval;
    }

    public void setPrintToConsole(final boolean printToConsole)
    {
        this.printToConsole = printToConsole;
    }

    public boolean getPrintToConsole()
    {
        return this.printToConsole;
    }

    public void setShowPackage(final boolean showPackage)
    {
        this.showPackage = showPackage;
    }

    private boolean isShowPackage()
    {
        return this.showPackage;
    }

    public void setShowMethod(final boolean showMethod)
    {
        this.showMethod = showMethod;
    }

    private boolean isShowMethod()
    {
        return this.showMethod;
    }

    public void setDeployedMode(final boolean flag)
    {
        this.deployed = flag;
    }

    /** Override. */
    @Override
    public String toString()
    {
        return getClass().getName();
    }

    public static void main(final String[] args)
    {

        final OafLogger logger = OafLogger.getInstance();
        logger.setPrintToConsole(true);
        logger.setLevel(OafLogger.class, Level.DEBUG);

        try {
            logger.error();
        } catch (final IllegalArgumentException iae) {
            logger.log("Default: ", iae);
            logger.info("INFO: ", iae);
            logger.debug("DEBUG: ", iae);
            logger.warn("WARN: ", iae);
            logger.error("ERROR: ", iae);
        }
    }

    public void setDefaultLevel(final int defaultLevel)
    {
        this.defaultLevel = defaultLevel;
    }

    public int getDefaultLevel()
    {
        return this.defaultLevel;
    }

    private boolean isShortPackage()
    {
        return this.shortPackage;
    }

    /**
     * @param viewObject
     * @return
     */
    public String getVOValues(final ViewObject viewObject)
    {
        return getVOValues((RowSet) viewObject);
    }

    /**
     * @return
     */
    public String getVOValues(final RowIterator rowIter)
    {
        final StringBuilder strBuilder = new StringBuilder();
        return traverseRowSet(rowIter, false, strBuilder).toString();
    }


    /**
     * NOTE: Recursive!
     * 
     * @param rowSet
     * @return
     */
    public String getVOValues(final RowSet rowSet, final boolean doExecute)
    {
        final StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(rowSet.getName());
        if (rowSet != null && doExecute && !rowSet.isExecuted()) {
            rowSet.executeQuery();
        }

        if (rowSet != null && rowSet.isExecuted()) {
            traverseRowSet(rowSet, doExecute, strBuilder);
        } else {
            strBuilder.append(" - Not executed.");
        }
        return strBuilder.toString();
    }

    /**
     * @param rowSet
     * @param doExecute
     * @param strBuilder
     */
    StringBuilder traverseRowSet(final RowIterator rowSet, final boolean doExecute, final StringBuilder strBuilder)
    {
        strBuilder.append(" Count: ");
        strBuilder.append(rowSet.getAllRowsInRange().length + "\n");
        int count = 1;
        for (final Row nextRow : rowSet.getAllRowsInRange()) {
            strBuilder.append("Row " + count + ": ");
            for (final String nextAttr : nextRow.getAttributeNames()) {
                final Object attrValue = nextRow.getAttribute(nextAttr);
                if (attrValue instanceof RowSet) {
                    final RowSet childRowSet = (RowSet) attrValue;
                    strBuilder.append("\n");
                    strBuilder.append("Sub VO: " + childRowSet.getName());
                    strBuilder.append(getVOValues(childRowSet, doExecute));
                } else {
                    strBuilder.append(nextAttr + "=" + attrValue + ",");
                }
            }
            count++;
            strBuilder.append("\n");
        }
        return strBuilder;
    }

    /**
     * NOTE: Recursive!
     * 
     * @param rowSet
     * @return
     */
    public String getVOValues(final RowSet rowSet)
    {
        return getVOValues(rowSet, false);
    }
}
