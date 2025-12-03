package de.ddb.labs.europack.source.ddbapi;

import java.util.Locale;

public final class UserAgent {
    private UserAgent() {
    }

    public static String build(String appName, String homepage, String contactEmail) {
        final String appVersion = getAppVersion(appName);

        final String os = System.getProperty("os.name", "unknown");
        final String osVer = System.getProperty("os.version", "unknown");
        final String arch = System.getProperty("os.arch", "unknown");

        final String javaVer = System.getProperty("java.version", "unknown");
        final String javaVendor = System.getProperty("java.vendor", "unknown");

        final String locale = Locale.getDefault().toLanguageTag();

        return "%s/%s (%s %s; %s; Java/%s; %s; %s) (+%s; mailto:%s)".formatted(
                appName,
                appVersion,
                os, osVer,
                arch,
                javaVer,
                javaVendor,
                locale,
                homepage,
                contactEmail);
    }

    private static String getAppVersion(String appName) {
        final Package p = UserAgent.class.getPackage();
        final String v = (p != null) ? p.getImplementationVersion() : null;
        if (v != null && !v.isBlank())
            return v;
        return "unknown";
    }
}
