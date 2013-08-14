package com.peergreen.webconsole.scope.deployment.internal.deployed;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.scope.deployment.internal.deployable.DeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.deployable.DeployableContainerType;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableSource;
import com.peergreen.webconsole.scope.deployment.internal.DeploymentActions;
import com.peergreen.webconsole.scope.deployment.internal.actions.DeleteFileShortcutListener;
import com.peergreen.webconsole.scope.deployment.internal.actions.DoClickListener;
import com.peergreen.webconsole.scope.deployment.internal.actions.FilterFiles;
import com.peergreen.webconsole.scope.deployment.internal.actions.SelectAll;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.TableItemStyle;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.dd.DeploymentDropHandler;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import java.net.URI;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.deployment.internal.DeploymentScope.deployed")
public class DeployedPanel extends Panel implements DeployableContainer {

    public final static String TREE_ITEM_ID = "Deployed";

    @Inject
    private ArtifactModelManager artifactModelManager;
    @Inject
    private ArtifactBuilder artifactBuilder;
    @Inject
    private INotifierService notifierService;
    @Inject
    private UIContext uiContext;
    @Inject
    private DeploymentViewManager deploymentViewManager;
    private HierarchicalContainer container = new HierarchicalContainer();

    @Ready
    public void init() {
        setSizeFull();
        Table table = new Table();

        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSpacing(true);
        mainContent.setMargin(true);
        mainContent.setStyleName("deployable-style");
        mainContent.setSizeFull();
        mainContent.setCaption("Deployables artifacts :");

        setContent(mainContent);

        HorizontalLayout toolBar = new HorizontalLayout();
        toolBar.setMargin(true);
        toolBar.setSpacing(true);
        toolBar.setWidth("100%");

        // Select all deployed artifacts
        CheckBox selectAll = new CheckBox("All");
        selectAll.addValueChangeListener(new SelectAll(table));
        toolBar.addComponent(selectAll);
        toolBar.setExpandRatio(selectAll, 1);

        // Filter
        TextField filter = new TextField();
        filter.setInputPrompt("Filter deployed artifacts");
        filter.setWidth("100%");
        filter.addTextChangeListener(new FilterFiles(container, TREE_ITEM_ID));
        toolBar.addComponent(filter);
        toolBar.setComponentAlignment(filter, Alignment.TOP_LEFT);
        toolBar.setExpandRatio(filter, 3);

        HorizontalLayout actionArea = new HorizontalLayout();
        final NativeSelect actionSelection = new NativeSelect();
        actionSelection.addItem(DeploymentActions.UNDEPLOY);
        actionSelection.addItem(DeploymentActions.DELETE);
        actionSelection.setWidth("100px");
        actionSelection.setNullSelectionAllowed(false);

        Button doButton = new Button("Do");
        doButton.addStyleName("default");
        doButton.addClickListener(new DoClickListener(artifactBuilder, table, actionSelection, deploymentViewManager));

        actionArea.addComponent(actionSelection);
        actionArea.addComponent(doButton);
        toolBar.addComponent(actionArea);
        toolBar.setExpandRatio(actionArea, 2);
        toolBar.setComponentAlignment(actionArea, Alignment.TOP_RIGHT);
        mainContent.addComponent(toolBar);

        VerticalLayout deployedContainer = new VerticalLayout();
        DragAndDropWrapper deployedContainerWrapper = new DragAndDropWrapper(deployedContainer);
        deployedContainerWrapper.setDropHandler(new DeploymentDropHandler(deploymentViewManager, this, notifierService));
        deployedContainerWrapper.setSizeFull();
        mainContent.addComponent(deployedContainerWrapper);
        mainContent.setExpandRatio(deployedContainerWrapper, 1.5f);

        container.addContainerProperty(TREE_ITEM_ID, String.class, null);
        table.setSizeFull();
        table.setImmediate(true);
        table.setMultiSelect(true);
        table.setSelectable(true);
        table.setContainerDataSource(container);
        table.setDragMode(Table.TableDragMode.MULTIROW);
        table.setItemCaptionPropertyId(TREE_ITEM_ID);
        table.setCellStyleGenerator(new TableItemStyle(DeployableContainerType.DEPLOYED));
        table.addShortcutListener(new DeleteFileShortcutListener(deploymentViewManager, table, "Delete", ShortcutAction.KeyCode.DELETE, null));
        deployedContainer.addComponent(table);
        deployedContainer.setExpandRatio(table, 1.5f);

        refresh();
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
        if (getDeployable(deployableEntry.getUri()) == null
                && !artifactModelManager.getDeployedRootURIs().contains(deployableEntry.getUri())) {
            deploymentViewManager.deploy(artifactBuilder.build(deployableEntry.getName(), deployableEntry.getUri()));
        }
    }

    @Override
    public void addDeployable(final DeployableEntry deployableEntry) {
        if (getDeployable(deployableEntry.getUri()) == null) {
            final DeployableContainer deployableContainer = this;
            uiContext.getUI().access(new Runnable() {
                @Override
                public void run() {
                    deployableEntry.setContainer(deployableContainer);
                    Item item = container.addItem(deployableEntry);
                    item.getItemProperty(TREE_ITEM_ID).setValue(deployableEntry.getName());
                }
            });
        }
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
    public Component getView() {
        return this;
    }

    @Override
    public void attach() {
        super.attach();
        refresh();
    }

    private void refresh() {
        for (URI uri : artifactModelManager.getDeployedRootURIs()) {
            if (getDeployable(uri) == null) {
                DeployableEntry deployableEntry = new DeployableEntry(uri, DeployableSource.FILE);
                addDeployable(deployableEntry);
            }
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
}