package com.epam.jdi.uitests.web.selenium.elements.base;
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


import com.epam.commons.Timer;
import com.epam.jdi.uitests.core.interfaces.base.IElement;
import com.epam.jdi.uitests.core.settings.HighlightSettings;
import com.epam.jdi.uitests.core.settings.JDISettings;
import com.epam.jdi.uitests.web.selenium.elements.composite.WebSite;
import com.epam.jdi.uitests.web.settings.WebSettings;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

import static com.epam.commons.ReflectionUtils.newEntity;
import static com.epam.jdi.uitests.core.logger.LogLevels.DEBUG;
import static com.epam.jdi.uitests.core.settings.JDISettings.asserter;
import static java.lang.String.format;

/**
 * Base Element control implementation
 *
 * @author Alexeenko Yan
 * @author Belin Yury
 * @author Belousov Andrey
 * @author Shubin Konstantin
 * @author Zharov Alexandr
 */
public class Element extends BaseElement implements IElement, IHasElement {

    private String imgPath;

    public Element() {
        super();
    }

    public Element(By byLocator) {
        super(byLocator);
    }

    public Element(WebElement webElement) {
        avatar.setWebElement(webElement);
    }

    public static <T extends Element> T copy(T element, By newLocator) {
        try {
            T result = newEntity((Class<T>) element.getClass());
            result.setAvatar(newLocator, element.getAvatar());
            return result;
        } catch (Exception ex) {
            throw JDISettings.exception("Can't copy Element: " + element);
        }
    }

    public WebElement getWebElement() {
        return invoker.doJActionResult("Get web element",
            () -> avatar.getElement(), DEBUG);
    }

    public WebElement getHighLightElement() {
        return avatar.getElement();
    }

    public void setWebElement(WebElement webElement) {
        avatar.setWebElement(webElement);
    }

    public WebElement get(By locator) {
        Element el = new Element(locator);
        el.setParent(this);
        return el.getWebElement();
    }

    public List<WebElement> getList(By locator) {
        return getWebElement().findElements(locator);
    }

    /**
     * Get element attribute
     *
     * @param name Specify name for attribute
     * @return Returns chosen attribute
     */
    public String getAttribute(String name) {
        return getAttribute(getName(), name);
    }

    @Step("{elName} - Get attribute {name}")
    private String getAttribute(String elName, String name) {
        return getWebElement().getAttribute(name);
    }

    /**
     * @param name Specify attribute name
     * @param value Specify attribute value Waits while attribute gets expected value. Return false
     * if this not happens
     */
    public void waitAttribute(String name, String value) {
        waitAttribute(getName(), name, value);
    }

    @Step("{elName} Wait attribute {name} gets value {value}")
    private void waitAttribute(String elName, String name, String value) {
        wait(el -> el.getAttribute(name).equals(value));
    }

    /**
     * @param attributeName Specify attribute name
     * @param value Specify attribute value Sets attribute value for Element
     */
    public void setAttribute(String attributeName, String value) {
        setAttribute(getName(), attributeName, value);
    }

    @Step("{elName} Set attribute {attributeName}={value}")
    private void setAttribute(String elName, String attributeName, String value) {
        invoker.doJAction(format("Set Attribute '%s'='%s'", attributeName, value),
            () -> jsExecutor().executeScript(
                format("arguments[0].setAttribute('%s',arguments[1]);", attributeName),
                getWebElement(), value));
    }


    protected boolean isDisplayedAction() {
        return avatar.findImmediately(() -> getWebElement().isDisplayed() && getWebElement().getSize().width>0 && getWebElement().getSize().height>0, false);
    }

    /**
     * @return Check is Element visible
     */
    public boolean isDisplayed() {
        return isDisplayed(getName());
    }

    @Step("{elName} - Is displayed")
    private boolean isDisplayed(String elName) {
        return actions.isDisplayed(this::isDisplayedAction);
    }

    @Override
    public String getImgPath() {
        return imgPath;
    }

    @Override
    public void setImgPath(String imgPath) {
        try {
            this.imgPath = imgPath;
        }
        catch (Exception ex){
        }
    }

    private String generateImgPath(){
        if (imgPath == null) return null;
        return Paths.get(WebSite.getDefaultPath()).toAbsolutePath().toString().replace('\\','/') + imgPath;
//        return Element.class.getResource(WebSite.getDefaultPath() + imgPath).getPath();
    }

    protected void waitDisplayedAction() {
        wait(WebElement::isDisplayed);
    }

    /**
     * @return Check is Element hidden
     */
    public boolean isHidden() {
        return actions.isDisplayed(() -> !isDisplayedAction());
    }

    /**
     * Waits while Element becomes visible
     */
    public void waitDisplayed() {
        waitDisplayed(getName());
    }

    @Step("{elName} - Wait displayed")
    private void waitDisplayed(String elName) {
        actions.waitDisplayed(getWebElement()::isDisplayed);
    }

    /**
     * Waits while Element becomes invisible
     */
    public void waitVanished() {
        waitVanished(getName());
    }

    @Step("{elName} - Waits while becomes invisible")
    private void waitVanished(String elName) {
        actions.waitVanished(() -> timer().wait(() -> !isDisplayedAction()));
    }

    public WebElement getInvisibleElement() {
        avatar.searchAll();
        return getWebElement();
    }

    /**
     * @param resultFunc Specify expected function result Waits while condition with WebElement
     * happens during specified timeout and returns result using resultFunc
     */
    public void wait(Function<WebElement, Boolean> resultFunc) {
        wait(getName(), resultFunc);
    }

    @Step("{elName} - Wait {resultFunc}")
    private void wait(String elName, Function<WebElement, Boolean> resultFunc) {
        boolean result = wait(resultFunc, r -> r);
        asserter.isTrue(result);
    }

    /**
     * @param resultFunc Specify expected function result
     * @param condition Specify expected function condition
     * @return Waits while condition with WebElement happens and returns result using resultFunc
     */
    @Step
    public <R> R wait(Function<WebElement, R> resultFunc, Function<R, Boolean> condition) {
        return timer().getResultByCondition(() -> resultFunc.apply(getWebElement()), condition);
    }

    /**
     * @param resultFunc Specify expected function result
     * @param timeoutSec Specify timeout Waits while condition with WebElement happens during
     * specified timeout and returns wait result
     */
    @Step
    public void wait(Function<WebElement, Boolean> resultFunc, int timeoutSec) {
        boolean result = wait(resultFunc, r -> r, timeoutSec);
        asserter.isTrue(result);
    }

    /**
     * @param resultFunc Specify expected function result
     * @param timeoutSec Specify timeout
     * @param condition Specify expected function condition
     * @return Waits while condition with WebElement and returns wait result
     */
    @Step
    public <R> R wait(Function<WebElement, R> resultFunc, Function<R, Boolean> condition,
                      int timeoutSec) {
        setWaitTimeout(timeoutSec);
        R result = new Timer(timeoutSec * 1000)
            .getResultByCondition(() -> resultFunc.apply(getWebElement()), condition);
        restoreWaitTimeout();
        return result;
    }

    public void highlight() {
        WebSettings.driverFactory.highlight(this);
    }

    public void highlight(HighlightSettings highlightSettings) {
        WebSettings.driverFactory.highlight(this, highlightSettings);
    }

    public void clickWithKeys(Keys... keys) {
        invoker.doJAction("Ctrl click on Element",
            () -> {
                Actions action = new Actions(getDriver());
                for (Keys key : keys) {
                    action = action.keyDown(key);
                }
                action = action.moveToElement(getWebElement()).click();
                for (Keys key : keys) {
                    action = action.keyUp(key);
                }
                action.perform();
            });
    }

    public void doubleClicks() {
        doubleClicks(getName());
    }

    @Step("Double click on {elName}")
    private void doubleClicks(String elName) {
        invoker.doJAction("Double click on Element", () -> {
            Actions builder = new Actions(getDriver());
            builder.doubleClick(getWebElement()).perform();
        });
    }

    public void rightClick() {
        rightClick(getName());
    }

    @Step("Right click on {elName}")
    private void rightClick(String elName) {
        invoker.doJAction("Right click on Element", () -> {
            Actions builder = new Actions(getDriver());
            builder.contextClick(getWebElement()).perform();
        });
    }

    public void clickCenter() {
        clickCenter(getName());
    }

    @Step("Click in Center of {elName}")
    private void clickCenter(String elName) {
        invoker.doJAction("Click in Center of Element", () -> {
            Actions builder = new Actions(getDriver());
            builder.click(getWebElement()).perform();
        });
    }

    public void mouseOver() {
        mouseOver(getName());
    }

    @Step("Move mouse over {elName}")
    private void mouseOver(String elName) {
        invoker.doJAction("Move mouse over Element", () -> {
            Actions builder = new Actions(getDriver());
            builder.moveToElement(getWebElement()).build().perform();
        });
    }

    public void focus() {
        focus(getName());
    }


    @Step("Focus on {elName}")
    private void focus(String elName) {
        invoker.doJAction("Scroll Element into the center of View and Focus on it", () -> {
            ((JavascriptExecutor) getDriver())
                    .executeScript("arguments[0].scrollIntoView({block: 'center'});", getWebElement());
            Dimension size = getWebElement().getSize(); //for scroll to object
            new Actions(getDriver()).moveToElement(getWebElement(), size.width / 2, size.height / 2)
                .build().perform();
        });
    }

    public void selectArea(int x1, int y1, int x2, int y2) {
        selectArea(getName(), x1, y1, x2, y2);
    }

    @Step("{elName} - Select area: from x={x1},y={y1};to x={x2},y={y2}")
    private void selectArea(String elName, int x1, int y1, int x2, int y2) {
        invoker.doJAction(format("Select area: from %d,%d;to %d,%d", x1, y1, x2, y2), () -> {
            WebElement element = getWebElement();
            new Actions(getDriver()).moveToElement(element, x1, y1).clickAndHold()
                .moveToElement(element, x2, y2).release().build().perform();
        });
    }

    public void dragAndDropBy(int x, int y) {
        dragAndDropBy(getName(), x, y);
    }

    @Step("Drag and drop {elName}: (x,y)=({x},{y})")
    private void dragAndDropBy(String elName, int x, int y) {
        invoker.doJAction(format("Drag and drop Element: (x,y)=(%s,%s)", x, y), () ->
            new Actions(getDriver()).dragAndDropBy(getWebElement(), x, y).build().perform());
    }

    public void dragAndDrop(Element target) {
        dragAndDrop(getName(), target, target.getName());
    }

    @Step("{elName} - Drag and drop to {tName}")
    private void dragAndDrop(String elName, Element target, String tName) {
        invoker.doJAction(format("Drag and drop to Target Element: %s", target.toString()), () ->
            new Actions(getDriver()).dragAndDrop(getWebElement(), target.getWebElement()).build()
                .perform());
    }

    public void removeAttribute(String attributeName) {
        removeAttribute(getName(), attributeName);
    }

    @Step("{elName} Remove attribute {attributeName}")
    private void removeAttribute(String elName, String attributeName) {
        invoker.doJAction(format("Remove Attribute '%s'", attributeName),
                () -> jsExecutor().executeScript("arguments[0].removeAttribute(arguments[1]);", getWebElement(), attributeName));
    }

    public void waitContainsAttribute(String name, String value) {
        waitContainsAttribute(getName(), name, value);
    }

    @Step("{elName} Wait attribute {name} contains value {value}")
    private void waitContainsAttribute(String elName, String name, String value) {
        wait(el -> el.getAttribute(name).contains(value));
    }
}
