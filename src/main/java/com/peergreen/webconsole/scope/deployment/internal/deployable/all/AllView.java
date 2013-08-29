package com.peergreen.webconsole.scope.deployment.internal.deployable.all;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.scope.deployment.internal.DeploymentActions;
import com.peergreen.webconsole.scope.deployment.internal.actions.DoClickListener;
import com.peergreen.webconsole.scope.deployment.internal.actions.FilterFiles;
import com.peergreen.webconsole.scope.deployment.internal.deployable.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.deployable.Deployable;
import com.peergreen.webconsole.scope.deployment.internal.deployable.DeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableSource;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.TreeItemExpandListener;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import org.apache.felix.ipojo.annotations.Requires;

import java.net.URI;
import java.util.Collections;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.deployment.internal.deployable.DeployablePanel.all")
@Deployable("quick search")
public class AllView extends AbstractDeployableContainer {

    public final static String DIRECTORY_ITEM_ID = "Files";
    public final static String MAVEN_ITEM_ID = "Maven";

    @Inject
    private INotifierService notifierService;
    @Inject
    private UIContext uiContext;
    @Inject
    private ArtifactBuilder artifactBuilder;
    @Inject
    private ArtifactModelManager artifactModelManager;
    @Inject
    private DeploymentViewManager deploymentViewManager;


    @Requires(filter = "(extension.point=com.peergreen.webconsole.scope.deployment.internal.deployable.DeployablePanel.directory)")
    private DeployableContainer directoryContainer;
    @Requires(filter = "(extension.point=com.peergreen.webconsole.scope.deployment.internal.deployable.DeployablePanel.maven)")
    private DeployableContainer mavenContainer;
    @Requires(filter = "(repository.type=" + RepositoryType.FACADE + ")")
    private MavenRepositoryService mavenRepositoryService;

    protected AllView() {
        super(null);
    }

    @Ready
    public void init() {
        setSizeFull();
        setSpacing(true);
        setMargin(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(true);

        final TextField filter = new TextField();
        filter.setInputPrompt("Type artifact name or maven dependency");
        filter.setWidth("100%");
        filter.addTextChangeListener(new FilterFiles(AbstractDeployableContainer.DEPLOYABLE_NAME, directoryContainer.getContainer(), mavenContainer.getContainer()));
        header.addComponent(filter);
        header.setComponentAlignment(filter, Alignment.TOP_LEFT);
        header.setExpandRatio(filter, 3);

        HorizontalLayout actionArea = new HorizontalLayout();
        final NativeSelect actionSelection = new NativeSelect();
        actionSelection.addItem(DeploymentActions.DEPLOY);
        actionSelection.addItem(DeploymentActions.DEPLOYMENT_PLAN);
        actionSelection.setWidth("100px");
        actionSelection.setNullSelectionAllowed(false);

        TreeTable directoryTable = createDeployableTable(AbstractDeployableContainer.DEPLOYABLE_NAME, DIRECTORY_ITEM_ID, directoryContainer.getContainer());
        TreeTable mavenTable = createDeployableTable(AbstractDeployableContainer.DEPLOYABLE_NAME, MAVEN_ITEM_ID, mavenContainer.getContainer());
        mavenTable.addExpandListener(new TreeItemExpandListener(this, mavenRepositoryService));

        Button doButton = new Button("Do");
        doButton.addStyleName("default");
        doButton.addClickListener(new DoClickListener(artifactBuilder, directoryTable, actionSelection, deploymentViewManager));

        actionArea.addComponent(actionSelection);
        actionArea.addComponent(doButton);
        header.addComponent(actionArea);
        header.setExpandRatio(actionArea, 2);
        header.setComponentAlignment(actionArea, Alignment.TOP_RIGHT);
        addComponent(header);

        addComponent(directoryTable);
        setExpandRatio(directoryTable, 1.5f);
        addComponent(mavenTable);
        setExpandRatio(mavenTable, 1.5f);
    }

    private TreeTable createDeployableTable(String itemId, String caption, HierarchicalContainer container) {
        TreeTable table = new TreeTable(caption);
        table.setSizeFull();
        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        table.setImmediate(true);
        table.setMultiSelect(true);
        table.setSelectable(true);
        table.setColumnCollapsingAllowed(true);
        table.setContainerDataSource(container, Collections.singleton(itemId));
        table.setDragMode(Table.TableDragMode.MULTIROW);
        table.setItemCaptionPropertyId(itemId);
        return table;
    }

    @Override
    public HierarchicalContainer getContainer() {
        return null;
    }

    @Override
    protected void updateTree() {

    }

}
