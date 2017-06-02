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


import com.epam.jdi.uitests.core.interfaces.base.IClickable;
import com.epam.jdi.uitests.core.interfaces.base.IHasValue;
import com.epam.jdi.uitests.core.interfaces.common.IText;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import io.qameta.allure.Step;

/**
 * Button control implementation
 *
 * @author Alexeenko Yan
 */
public class ClickableText extends Clickable implements IHasValue, IClickable, IText {
    public ClickableText() {
    }

    public ClickableText(By byLocator) {
        super(byLocator);
    }

    public ClickableText(WebElement webElement) {
        super(webElement);
    }

    protected String getTextAction() {
        String getText = getWebElement().getText();
        if (!getText.equals(""))
            return getText;
        String getValue = getWebElement().getAttribute("value");
        return getValue != null
                ? getValue
                : getText;
    }

    /**
     * @return Get value of Element
     */
    public final String getValue() {
        return actions.getValue(this::getTextAction);
    }

    /**
     * @return Get Element’s text
     */
    @Step
    public final String getText() {
        return actions.getText(this::getTextAction);
    }

    /**
     * @param text Specify expected text
     * @return Wait while Element’s text contains expected text. Returns Element’s text
     */
    @Step
    public final String waitText(String text) {
        return actions.waitText(text, this::getTextAction);
    }

    /**
     * @param regEx Specify expected regular expression Text
     * @return Wait while Element’s text matches regEx. Returns Element’s text
     */
    @Step
    public final String waitMatchText(String regEx) {
        return actions.waitMatchText(regEx, this::getTextAction);
    }
}