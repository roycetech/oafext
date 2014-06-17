/**
 * Copyright 2014 Asian Development Bank. All rights reserved.
 * ADB PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Project Name:  ISTSII ADB Manage Procurement Review System.
 * Module :  Common
 * Use Case:
 * Purpose:
 * Design Document Reference:  <full path where the design document is and page no>
 * File Path: $HeadURL$
 *
 * Created by: Royce Remulla (R39)
 *
 * Oracle ERP Release: 12.0.5
 * Oracle ERP Module: Custom.
 */
package oafext.logging;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import oafext.lang.ObjectUtil;
import oracle.apps.fnd.framework.OAFwkConstants;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;
import oracle.apps.fnd.framework.server.OADBTransaction;
import oracle.apps.fnd.framework.webui.OAPageContext;
import oracle.apps.fnd.framework.webui.OAWebBeanConstants;
import oracle.apps.fnd.framework.webui.beans.OARawTextBean;
import oracle.jbo.Row;
import oracle.jbo.RowIterator;
import oracle.jbo.RowSet;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

/**
 * Simple logger. Configuration via property file or via runtime. Will look for
 * log4oaf.properties in root classes directory.
 * 
 * WARNING: Not currently compatible with IDE's other than JDEV 10.1.3.3.
 * 
 * Example log4oaf.properties. Copy this to class folder (e.g.
 * U:\JDev\jdevhome\jdev\myclasses)
 * 
 * <pre>
 * #log4oaf.properties
 *  
 * #defaults to INFO 
 * log4oaf.defaultLevel=INFO
 * 
 * #defaults to yes 
 * log4oaf.showMethod=yes
 * 
 * #defaults to yes 
 * #log4oaf.showPackage=yes
 * 
 * #defaults to no 
 * log4oaf.printToConsole=yes
 * 
 * #defaults to yes 
 * log4oaf.isDeployed=no
 * 
 * # log categories 
 * log4oaf.logger.adb.oracle.apps.pa.workout=DEBUG 
 * log4oaf.logger.adb.oracle.apps.pa.workplan=OFF
 * log4oaf.logger.adb.oracle.apps.pa.util.server=DEBUG
 * </pre>
 * 
 * <pre>
 * $Author$ 
 * $Date$
 * </pre>
 */
@SuppressWarnings({
        "PMD.GodClass" /* God of logs. */,
        "PMD.ExcessiveClassLength",
        "PMD.ExcessivePublicCount",
        "PMD.TooManyMethods" })
public class OafLogger {


    /** Standard Oracle versioning. */
    public static final String RCS_ID = "$Revision$";


    /** Allow System.out.println flag. */
    private static final boolean ENABLE_SYSOUT = false;

    /** Maximum length of RCS_ID for display. */
    static final int MAX_RCSLEN = 20;

    /** Transient session logs captured during processFormRequest. */
    static final String DEFERRED_LOGS = OafLogger.class.getName();

    /** Singleton instance. */
    private static final OafLogger INSTANCE = new OafLogger();

    /**
     * This will be the subject of the logger. Set this using getInstance(Class)
     * otherwise the calling class will be the active Class.
     */
    private static String activeClass;


    /** Common component utility class. */
    private static final transient ObjectUtil OBJ_UTIL = new ObjectUtil();


    /** Original stream used to toggle blocking of System.out.println. */
    private static final PrintStream ORIG_STREAM = System.out;

    /** Empty stream used to block System.out.println. */
    private static final PrintStream EMPTY_STREAM = new PrintStream(
        new OutputStream() {
            public void write(final int unused)
            {
                //NO-OP
            }
        });

    /** Logging properties config file. */
    private static final String RESOURCE_NAME = "log4oaf";

    /** */
    private static final String[] LOG_PREFIX = {
            "IGNO",
            "DEBUG",
            "INFO",
            "WARN",
            "ERROR" };


    private final transient Set<String> ignoreSet = new HashSet<String>();

    /** Flag to enable/disable printing to standard output. */
    private transient boolean printToConsole;
    /** Flag to enable/disable class name or simple name. */
    private transient boolean showPackage = true;
    /**
     * Flag to enable/disable shortened (remove 'adb.oracle.apps' prefix)
     * package.
     */
    private transient boolean shortPackage = true;
    /** Base package of project. */
    private transient String basePackage = "";
    /** Flag to show or hide the calling method. */
    private boolean showMethod = true;
    /**
     * Flag to determine if running on local JDeveloper or E-Business Suite
     * instance..
     */
    private transient boolean deployed = true;
    private transient int defaultLevel = Level.INFO;
    private final transient Map<String, Integer> classLevel = new LinkedHashMap<String, Integer>();

    /** Flag for initialize from properties file. */
    private static boolean initialized;

    /** Stack caller position for JDK 6. Use 3 for Vanilla JDEV using JDK 1.5. */
    static final int IDX_LOCAL_CALL = 2;
    /** Stack caller position server JDK. */
    static final int IDX_DEPLOYED_CALL = 2;

    /** Level - Message separator. */
    static final String SEP_MSG = " - ";


    /** Log levels. */
    public class Level {

        /** Will never show. */
        public static final int OFF = 0;
        /** Most detailed. */
        public static final int IGNORE = 1;
        /** Verbose. */
        public static final int DEBUG = 2;
        /** */
        public static final int INFO = 3;
        /** Important, appears in red. */
        public static final int WARN = 4;
        /** Critical, appears in red. */
        public static final int ERROR = 5;

        /** Utility method. */
        private Level() {}
    }


    /** This is a utility logging class. */
    private OafLogger() {}


    /**
     * Factory method.
     * 
     * @return Singleton instance.
     */
    public static OafLogger getInstance()
    {
        if (!initialized) {
            try {
                final ResourceBundle resBundle = ResourceBundle
                    .getBundle(RESOURCE_NAME);
                final String cfgDefaultLevel = resBundle
                    .getString("log4oaf.defaultLevel");
                final Map<String, Integer> levelToStr = new HashMap<String, Integer>();
                levelToStr.put("INFO", Level.INFO);
                levelToStr.put("DEBUG", Level.DEBUG);
                levelToStr.put("WARN", Level.WARN);
                levelToStr.put("ERROR", Level.ERROR);
                levelToStr.put("OFF", Level.OFF);

                INSTANCE.defaultLevel = getUtil().nvl(
                    levelToStr.get(cfgDefaultLevel),
                    Level.INFO);

                INSTANCE.showMethod = getResourceValue(
                    resBundle,
                    "log4oaf.showMethod",
                    INSTANCE.showMethod);
                INSTANCE.showPackage = getResourceValue(
                    resBundle,
                    "log4oaf.showPackage",
                    INSTANCE.showPackage);
                INSTANCE.shortPackage = getResourceValue(
                    resBundle,
                    "log4oaf.shortPackage",
                    INSTANCE.shortPackage);
                INSTANCE.basePackage = resBundle.getString("log4oaf.basepkg");
                INSTANCE.printToConsole = getResourceValue(
                    resBundle,
                    "log4oaf.printToConsole",
                    INSTANCE.printToConsole);
                INSTANCE.deployed = getResourceValue(
                    resBundle,
                    "log4oaf.isDeployed",
                    INSTANCE.deployed);

                for (final Enumeration<String> enu = resBundle.getKeys(); enu
                    .hasMoreElements();) {
                    final String logger = enu.nextElement();
                    if (logger.startsWith("log4oaf.logger.")
                            && !"log4oaf.logger.".equals(logger.trim())) {
                        final String classPrefix = logger.substring(15);
                        INSTANCE.setLevel(
                            classPrefix,
                            levelToStr.get(resBundle.getString(logger).trim()));
                    }
                }

                if (!ENABLE_SYSOUT) {
                    System.setOut(EMPTY_STREAM);
                }

                INSTANCE.print("Completed configuring from " + RESOURCE_NAME
                        + ".properties", OafLogger.Level.INFO);
            } catch (final MissingResourceException mre) {
                INSTANCE.print(
                    "INFO Resource " + RESOURCE_NAME
                            + " was not found. Configure from client calls.",
                    OafLogger.Level.WARN);
            }
            initialized = true;
        }

        activeClass = "";
        return INSTANCE;
    }

    /**
     * Retrieve key values from resource.
     * 
     * @param resBundle resource bundle.
     * @param resourceKey resource key.
     * @param defaultValue default value to use if resource key do not exist.
     */
    private static boolean getResourceValue(final ResourceBundle resBundle,
            final String resourceKey, final boolean defaultValue)
    {
        boolean retval; //NOPMD: false default, conditionally redefine.
        try {
            String resValue;
            if (resBundle.getString(resourceKey) == null) {
                resValue = "false";
            } else {
                resValue = resBundle.getString(resourceKey).trim();
            }

            retval = Arrays.asList(new String[] { //NOPMD: FP.
                        "yes",
                        "true" }).contains(
                resValue.toLowerCase(Locale.getDefault()));
        } catch (final MissingResourceException mre) { //NOPMD Reviewed.
            retval = defaultValue;
        }
        return retval;
    }

    /**
     * Factory method.
     * 
     * @return Singleton instance.
     * @param clazz class.
     * @param <T> class type.
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


    public void setShortPackage(final boolean shortPackage)
    {
        this.shortPackage = shortPackage;
    }

    /**
     * Add to list of prefix that will not print in the console.
     * 
     * @deprecated Use {@link #setLevel(String, int)}.
     * @param string prefix of objects to be excluded.
     */
    @Deprecated
    public void addToIgnore(final String string)
    {
        if (string != null) {
            this.ignoreSet.add(string);
        }
    }

    /**
     * @param source source class.
     * @param level log level.
     * 
     * @param <T> source class type.
     */
    public <T> void setLevel(final Class<T> source, final int level)
    {
        if (source != null && level <= Level.ERROR) {
            setLevel(source.getName(), level);
        }
    }


    /**
     * @param source source class name.
     * @param level log level.
     */
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
     * @param pageContext the current OA page context.
     * @param message
     */
    public void log(final OAPageContext pageContext, final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];
        log(pageContext, message, ste, this.defaultLevel);
    }

    private void log(final OAPageContext pageContext, final Object pMessage,
            final StackTraceElement ste, final int level)
    {
        final String message = pMessage instanceof RowSet ? getVOValues((RowSet) pMessage)
                : pMessage.toString();
        final String className = getClassNameDisp(ste);
        final String methName = getMethodDisp(ste);
        final int lineNo = ste.getLineNumber();

        if (pageContext.isDiagnosticMode()) {

            final String rcs = getClassRevision(ste);
            final String logStr = rcs + methName + "(" + lineNo + "): "
                    + message;
            pageContext.writeDiagnostics(
                className,
                logStr,
                OAFwkConstants.STATEMENT);

            final String htmlLog = new SimpleDateFormat(
                "HH:mm:ss",
                Locale.getDefault()).format(new Date())
                    + " "
                    + padSpace(LOG_PREFIX[level - 1])
                    + " "
                    + className
                    + logStr;
            if (isProcessRequest()) {
                writeHtmlComment(pageContext, htmlLog);
            } else {
                addDeferredLog(
                    (OAApplicationModuleImpl) pageContext.getRootApplicationModule(),
                    htmlLog);
            }

        }
        if (pageContext.isDeveloperMode() && isPrintToConsole()
                && isPrinted(getClassName(ste), level)) {

            print(
                className + methName + ":" + lineNo + SEP_MSG + message,
                level);
        }
    }


    private void addDeferredLog(final OAApplicationModuleImpl appModule,
            final String htmlLog)
    {
        if (appModule.getTransientValue(DEFERRED_LOGS) == null) {
            appModule.putTransientValue(DEFERRED_LOGS, new ArrayList<String>());
        }
        @SuppressWarnings("unchecked")
        final List<String> logList = (List<String>) appModule
            .getTransientValue(DEFERRED_LOGS);
        logList.add(htmlLog);
    }

    /** Detects from call stack if NOT processFormRequest is called. */
    boolean isProcessRequest()
    {
        boolean retval = true; //NOPMD: true default, conditionally redefine.
        for (final StackTraceElement nextSte : Thread
            .currentThread()
            .getStackTrace()) {
            if (nextSte.getMethodName().equals("processFormRequest")) {
                retval = false;
                break;
            }
        }
        return retval;
    }

    /**
     * @param pageContext the current OA page context.
     * @param logStr actual string to log.
     */
    private void writeHtmlComment(final OAPageContext pageContext,
            final String logStr)
    {

        OARawTextBean rawTextBean;
        if (pageContext.getPageLayoutBean().findChildRecursive(RESOURCE_NAME) == null) {
            rawTextBean = (OARawTextBean) pageContext
                .getWebBeanFactory()
                .createWebBean(
                    pageContext,
                    OAWebBeanConstants.RAW_TEXT_BEAN,
                    "VARCHAR2",
                    RESOURCE_NAME);
            pageContext.getPageLayoutBean().addIndexedChild(rawTextBean);
        } else {
            rawTextBean = (OARawTextBean) pageContext
                .getPageLayoutBean()
                .findChildRecursive(RESOURCE_NAME);
        }

        final StringBuilder strBuilder = new StringBuilder();

        if (rawTextBean.getValue(pageContext) != null) {
            final String existingValue = rawTextBean
                .getValue(pageContext)
                .toString();
            final int newLine = existingValue.indexOf('\n');
            final int endIdx = existingValue.lastIndexOf('\n') + 1;
            strBuilder.append(existingValue.substring(newLine, endIdx));
        }
        strBuilder.append(logStr);
        strBuilder.append('\n');

        final OAApplicationModuleImpl appModule = (OAApplicationModuleImpl) pageContext
            .getRootApplicationModule();
        if (appModule.getTransientValue(DEFERRED_LOGS) != null) {
            @SuppressWarnings("unchecked")
            final List<String> logList = (List<String>) appModule
                .getTransientValue(DEFERRED_LOGS);
            appModule.removeTransientValue(DEFERRED_LOGS);

            for (final String string : logList) {
                strBuilder.append(string);
                strBuilder.append('\n');
            }
        }

        rawTextBean.setValue(pageContext, "<!--\n"
                + strBuilder.toString().trim() + "\n//-->");
    }

    /**
     * For controller code or object with access to page context.
     * 
     * @param pageContext the current OA page context.
     * @param message
     */
    public void log(final OAPageContext pageContext, final Object message,
            final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(pageContext, dispMessage, ste, this.defaultLevel);
    }


    /**
     * For controller code or object with access to page context.
     * 
     * @param pageContext the current OA page context.
     * @param message
     */
    public void log(final OAPageContext pageContext, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(null, exception);
        log(pageContext, dispMessage, ste, this.defaultLevel);
    }


    public void info(final OAPageContext pageContext, final Object message,
            final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(pageContext, dispMessage, ste, Level.INFO);
    }

    public void debug(final OAPageContext pageContext, final Object message,
            final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(pageContext, dispMessage, ste, Level.DEBUG);
    }

    public void warn(final OAPageContext pageContext, final Object message,
            final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(pageContext, dispMessage, ste, Level.WARN);
    }

    public void error(final OAPageContext pageContext, final Object message,
            final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(pageContext, dispMessage, ste, Level.ERROR);
    }

    public void error(final OAPageContext pageContext, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(null, exception);
        log(pageContext, dispMessage, ste, Level.ERROR);
    }


    /* 2. Logger method: OAApplicationModule */

    private void log(final OAApplicationModuleImpl appModule,
            final Object pMessage, final StackTraceElement ste, final int level)
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

                final String logStr = methName + "(" + lineNo + "): " + message;

                final String htmlLog = new SimpleDateFormat(
                    "HH:mm:ss",
                    Locale.getDefault()).format(new Date())
                        + " "
                        + padSpace(LOG_PREFIX[level - 1])
                        + " "
                        + classNameDisp + getClassRevision(ste) + logStr;

                addDeferredLog(appModule, htmlLog);

                appModule.writeDiagnostics(
                    classNameDisp,
                    logStr,
                    OAFwkConstants.STATEMENT);
            }

            if (appModule.getOADBTransaction().isDeveloperMode()
                    && isPrintToConsole()
                    && isPrinted(getClassName(ste), level)) {
                print(classNameDisp + methName + ":" + lineNo + SEP_MSG
                        + message, level);
            }
        }
    }

    /**
     * @param appModule current application module instance.
     * @param message
     */
    public void log(final OAApplicationModuleImpl appModule,
            final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (appModule == null) {
            log(message, ste, this.defaultLevel);
        } else {
            log(appModule, message, ste, this.defaultLevel);
        }
    }

    public void info(final OAApplicationModuleImpl appModule,
            final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (appModule == null) {
            log(message, ste, Level.INFO);
        } else {
            log(appModule, message, ste, Level.INFO);
        }
    }

    public void debug(final OAApplicationModuleImpl appModule,
            final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (appModule == null) {
            log(message, ste, Level.DEBUG);
        } else {
            log(appModule, message, ste, Level.DEBUG);
        }
    }

    public void warn(final OAApplicationModuleImpl appModule,
            final Object message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (appModule == null) {
            log(message, ste, Level.WARN);
        } else {
            log(appModule, message, ste, Level.WARN);
        }
    }

    public void error(final OAApplicationModuleImpl appModule,
            final Object message)
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
    public void log(final OAApplicationModuleImpl appModule,
            final Object message, final Throwable exception)
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

    public void info(final OAApplicationModuleImpl appModule,
            final Object message, final Throwable exception)
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

    public void debug(final OAApplicationModuleImpl appModule,
            final Object message, final Throwable exception)
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

    public void warn(final OAApplicationModuleImpl appModule,
            final Object message, final Throwable exception)
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

    public void error(final OAApplicationModuleImpl appModule,
            final Object message, final Throwable exception)
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

    public void warn(final OADBTransaction trx, final String message)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        if (trx == null) {
            log(message, ste, Level.WARN);
        } else {
            log(trx, message, ste, Level.WARN);
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

    public void debug(final OADBTransaction trx, final Object message,
            final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(trx, dispMessage, ste, Level.DEBUG);
    }

    public void error(final OADBTransaction trx, final Object message,
            final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(message, exception);
        log(trx, dispMessage, ste, Level.ERROR);
    }

    public void error(final DBTransaction trx, final Throwable exception)
    {
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                : IDX_LOCAL_CALL];

        final String dispMessage = getDispMessage(null, exception);
        if (trx instanceof OADBTransaction) {
            log((OADBTransaction) trx, dispMessage, ste, Level.ERROR);
        } else {
            log(dispMessage, ste, Level.ERROR);
        }
    }


    private void log(final OADBTransaction trx, final Object pMessage,
            final StackTraceElement ste, final int level)
    {
        Object message;
        if (pMessage instanceof ViewObject) {
            message = getVOValues((ViewObject) pMessage);
        } else {
            message = pMessage;
        }
        if (trx == null) {
            log(message, ste, level);
        } else {

            if (trx.isDiagnosticMode()) {

                final String classNameDisp = getClassNameDisp(ste);
                final String methName = getMethodDisp(ste);
                final int lineNo = ste.getLineNumber();

                final String logStr = methName + "(" + lineNo + ")" + message;

                final String htmlLog = new SimpleDateFormat(
                    "HH:mm:ss",
                    Locale.getDefault()).format(new Date())
                        + " "
                        + padSpace(LOG_PREFIX[level - 1])
                        + " "
                        + classNameDisp + logStr;
                addDeferredLog(
                    (OAApplicationModuleImpl) trx.getRootApplicationModule(),
                    htmlLog);

                trx.writeDiagnostics(
                    getClassName(ste),
                    logStr,
                    OAFwkConstants.STATEMENT);
            }

            if (trx.isDeveloperMode() && isPrintToConsole()
                    && isPrinted(getClassName(ste), level)) {

                final String classNameDisp = getClassNameDisp(ste);
                final String methName = getMethodDisp(ste);
                final int lineNo = ste.getLineNumber();

                print(classNameDisp + methName + ":" + lineNo + SEP_MSG
                        + message, level);
            }
        }
    }


    /* 4. Logger method: ViewObject ==========================================*/

    private void log(final ViewObject viewObject, final StackTraceElement ste,
            final int level)
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
                final OAApplicationModuleImpl appModule = (OAApplicationModuleImpl) viewObject
                    .getApplicationModule();
                devMode = appModule.getOADBTransaction().isDeveloperMode();
                if (appModule.getOADBTransaction().isDiagnosticMode()) {
                    appModule.writeDiagnostics(classNameDisp, methName + "("
                            + lineNo + ")" + message, OAFwkConstants.STATEMENT);
                }
            }

            if (devMode && isPrintToConsole()
                    && isPrinted(getClassName(ste), level)) {
                print(classNameDisp + methName + ":" + lineNo + SEP_MSG
                        + message, level);
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

    private void log(final Object pMessage, final StackTraceElement ste,
            final int level)
    {
        Object message;
        if (pMessage instanceof ViewObject) {
            message = getVOValues((ViewObject) pMessage);
        } else if (pMessage instanceof RowIterator) {
            message = getVOValues((RowIterator) pMessage);
        } else {
            message = pMessage;
        }

        if (isPrintToConsole() && isPrinted(getClassName(ste), level)) {

            final String classNameDisp = getClassNameDisp(ste);
            final String methName = getMethodDisp(ste);
            final int lineNo = ste.getLineNumber();

            print(
                classNameDisp + methName + ":" + lineNo + SEP_MSG + message,
                level);
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
     * Note changes are for testing purpose on eclipse only in case I
     * accidentally commit.
     * 
     * @param message debug message.
     */
    public void debug(final Object message)
    {
        final int lastIdx = Thread.currentThread().getStackTrace().length - 1;
        StackTraceElement ste;
        if (Thread.currentThread().getStackTrace()[lastIdx]
            .getClassName()
            .contains("org.eclipse.jdt")) {//eclipse local.
            ste = Thread.currentThread().getStackTrace()[IDX_DEPLOYED_CALL];
        } else if (IDX_LOCAL_CALL > lastIdx) {
            ste = Thread.currentThread().getStackTrace()[lastIdx];
        } else {
            ste = Thread.currentThread().getStackTrace()[this.deployed ? IDX_DEPLOYED_CALL
                    : IDX_LOCAL_CALL];
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

    private void log(final Throwable message, final StackTraceElement ste,
            final int level)
    {
        if (isPrintToConsole() && isPrinted(getClassName(ste), level)) {

            final String classNameDisp = getClassNameDisp(ste);
            final String methName = getMethodDisp(ste);
            final int lineNo = ste.getLineNumber();

            print(classNameDisp + methName + ":" + lineNo + SEP_MSG + "\n"
                    + stackTraceToString(message), level);
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
        String retval = null; //NOPMD: null default, conditionally redefine.
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

    private boolean isUnIgnoredPrinted(final boolean pCurrVal,
            final String className, final int level)
    {
        boolean retval = pCurrVal; //NOPMD: init default, conditionally redefine.
        boolean isIdentified = false; //NOPMD: false default, conditionally redefine.
        for (final String nextClsLvl : this.classLevel.keySet()) {
            if (className.startsWith(nextClsLvl)) {
                retval = level >= this.classLevel.get(nextClsLvl)
                        && this.classLevel.get(nextClsLvl) != Level.OFF;
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
        boolean retval = false; //NOPMD: false default, conditionally redefine.
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
        return message == null ? "null\n" : message.toString() + '\n'
                + stackTraceToString(exception);
    }

    String getMethodDisp(final StackTraceElement ste)
    {
        return isShowMethod() ? '.' + ste.getMethodName() : "";
    }

    String getClassNameDisp(final StackTraceElement ste)
    {
        final String className = "".equals(activeClass) ? ste.getClassName()
                : activeClass;
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
            System.err.println(padSpace(LOG_PREFIX[level - 1]) + " " + message);
        } else {
            System.setOut(ORIG_STREAM);
            System.out.println(padSpace(LOG_PREFIX[level - 1]) + " " + message);

            if (!ENABLE_SYSOUT) {
                System.setOut(EMPTY_STREAM);
            }
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

    public void setPrintToConsole(final boolean pPrintToConsole)
    {
        this.printToConsole = pPrintToConsole;
    }

    public boolean isPrintToConsole()
    {
        return this.printToConsole;
    }

    public void setShowPackage(final boolean pShowPackage)
    {
        this.showPackage = pShowPackage;
    }

    private boolean isShowPackage()
    {
        return this.showPackage;
    }

    public void setShowMethod(final boolean pShowMethod)
    {
        this.showMethod = pShowMethod;
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

    public void setDefaultLevel(final int pDefaultLevel)
    {
        this.defaultLevel = pDefaultLevel;
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
     * @param rowSet row set to traverse. Not null.
     * @param doExecute will execute row set if true.
     * @param strBuilder output String Builder.
     */
    StringBuilder traverseRowSet(final RowIterator rowSet,
            final boolean doExecute, final StringBuilder strBuilder)
    {
        assert rowSet != null;

        RowIterator allRow;
        if (rowSet instanceof ViewObject) {
            allRow = ((ViewObject) rowSet).createRowSetIterator("oaflogIter");
            allRow.setRangeSize(-1);
        } else {
            allRow = rowSet;
        }

        strBuilder.append(" Count: ");
        strBuilder.append(allRow.getAllRowsInRange().length + '\n');
        int count = 1;
        for (final Row nextRow : allRow.getAllRowsInRange()) {
            strBuilder.append("Row " + count + ": ");
            for (final String nextAttr : nextRow.getAttributeNames()) {
                final Object attrValue = nextRow.getAttribute(nextAttr);
                if (attrValue instanceof RowSet) {
                    final RowSet childRowSet = (RowSet) attrValue;
                    strBuilder.append("\nSub VO: ");
                    strBuilder.append(childRowSet.getName());
                    strBuilder.append(getVOValues(childRowSet, doExecute));
                } else {
                    strBuilder.append(nextAttr + "=" + attrValue + ",");
                }
            }
            count++;
            strBuilder.append('\n');
        }
        if (allRow instanceof RowSetIterator) {
            final RowSetIterator rsIter = (RowSetIterator) allRow;
            rsIter.closeRowSetIterator();
        }
        return strBuilder;
    }

    /**
     * Get class revision by static field RCS_ID.
     * 
     * @param errObj current offending class.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    String getClassRevision(final StackTraceElement errObj)
    {
        String rcs = ""; //NOPMD: empty default, conditionally redefine.
        Class<?> klass;
        try {
            klass = Class.forName(errObj.getClassName());
            final String rcsRaw = (String) klass
                .getDeclaredField("RCS_ID")
                .get(null);
            if (rcsRaw.length() < MAX_RCSLEN) {
                rcs = "(r"
                        + rcsRaw.substring(
                            rcsRaw.indexOf(' ') + 1,
                            rcsRaw.lastIndexOf(' ')) + ")";
            }
        } catch (final Exception ignore) {
            INSTANCE.ignore(ignore);
        }
        return rcs;
    }

    /** @param rowSet */
    public String getVOValues(final RowSet rowSet)
    {
        return getVOValues(rowSet, false);
    }

    static ObjectUtil getUtil()
    {
        return OBJ_UTIL;
    }

}
