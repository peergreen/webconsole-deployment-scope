package com.peergreen.webconsole.scope.deployment.components;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import java.io.File;
import java.net.URI;

/**
 * @author Mohammed Boukada
 */
public class DeployableEntry extends DragAndDropWrapper {
    private CheckBox checkBox;
    private URI uri;
    private Label state;

    public DeployableEntry(HorizontalLayout row, CheckBox checkBox, URI uri, Label state) {
        super(row);
        this.checkBox = checkBox;
        this.uri = uri;
        this.state = state;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public URI getUri() {
        return uri;
    }

    public String getFileName() {
        return uri.toString().substring(uri.toString().lastIndexOf(File.separator) + 1);
    }

    public void setState(String value) {
        state.setValue(value);
    }
}
