package com.epam.jdi.uitests.web.selenium.driver;
/*
 * Copyright 2004-2016 EPAM Systems
 *
 * This file is part of JDI project.
 *
 * JDI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JDI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JDI. If not, see <http://www.gnu.org/licenses/>.
 */


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman_Iovlev on 7/31/2015.
 */
public enum DriverTypes implements DriverSetup  {
    FIREFOX("firefox") {
        public DesiredCapabilities getDesiredCapabilities(String downloadsDir) {
            DesiredCapabilities capabilities = DesiredCapabilities.firefox();
            capabilities.setBrowserName("firefox");
            capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
            downloadFileDir = downloadsDir;
            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            return new FirefoxDriver(new FirefoxBinary(), getFirefoxProfile(downloadFileDir), capabilities);
        }

        public FirefoxProfile getFirefoxProfile(String downloadsDir) {
            FirefoxProfile firefoxProfile = new FirefoxProfile();
            firefoxProfile.setAssumeUntrustedCertificateIssuer(false);
            firefoxProfile.setEnableNativeEvents(false);

            firefoxProfile.setPreference("browser.download.folderList", 2);
            firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
            firefoxProfile.setPreference("browser.helperApps.alwaysAsk.force", false);
            firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/xls;text/csv;text/plain");
            firefoxProfile.setPreference("browser.download.dir", downloadsDir);

            firefoxProfile.setPreference("print.always_print_silent", "true");
            firefoxProfile.setPreference("print.show_print_progress", "false");
            firefoxProfile.setPreference("browser.startup.homepage", "about:blank");
            firefoxProfile.setPreference("startup.homepage_welcome_url", "about:blank");
            firefoxProfile.setPreference("startup.homepage_welcome_url.additional", "about:blank");
            firefoxProfile.setPreference("network.http.phishy-userpass-length", 255);
            return firefoxProfile;
        }
    },
    CHROME("chrome") {
        public DesiredCapabilities getDesiredCapabilities(String downloadFilepath) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-web-security");
            options.addArguments("--disable-extensions");
            options.addArguments("test-type");
            HashMap<String, Object> chromePrefs = new HashMap<>();
            chromePrefs.put("profile.default_content_setting_values.notifications", 0);
            chromePrefs.put("profile.password_manager_enabled", false);
            chromePrefs.put("credentials_enable_service", false);
            chromePrefs.put("profile.default_content_settings.popups", 0);
            chromePrefs.put("download.default_directory", downloadFilepath);
            options.setExperimentalOption("prefs", chromePrefs);
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            capabilities.setBrowserName("chrome");
            capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            capabilities.setCapability(ChromeOptions.CAPABILITY, options);
            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            return new ChromeDriver(capabilities);
        }
    },
    ANDROID("android") {
        public DesiredCapabilities getDesiredCapabilities(String downloadsDir ) {
            return getChromeMobileEmulatorCapabilities("Google Nexus 5");
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            return new ChromeDriver(capabilities);
        }
    },
    IPAD("ipad") {
        public DesiredCapabilities getDesiredCapabilities(String downloadsDir) {
            return getChromeMobileEmulatorCapabilities("Apple iPad");
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            return new ChromeDriver(capabilities);
        }
    },
    IPHONE("iphone") {
        public DesiredCapabilities getDesiredCapabilities(String downloadsDir) {
            return getChromeMobileEmulatorCapabilities("Apple iPhone 5");
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            return new ChromeDriver(capabilities);
        }
    },
    IE("ie") {
        public DesiredCapabilities getDesiredCapabilities(String downloadsDir) {
            DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
            capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
            capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
            capabilities.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, true);
            capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
            capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
            capabilities.setJavascriptEnabled(true);
            capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            return new InternetExplorerDriver(capabilities);
        }
    },
    SAFARI("safari") {
        public DesiredCapabilities getDesiredCapabilities(String downloadsDir) {
            DesiredCapabilities capabilities = DesiredCapabilities.safari();
            SafariOptions safariOptions = new SafariOptions();
            safariOptions.setUseCleanSession(true);
            capabilities.setCapability(SafariOptions.CAPABILITY, safariOptions);
            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            return new SafariDriver(capabilities);
        }
    };

    private static DesiredCapabilities getChromeMobileEmulatorCapabilities(String deviseName) {
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        Map<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", deviseName);
        Map<String, Object> chromeOptions = new HashMap<>();
        chromeOptions.put("mobileEmulation", mobileEmulation);
        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        capabilities.setBrowserName("chrome");
        return capabilities;
    }


    public String downloadFileDir;
    public String name;

    DriverTypes(String name) {
        this.name = name;
    }

}