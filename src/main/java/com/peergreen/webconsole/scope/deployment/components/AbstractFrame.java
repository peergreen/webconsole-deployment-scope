package com.peergreen.webconsole.scope.deployment.components;

import com.peergreen.webconsole.scope.deployment.IDeploymentView;
import com.peergreen.webconsole.scope.deployment.DeploymentActions;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Mohammed Boukada
 */
public class AbstractFrame extends VerticalLayout {

    private List<DeployableEntry> deployableEntries = new CopyOnWriteArrayList<>();
    private IDeploymentView deploymentView;

    public AbstractFrame(String caption, String[] actions, IDeploymentView deploymentView) {
        this.deploymentView = deploymentView;
        setSpacing(true);
        setMargin(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(true);

        CheckBox selectAll = new CheckBox("All");
        selectAll.addValueChangeListener(new CheckBoxValueListener());
        header.addComponent(selectAll);
        header.setExpandRatio(selectAll, 1);

        final TextField filter = new TextField();
        filter.setInputPrompt("Filter " + caption);
        filter.setWidth("100%");
        filter.addTextChangeListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                doFilter(event.getText());
            }
        });
        filter.addShortcutListener(new ShortcutListener("Clear",
                ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                filter.setValue("");
                showAll();
            }
        });
        header.addComponent(filter);
        header.setComponentAlignment(filter, Alignment.TOP_LEFT);
        header.setExpandRatio(filter, 3);

        final Button download = new Button("D");
        download.setVisible(false);
        header.addComponent(download);

        HorizontalLayout actionArea = new HorizontalLayout();
        final NativeSelect actionSelection = new NativeSelect();
        for (String action : actions) {
            actionSelection.addItem(action);
        }
        actionSelection.setWidth("100px");
        actionSelection.setNullSelectionAllowed(false);
        actionSelection.addShortcutListener(new ShortcutListener("Add",
                ShortcutAction.KeyCode.ENTER, null) {

            @Override
            public void handleAction(Object sender, Object target) {
                doAction((String) actionSelection.getValue(), download);
            }
        });

        Button doButton = new Button("Do");
        doButton.addStyleName("default");
        doButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                doAction((String) actionSelection.getValue(), download);
            }
        });

        actionArea.addComponent(actionSelection);
        actionArea.addComponent(doButton);
        header.addComponent(actionArea);
        header.setExpandRatio(actionArea, 2);
        header.setComponentAlignment(actionArea, Alignment.TOP_RIGHT);
        addComponent(header);
    }

    private void doAction(String action, Button download) {
        if (action != null && !action.equals("")) {
            List<DeployableEntry> selectedDeployableEntries = new ArrayList<>();
            for (DeployableEntry deployableEntry : deployableEntries) {
                if (deployableEntry.getCheckBox().getValue()) {
                    selectedDeployableEntries.add(deployableEntry);
                }
            }
            switch (action) {
                case DeploymentActions.DEPLOY:
                    deploymentView.deploy(selectedDeployableEntries);
                    break;
                case DeploymentActions.UPDATE:
                    deploymentView.update(selectedDeployableEntries);
                    break;
                case DeploymentActions.UNDEPLOY:
                    deploymentView.undeploy(selectedDeployableEntries);
                    break;
                case DeploymentActions.DELETE:
                    deploymentView.delete(selectedDeployableEntries);
                    break;
                case DeploymentActions.DOWNLOAD:
                    deploymentView.download(selectedDeployableEntries, download);
                    break;
            }
        }
    }

    private void doFilter(String s) {
        if ("".equals(s)) {
            showAll();
        }
        else {
            for (DeployableEntry deployableEntry : deployableEntries) {
                if (!deployableEntry.getFileName().contains(s)) {
                    deployableEntry.setVisible(false);
                } else {
                    deployableEntry.setVisible(true);
                }
            }
        }
    }

    private void showAll() {
        for (DeployableEntry deployableEntry : deployableEntries) {
            deployableEntry.setVisible(true);
        }
    }

    public DeployableEntry addDeployableEntry(URI uri) {
        return addDeployableEntry(createDeployableEntry(uri));
    }

    public DeployableEntry addDeployableEntry(DeployableEntry deployableEntry) {
        addComponent(deployableEntry);
        deployableEntries.add(deployableEntry);
        return deployableEntry;
    }

    public void removeDeployableEntry(URI uri) {
        for (DeployableEntry deployableEntry : deployableEntries) {
            if (uri.equals(deployableEntry.getUri())) {
                deployableEntries.remove(deployableEntry);
                break;
            }
        }
    }

    public void removeDeployableEntry(DeployableEntry deployableEntry) {
        removeComponent(deployableEntry);
        deployableEntries.remove(deployableEntry);
    }

    public boolean containsDeployableEntry(DeployableEntry deployableEntry) {
        return deployableEntries.contains(deployableEntry);
    }

    public boolean containsDeployableEntry(URI uri) {
        for (DeployableEntry deployableEntry : deployableEntries) {
            if (uri.equals(deployableEntry.getUri())) return true;
        }
        return false;
    }

    private DeployableEntry createDeployableEntry(URI uri) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSizeFull();
        final CheckBox checkBox = new CheckBox();
        row.addComponent(checkBox);
        row.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                checkBox.setValue(!checkBox.getValue());
            }
        });
        String uriToString = uri.toString();
        Label label = new Label(uriToString.substring(uriToString.lastIndexOf(File.separator) + 1));
        row.addComponent(label);
        Label state = new Label("");
        row.addComponent(state);
        row.setComponentAlignment(checkBox, Alignment.MIDDLE_LEFT);
        row.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
        row.setComponentAlignment(state, Alignment.MIDDLE_RIGHT);

        DeployableEntry deployableEntry = new DeployableEntry(row, checkBox, uri, state);
        deployableEntry.setDragStartMode(DragAndDropWrapper.DragStartMode.COMPONENT);
        deployableEntry.setSizeUndefined();
        deployableEntry.addStyleName("deployable-entry");

        return deployableEntry;
    }

    private class CheckBoxValueListener implements Property.ValueChangeListener {

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            Boolean state = (Boolean) event.getProperty().getValue();
            if (state) {
                for (DeployableEntry deployableEntry : deployableEntries) {
                    deployableEntry.getCheckBox().setValue(true);
                }
            } else {
                for (DeployableEntry deployableEntry : deployableEntries) {
                    deployableEntry.getCheckBox().setValue(false);
                }
            }
        }
    }
}
