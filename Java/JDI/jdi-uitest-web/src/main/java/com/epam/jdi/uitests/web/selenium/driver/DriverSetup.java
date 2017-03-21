package com.epam.jdi.uitests.web.selenium.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

public interface DriverSetup {

    WebDriver getWebDriverObject(DesiredCapabilities desiredCapabilities);
    DesiredCapabilities getDesiredCapabilities(String downloadDir);
}