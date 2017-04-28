package com.epam.jdi.uitests.web.selenium.elements.composite;
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


import com.epam.commons.map.MapArray;
import com.epam.jdi.uitests.core.interfaces.complex.IPopup;
import ru.yandex.qatools.allure.annotations.Step;

import static com.epam.jdi.uitests.core.annotations.functions.Functions.*;
import static java.lang.String.format;

/**
 * Created by Roman_Iovlev on 7/8/2015.
 */
public class PopupForm<T> extends Form<T> implements IPopup {
    /**
     * @param objStrings Fill all SetValue elements and click on Button specified button e.g. "Publish" or "Save" <br>
     * @apiNote To use this option Form pageObject should have button names in specific format <br>
     * e.g. if you call "submit(user, "Publish") then you should have Element 'publishButton'. <br>
     * * Letters case in button name  no matters
     */
    @Override
    public void submit(MapArray<String, String> objStrings) {
        submit(getName(), objStrings);
    }

    @Step("{0} - submit [{1}]")
    public void submit(String elName, MapArray<String, String> objStrings) {
        fill(objStrings);
        ok();
    }

    /**
     * Click on Button marked with annotation @OkButton or named "okButton"
     */
    public void ok() {
        okCustom(getName());
    }

    @Step("{0} - Press Ok on popup")
    private void okCustom(String elName) {
        getElementClass.getButton(OK_BUTTON).click();
    }

    /**
     * Click on Button marked with annotation @CancelButton or named "cancelButton"
     */
    public void cancel() {
        cancelCustom(getName());
    }

    @Step("{0} - Press Cancel on popup")
    private void cancelCustom(String elName) {
        getElementClass.getButton(CANCEL_BUTTON).click();
    }

    /**
     * Click on Button marked with annotation @CloseButton or named "closeButton"
     */
    public void close() {
        closeCustom(getName());
    }

    @Step("{0} - Close on popup")
    private void closeCustom(String elName) {
        getElementClass.getButton(CLOSE_BUTTON).click();
    }

    protected String getTextAction() {
        return getWebElement().getText();
    }

    /**
     * @return Get Element’s text
     */
    public final String getText() {
        return getText(getName());
    }

    @Step("{0} - Get text")
    private String getText(String elName) {
        return invoker.doJActionResult("Get text", this::getTextAction);
    }

    /**
     * @param text Specify expected text
     * @return Wait while Element’s text contains expected text. Returns Element’s text
     */
    public final String waitText(String text) {
        return waitText(getName(), text);
    }
    @Step("{0} - Wait text contains [{1}]")
    private String waitText(String elName, String text) {
        return invoker.doJActionResult(format("Wait text contains '%s'", text),
                () -> timer().getResultByCondition(this::getTextAction, t -> t.contains(text)));
    }

    /**
     * @param regEx Specify expected regular expression Text
     * @return Wait while Element’s text matches regEx. Returns Element’s text
     */
    public final String waitMatchText(String regEx) {
        return waitMatchText(getName(), regEx);
    }
    @Step("{0} - Wait text match regex [{1}]")
    private String waitMatchText(String elName, String regEx) {
        return invoker.doJActionResult(format("Wait text match regex '%s'", regEx),
                () -> timer().getResultByCondition(this::getTextAction, t -> t.matches(regEx)));
    }
}