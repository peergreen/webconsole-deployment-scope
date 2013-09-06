package com.peergreen.webconsole.scope.deployment.internal.deploymentplan;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainerType;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableSource;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.TableItemStyle;
import com.peergreen.webconsole.scope.deployment.internal.dd.DeploymentDropHandler;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.peergreen.webconsole.vaadin.ConfirmDialog;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.deployment.internal.DeploymentScope.deployment.plan")
public class DeploymentPlanPanel extends Panel implements DeployableContainer {

    private static final String TREE_ITEM_ID = "Deployable";
    private static final String DEFAULT_DP_NAME_PREFIX = "deployment-plan-";
    public static final String XML_EXTENSION = ".xml";

    @Inject
    private ArtifactBuilder artifactBuilder;
    @Inject
    private INotifierService notifierService;
    @Inject
    private DeploymentViewManager deploymentViewManager;
    @Inject
    private UIContext uiContext;

    private HierarchicalContainer container = new HierarchicalContainer();
    private Label error;
    private CheckBox deployIt;
    private TextField deploymentPlanName;

    @PostConstruct
    public void init() {
        setSizeFull();

        TreeTable table = new TreeTable();

        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSpacing(true);
        mainContent.setMargin(true);
        mainContent.setStyleName("deployable-style");
        mainContent.setSizeFull();

        setContent(mainContent);

        HorizontalLayout toolBar = new HorizontalLayout();
        toolBar.setMargin(true);
        toolBar.setSpacing(true);
        toolBar.setWidth("100%");

        // Deployment Plan name
        Label deploymentPlanNameLabel = new Label("Deployment plan name");
        toolBar.addComponent(deploymentPlanNameLabel);
        toolBar.setExpandRatio(deploymentPlanNameLabel, 1);

        deploymentPlanName = new TextField();
        deploymentPlanName.setInputPrompt(getDefaultName());
        deploymentPlanName.setWidth("100%");
        toolBar.addComponent(deploymentPlanName);
        toolBar.setComponentAlignment(deploymentPlanName, Alignment.TOP_LEFT);
        toolBar.setExpandRatio(deploymentPlanName, 3);

        error = new Label("", ContentMode.HTML);
        error.addStyleName("error");
        error.setSizeUndefined();
        error.addStyleName("light");
        error.addStyleName("v-animate-reveal");
        error.setVisible(false);
        toolBar.addComponent(error);
        toolBar.setComponentAlignment(error, Alignment.TOP_RIGHT);
        toolBar.setExpandRatio(error, 1);
        mainContent.addComponent(toolBar);
        mainContent.setComponentAlignment(toolBar, Alignment.TOP_LEFT);
        mainContent.setExpandRatio(toolBar, 1);

        VerticalLayout deploymentPlanContainer = new VerticalLayout();
        DragAndDropWrapper deploymentPlanContainerWrapper = new DragAndDropWrapper(deploymentPlanContainer);
        DropHandler deploymentPlanDropHandler = new DeploymentDropHandler(deploymentViewManager, this, notifierService);
        deploymentPlanContainerWrapper.setDropHandler(deploymentPlanDropHandler);
        deploymentPlanContainerWrapper.setSizeFull();
        mainContent.addComponent(deploymentPlanContainerWrapper);
        mainContent.setExpandRatio(deploymentPlanContainerWrapper, 10);

        container.addContainerProperty(TREE_ITEM_ID, String.class, null);
        table.setSizeFull();
        table.setImmediate(true);
        table.setMultiSelect(true);
        table.setSelectable(true);
        table.setContainerDataSource(container);
        table.setDragMode(Table.TableDragMode.MULTIROW);
        table.setItemCaptionPropertyId(TREE_ITEM_ID);
        table.setCellStyleGenerator(new TableItemStyle(DeployableContainerType.DEPLOYED));
        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        table.setDropHandler(new OrderedContainerDropHandler(table, deploymentPlanDropHandler));
        table.addShortcutListener(new ShortcutListener("Delete", ShortcutAction.KeyCode.DELETE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                Table table = (Table) target;
                Collection<DeployableEntry> deployableEntries = (Collection<DeployableEntry>) table.getValue();
                for (DeployableEntry deployableEntry : deployableEntries) {
                    removeDeployable(deployableEntry);
                }
            }
        });
        deploymentPlanContainer.addComponent(table);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setSizeFull();
        footer.setSpacing(true);
        footer.setMargin(true);
        footer.addStyleName("footer");
        footer.setWidth("100%");

        deployIt = new CheckBox("Deploy this deployment plan");
        footer.addComponent(deployIt);
        footer.setComponentAlignment(deployIt, Alignment.TOP_LEFT);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(true);
        Button cancel = new Button("Cancel");
        cancel.addClickListener(new CancelButtonListener());
        Button create = new Button("Create");
        create.addClickListener(new CreateButtonListener());
        create.addStyleName("wide");
        create.addStyleName("default");
        buttons.addComponent(cancel);
        buttons.addComponent(create);
        footer.addComponent(buttons);
        footer.setComponentAlignment(buttons, Alignment.TOP_RIGHT);

        mainContent.addComponent(footer);
        mainContent.setComponentAlignment(footer, Alignment.BOTTOM_RIGHT);
        mainContent.setExpandRatio(footer, 1);
    }

    @Override
    public void receive(URI uri) {
        if (getDeployable(uri) == null) {
            DeployableEntry deployableEntry = new DeployableEntry(uri, DeployableSource.FILE);
            receive(deployableEntry);
        }
    }

    @Override
    public void receive(DeployableEntry deployableEntry) {
        if (getDeployable(deployableEntry.getUri()) == null) {
            addDeployable(deployableEntry);
        }
    }

    @Override
    public void addDeployable(final DeployableEntry deployableEntry) {
        if (getDeployable(deployableEntry.getUri()) == null) {
            final DeployableEntry clone = cloneEntry(deployableEntry, null);
            uiContext.getUI().access(new Runnable() {
                @Override
                public void run() {
                    Item item = container.addItem(clone);
                    item.getItemProperty(TREE_ITEM_ID).setValue(clone.getName());
                    container.setChildrenAllowed(deployableEntry, deployableEntry.getChildren().size() > 0);
                    for (DeployableEntry child : deployableEntry.getChildren()) {
                        DeployableEntry childClone = cloneEntry(child, clone);
                        Item childItem = container.addItem(childClone);
                        childItem.getItemProperty(TREE_ITEM_ID).setValue(childClone.getName());
                        container.setParent(childClone, clone);
                    }
                }
            });
        }
    }

    private DeployableEntry cloneEntry(DeployableEntry deployableEntry, DeployableEntry parent) {
        return new DeployableEntry(deployableEntry.getUri(), deployableEntry.getName(), deployableEntry.getSource(), this, parent);
    }

    @Override
    public void removeDeployable(final DeployableEntry deployableEntry) {
        if (getDeployable(deployableEntry.getUri()) != null) {
            uiContext.getUI().access(new Runnable() {
                @Override
                public void run() {
                    container.removeItem(deployableEntry);
                }
            });
        }
    }

    @Override
    public DeployableEntry getDeployable(URI uri) {
        List<DeployableEntry> deployableEntries = (List<DeployableEntry>) container.getItemIds();
        for (DeployableEntry deployableEntry : deployableEntries) {
            if (uri.equals(deployableEntry.getUri())) {
                return deployableEntry;
            }
        }
        return null;
    }

    @Override
    public Component getView() {
        return this;
    }

    @Override
    public HierarchicalContainer getContainer() {
        return container;
    }

    private String getDefaultName() {
        return DEFAULT_DP_NAME_PREFIX + System.currentTimeMillis() + XML_EXTENSION;
    }

    protected void clearDeploymentPlanPanel() {
        container.removeAllItems();
        error.setValue("");
        error.setVisible(false);
        deploymentPlanName.setValue("");
        deploymentPlanName.setInputPrompt(getDefaultName());
        deployIt.setValue(false);
    }

    // FIXME this doesn't change the order of list returned by container.getItemIds()
    protected void moveNode(final Object sourceItemId, final Object targetItemId, boolean before) {
        if (before) {
            container.moveAfterSibling(sourceItemId, targetItemId);
        } else {
            container.moveAfterSibling(targetItemId, sourceItemId);
        }
    }

    private class CancelButtonListener implements Button.ClickListener {

        @Override
        public void buttonClick(Button.ClickEvent event) {
            if (container.size() > 0) {
                ConfirmDialog.show(event.getButton().getUI(),
                        "Save deployment plan",
                        new Label("Would you like to temporary save the content <br />of this deployment plan?", ContentMode.HTML),
                        "Yes",
                        "No",
                        new ConfirmDialogListener());
            } else {
                deploymentViewManager.showDeployedView();
            }
        }

        private class ConfirmDialogListener implements ConfirmDialog.Listener {

            @Override
            public void onClose(boolean isConfirmed) {
                if (!isConfirmed) {
                    clearDeploymentPlanPanel();
                }
                deploymentViewManager.showDeployedView();
            }
        }
    }

    private class CreateButtonListener implements Button.ClickListener {

        @Override
        public void buttonClick(Button.ClickEvent event) {
            if (checkDeploymentPlanName(deploymentPlanName)) {
                String fileName =  Constants.STORAGE_DIRECTORY + File.separator + deploymentPlanName.getValue();
                write(fileName);
                URI uri = new File(fileName).toURI();
                if (deployIt.getValue()) {
                    deploymentViewManager.deploy(artifactBuilder.build(fileName, uri));
                } else {
                    deploymentViewManager.addToDeployable(uri);
                }
                clearDeploymentPlanPanel();
                deploymentViewManager.showDeployedView();
            } else {
                error.setVisible(true);
            }
        }

        private boolean checkDeploymentPlanName(TextField textField) {
            String name = textField.getValue();
            if (name == null || "".equals(name)) {
                name = textField.getInputPrompt();
            }
            if (!name.endsWith(XML_EXTENSION)) {
                error.setValue(String.format("'%s' is not valid", name));
                return false;
            }

            File storageDir = new File(Constants.STORAGE_DIRECTORY);
            if (storageDir.exists()) {
                for (File file : storageDir.listFiles()) {
                    if (name.equals(file.getName())) {
                        error.setValue(String.format("'%s' already exists", name));
                        return false;
                    }
                }
            }
            textField.setValue(name);
            return true;
        }

        private void write(String fileName) {
            List<DeployableEntry> deployableEntries = (List<DeployableEntry>) container.getItemIds();
            if (deployableEntries != null) {
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
                    DeployableEntry firstElement = (DeployableEntry) container.firstItemId();
                    bw.write(firstElement.getUri().toString());
                    bw.newLine();
                    DeployableEntry next = (DeployableEntry) container.nextItemId(firstElement);
                    while (next != null) {
                        bw.write(next.getUri().toString());
                        bw.newLine();
                        next = (DeployableEntry) container.nextItemId(next);
                    }
                    bw.close();
                } catch (IOException e) {
                    notifierService.addNotification(String.format("Cannot write '%s'", fileName));
                }
            }
        }
    }

    private class OrderedContainerDropHandler implements DropHandler {

        private Component source;
        private DropHandler delegate;

        public OrderedContainerDropHandler(Component source, DropHandler delegate) {
            this.source = source;
            this.delegate = delegate;
        }

        @Override
        public void drop(DragAndDropEvent event) {
            DataBoundTransferable t = (DataBoundTransferable) event.getTransferable();
            if (t.getSourceComponent() == source) {
                final Table.AbstractSelectTargetDetails dropData = (AbstractSelect.AbstractSelectTargetDetails) event.getTargetDetails();

                final Object sourceItemId = t.getItemId();
                final Object targetItemId = dropData.getItemIdOver();
                final VerticalDropLocation location = dropData.getDropLocation();

                moveNode(sourceItemId, targetItemId, location.equals(VerticalDropLocation.BOTTOM));
            } else {
                delegate.drop(event);
            }
        }

        @Override
        public AcceptCriterion getAcceptCriterion() {
            return AcceptAll.get();
        }
    }
}
