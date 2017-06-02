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


import com.epam.jdi.uitests.core.interfaces.complex.IPopup;
import com.epam.jdi.uitests.web.selenium.elements.common.Text;
import io.qameta.allure.Step;

import static com.epam.jdi.uitests.core.annotations.functions.Functions.*;

/**
 * Created by Roman_Iovlev on 7/8/2015.
 */
public class Popup extends Text implements IPopup {

    @Override
    protected String getTextAction() {
        return getWebElement().getText();
    }

    protected void okAction() {
        getElementClass.getButton(OK_BUTTON).click();
    }
    protected void cancelAction() {
        getElementClass.getButton(CANCEL_BUTTON).click();
    }
    protected void closeAction() {
        getElementClass.getButton(CLOSE_BUTTON).click();
    }
    /**
     * Click on Button marked with annotation @OkButton or named "okButton"
     */
    public final void ok() {
        ok(getName());
    }

    @Step("{elName} - Press Ok on popup")
    private void ok(String elName) {
        invoker.doJAction("Press Ok on popup", this::okAction);
    }

    /**
     * Click on Button marked with annotation @CancelButton or named "cancelButton"
     */
    public void cancel() {
        cancel(getName());
    }

    @Step("{elName} - Press Cancel on popup")
    private void cancel(String elName) {
        invoker.doJAction("Press Cancel on popup", this::cancelAction);
    }

    /**
     * Click on Button marked with annotation @CloseButton or named "closeButton"
     */
    public void close() {
        close(getName());
    }

    @Step("{elName} - Close on popup")
    private void close(String elName) {
        invoker.doJAction("Close on popup", this::closeAction);
    }

}