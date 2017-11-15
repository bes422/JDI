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


import com.epam.commons.linqinterfaces.JFuncTREx;
import com.epam.commons.map.MapArray;
import com.epam.commons.pairs.Pair;
import com.epam.jdi.uitests.core.interfaces.base.IElement;
import com.epam.jdi.uitests.core.interfaces.settings.IDriver;
import com.epam.jdi.uitests.core.settings.HighlightSettings;
import com.epam.jdi.uitests.web.selenium.elements.base.BaseElement;
import com.epam.jdi.uitests.web.selenium.elements.base.Element;
import com.epam.jdi.uitests.web.settings.WebSettings;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.epam.commons.LinqUtils.any;
import static com.epam.commons.ReflectionUtils.isClass;
import static com.epam.commons.StringUtils.LINE_BREAK;
import static com.epam.commons.Timer.sleep;
import static com.epam.jdi.uitests.core.settings.JDISettings.exception;
import static com.epam.jdi.uitests.core.settings.JDISettings.timeouts;
import static com.epam.jdi.uitests.web.selenium.driver.DriverTypes.*;
import static com.epam.jdi.uitests.web.selenium.driver.RunTypes.LOCAL;
import static com.epam.jdi.uitests.web.settings.WebSettings.*;
import static java.lang.String.format;
import static java.lang.System.setProperty;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openqa.selenium.ie.InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS;
import static org.openqa.selenium.remote.CapabilityType.PAGE_LOAD_STRATEGY;

/**
 * Created by Roman_Iovlev on 6/10/2015.
 */
public class SeleniumDriverFactory implements IDriver<WebDriver> {
    public static JFuncTREx<WebElement, Boolean> elementSearchCriteria = WebElement::isDisplayed;
    public static boolean onlyOneElementAllowedInSearch = true;
    public RunTypes runType = LOCAL;
    static final String FOLDER_PATH = new File("").getAbsolutePath() + File.separator+"src"+File.separator+"main"+File.separator+"resources"+File.separator+"driver"+File.separator;
    public Boolean getLatestDriver = false;
    public static String currentDriverName = "CHROME";
    public boolean isDemoMode = false;
    public String pageLoadStrategy = "normal";
    public HighlightSettings highlightSettings = new HighlightSettings();
    private String driversPath = FOLDER_PATH;
    private MapArray<String, Supplier<WebDriver>> drivers = new MapArray<>();
    private ThreadLocal<MapArray<String, WebDriver>> runDrivers = new ThreadLocal<>();

    public SeleniumDriverFactory() {
        this(false, new HighlightSettings(), WebElement::isDisplayed);
    }

    public SeleniumDriverFactory(boolean isDemoMode) {
        this(isDemoMode, new HighlightSettings(), WebElement::isDisplayed);
    }

    public SeleniumDriverFactory(HighlightSettings highlightSettings) {
        this(false, highlightSettings, WebElement::isDisplayed);
    }

    public SeleniumDriverFactory(JFuncTREx<WebElement, Boolean> elementSearchCriteria) {
        this(false, new HighlightSettings(), elementSearchCriteria);
    }

    public SeleniumDriverFactory(boolean isDemoMode, HighlightSettings highlightSettings,
                                 JFuncTREx<WebElement, Boolean> elementSearchCriteria) {
        this.isDemoMode = isDemoMode;
        this.highlightSettings = highlightSettings;
        SeleniumDriverFactory.elementSearchCriteria = elementSearchCriteria;
    }

    public String getDriverPath() {
        return driversPath;
    }

    public void setDriverPath(String driverPath) {
        this.driversPath = driverPath;
    }

    static final String getChromeDriverPath(String folderPath) {
        return checkOS().equals("win") ? folderPath + "\\chromedriver.exe" : folderPath + "\\chromedriver";
    }

    static final String getIEDriverPath(String folderPath) {
        return folderPath + "\\IEDriverServer.exe";
    }

    static final String getFirefoxDriverPath(String folderPath) {
        return checkOS().equals("win") ? folderPath + "\\geckodriver.exe" : folderPath + "\\geckodriver";
    }

    static String checkOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            return "mac";
        } else if (osName.contains("win") || osName.contains("ms")) {
            return "win";
        } else {
            return "nix";
        }
    }

    public String currentDriverName() {
        return currentDriverName;
    }

    public void setCurrentDriver(String driverName) {
        currentDriverName = driverName;
    }

    public boolean hasDrivers() {
        return drivers.any();
    }

    public boolean hasRunDrivers() {
        return runDrivers.get() != null && runDrivers.get().any();
    }

    // REGISTER DRIVER

    public String registerDriver(Supplier<WebDriver> driver) {
        return registerDriver("Driver" + (drivers.size() + 1), driver);
    }

    public void setRunType(String runType) {
        switch (runType.toLowerCase()) {
            case "local":
                this.runType = LOCAL;
                break;
            case "remote":
                this.runType = RunTypes.REMOTE;
                break;
            case "grid":
                this.runType = RunTypes.GRID;
                break;
        }
    }

    public String registerDriver(String driverName) {
        switch (driverName.toLowerCase()) {
            case "chrome":
                return registerDriver(CHROME);
            case "firefox":
                return registerDriver(FIREFOX);
            case "ie":
            case "internetexplorer":
                return registerDriver(IE);
            case "iphone":
                return registerDriver(IPHONE);
            case "safari":
                return registerDriver(SAFARI);
            case "ipad":
                return registerDriver(IPAD);
            case "android":
                return registerDriver(ANDROID);
            default:
                throw exception("Unknown driver: " + driverName);
        }
    }

    public String registerDriver(DriverTypes driverType) {
        switch (runType) {
            case LOCAL:
                return registerLocalDriver(driverType);
            case GRID:
                return registerDriver("Grid " + driverType,
                        () -> {
                            RemoteWebDriver remoteWebDriver = new RemoteWebDriver(hub, driverType.getDesiredCapabilities(downloadsDir));
                            remoteWebDriver.manage().window().maximize();
                            return remoteWebDriver;
                        });
            case REMOTE:
                return registerDriver("Remote " + driverType,
                        () -> new RemoteWebDriver(SauceLabRunner.getSauceUrl(), SauceLabRunner.getSauceDesiredCapabilities(driverType)));
        }
        throw exception("Unknown driver: " + driverType);
    }

    // GET DRIVER

    protected String registerLocalDriver(DriverTypes driverType) {
        switch (driverType) {
            case CHROME:
            case IPAD:
            case IPHONE:
            case ANDROID:
                return registerDriver(driverType,
                        () -> {
                            System.setProperty("wdm.targetPath", downloadsDir);
                            System.setProperty("wdm.architecture", "");
                            ChromeOptions chromeOptions = defaultChromeOptions();
                            chromeOptions.merge(driverType.getDesiredCapabilities(downloadsDir));

                            if (getLatestDriver) {
                                ChromeDriverManager.getInstance().setup();
                                ChromeDriver t = new ChromeDriver(chromeOptions);
                                t.manage().window().maximize();
                                return webDriverSettings.apply(t);
                            } else {
                                setProperty("webdriver.chrome.driver", getChromeDriverPath(driversPath));
                                ChromeDriver t = new ChromeDriver(chromeOptions);
                                t.manage().window().maximize();
                                return webDriverSettings.apply(t);
                            }
                        });

            case FIREFOX:
                return registerDriver(driverType,
                        () -> {
                            System.setProperty("wdm.targetPath", downloadsDir);
                            System.setProperty("wdm.architecture", "");
                            if (getLatestDriver) {
                                return webDriverSettings.apply(initFirefox());
                            } else {
                                setProperty("webdriver.gecko.driver", getFirefoxDriverPath(driversPath));
                                return webDriverSettings.apply(new FirefoxDriver(defaultFirefoxOptions()));
                            }
                        });
            case IE:
                return registerDriver(driverType, () -> {
                    DesiredCapabilities capabilities = driverType.getDesiredCapabilities(downloadsDir);
                    capabilities.setCapability(INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                    InternetExplorerOptions internetExplorerOptions = defaultIEOptions();
                    internetExplorerOptions.merge(capabilities);
                    if (getLatestDriver) {
                        InternetExplorerDriverManager.getInstance().setup();

                        return webDriverSettings.apply(new InternetExplorerDriver(internetExplorerOptions));
                    } else {
                        setProperty("webdriver.ie.driver", getIEDriverPath(driversPath));
                        return webDriverSettings.apply(new InternetExplorerDriver(internetExplorerOptions));
                    }
                });
        }
        throw exception("Unknown driver: " + driverType);
    }
    private FirefoxOptions defaultFirefoxOptions() {
        FirefoxOptions cap = new FirefoxOptions();
        cap.setCapability(PAGE_LOAD_STRATEGY, pageLoadStrategy);
        return cap;
    }
    private ChromeOptions defaultChromeOptions() {
        ChromeOptions cap = new ChromeOptions();
        cap.setCapability(PAGE_LOAD_STRATEGY, pageLoadStrategy);
        return cap;
    }
    private InternetExplorerOptions defaultIEOptions() {
        InternetExplorerOptions cap = new InternetExplorerOptions();
        cap.setCapability(INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
        cap.setCapability("ignoreZoomSetting", true);
        //cap.setCapability("requireWindowFocus", true);
        cap.setCapability(PAGE_LOAD_STRATEGY, pageLoadStrategy);
        return cap;
    }
    public String registerDriver(DriverTypes driverType, Supplier<WebDriver> driver) {
        int numerator = 2;
        String driverName = driverType.toString();
        // TODO correct constant 100
        while (!drivers.add(driverName, driver) && numerator < 100)
            driverName = driverType.toString() + numerator++;
        currentDriverName = driverName;
        return driverName;
    }

    public String registerDriver(String driverName, Supplier<WebDriver> driver) {
        if (!drivers.add(driverName, driver))
            throw exception("Can't register WebDriver '%s'. Driver with same name already registered", driverName);
        currentDriverName = driverName;
        return driverName;
    }

    public WebDriver getDriver() {
        try {
            if (!currentDriverName.equals(""))
                return getDriver(currentDriverName);
            registerDriver(CHROME);
            return getDriver(CHROME.toString());
        } catch (Exception ex) {
            throw WebSettings.asserter.exception("Can't get WebDriver. " + LINE_BREAK + ex.getMessage());
        }
    }

    private WebDriver initFirefox() {
        FirefoxDriverManager.getInstance().arch32().setup();
        return new FirefoxDriver(defaultFirefoxOptions());
    }

    private WebDriver initChrome() {
        ChromeDriverManager.getInstance().setup();
        return new ChromeDriver(defaultChromeOptions());
    }

    private WebDriver initIE() {
        InternetExplorerDriverManager.getInstance().setup();
        return new InternetExplorerDriver(defaultIEOptions());
    }

    public static Dimension browserSizes;

    private static void maximizeMacBrowser(WebDriver driver) {
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        driver.manage().window()
                .setSize(new org.openqa.selenium.Dimension(screenSize.width, screenSize.height));
    }

    public static Function<WebDriver, WebDriver> webDriverSettings = driver -> {
        if (browserSizes == null) {
            if (any(asList("chrome", "internetexplorer"),
                    el -> driver.toString().toLowerCase().contains(el))) {
                if (System.getProperty("os.name").toLowerCase().contains("mac"))
                    maximizeMacBrowser(driver);
                else
                    driver.manage().window().maximize();
            }
        }
        else
            driver.manage().window().setSize(browserSizes);
        driver.manage().timeouts().implicitlyWait(timeouts.getCurrentTimeoutSec(), SECONDS);
        return driver;
    };

    public WebDriver getDriver(String driverName) {
        if (!drivers.keys().contains(driverName))
            if (drivers.count() == 0)
                registerDriver(driverName);
            else throw exception("Can't find driver with name '%s'", driverName);
        try {
            Lock lock = new ReentrantLock();
            lock.lock();
            if (runDrivers.get() == null || !runDrivers.get().keys().contains(driverName)) {
                MapArray<String, WebDriver> rDrivers = runDrivers.get();
                if (rDrivers == null)
                    rDrivers = new MapArray<>();
                WebDriver resultDriver = drivers.get(driverName).get();
                if (resultDriver == null)
                    throw exception("Can't get WebDriver '%s'. This Driver name not registered", driverName);
                rDrivers.add(driverName, resultDriver);
                runDrivers.set(rDrivers);
            }
            WebDriver result = runDrivers.get().get(driverName);
            if (result.toString().contains("(null)")) {
                result = drivers.get(driverName).get();
                runDrivers.get().update(driverName, result);
            }
            lock.unlock();
            return result;
        } catch (Exception ex) {
            throw exception("Can't get driver; Thread: " + currentThread().getId() + LINE_BREAK +
                    format("Drivers: %s; Run: %s", drivers, runDrivers) +
                    "Exception: " + ex.getMessage());
        }
    }

    public void reopenDriver() {
        reopenDriver(currentDriverName);
    }

    public void reopenDriver(String driverName) {
        MapArray<String, WebDriver> rDriver = runDrivers.get();
        if (rDriver.keys().contains(driverName)) {
            rDriver.get(driverName).close();
            rDriver.removeByKey(driverName);
            runDrivers.set(rDriver);
        }
        if (drivers.keys().contains(driverName))
            getDriver();
    }

    public void switchToDriver(String driverName) {
        if (drivers.keys().contains(driverName))
            currentDriverName = driverName;
        else
            throw exception("Can't switch to Webdriver '%s'. This Driver name not registered", driverName);
    }

    public void processDemoMode(BaseElement element) {
        if (isDemoMode)
            if (isClass(element.getClass(), Element.class))
                highlight((Element) element, highlightSettings);
    }

    public void highlight(IElement element) {
        highlight(element, highlightSettings);
    }

    public void highlight(IElement element, HighlightSettings settings) {
        HighlightSettings highlightSettings = settings;
        if (highlightSettings == null)
            highlightSettings = new HighlightSettings();
        WebElement webElement = ((Element) element).getHighLightElement();
        String orig = webElement.getAttribute("style");
        getJSExecutor().executeScript(format("arguments[0].setAttribute('%s',arguments[1]);", "style"),
                webElement, format("border: 3px solid %s; background-color: %s;", highlightSettings.getFrameColor(),
                        highlightSettings.getBgColor()));
        sleep(highlightSettings.getTimeoutInSec() * 1000);
        getJSExecutor().executeScript(format("arguments[0].setAttribute('%s',arguments[1]);", "style"),
                webElement, orig);
    }

    public void runApplication() {

    }

    public void closeApplication() {
    }

    public void get(String s) {

    }

    public String getCurrentUrl() {
        return null;
    }

    public String getTitle() {
        return null;
    }

    public List<WebElement> findElements(By by) {
        return null;
    }

    public WebElement findElement(By by) {
        return null;
    }

    public String getPageSource() {
        return null;
    }

    public void close() {
        for (Pair<String, WebDriver> driver : runDrivers.get())
            driver.value.quit();
        runDrivers.get().clear();
    }

    public void quit() {
        close();
    }

    public Set<String> getWindowHandles() {
        return null;
    }

    public String getWindowHandle() {
        return null;
    }
}