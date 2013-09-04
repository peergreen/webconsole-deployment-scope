package com.peergreen.webconsole.scope.deployment.internal.deployable.maven;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.deployment.repository.RepositoryManager;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.scope.deployment.internal.DeploymentActions;
import com.peergreen.webconsole.scope.deployment.internal.actions.DoClickListener;
import com.peergreen.webconsole.scope.deployment.internal.actions.FilterFiles;
import com.peergreen.webconsole.scope.deployment.internal.container.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableSource;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.TreeItemExpandListener;
import com.peergreen.webconsole.scope.deployment.internal.deployable.Deployable;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import org.apache.felix.ipojo.annotations.Requires;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.deployment.internal.deployable.DeployablePanel.maven")
@Deployable("maven")
public class MavenView extends AbstractDeployableContainer {

    private static final String CLEAR_FILTER = "Clear filters";

    @Inject
    private UIContext uiContext;
    @Inject
    private ArtifactBuilder artifactBuilder;
    @Inject
    private ArtifactModelManager artifactModelManager;
    @Inject
    private DeploymentViewManager deploymentViewManager;
    @Inject
    private RepositoryManager repositoryManager;
    @Requires(filter = "(repository.type=" + RepositoryType.FACADE + ")")
    private MavenRepositoryService mavenRepositoryService;

    protected MavenView() {
        super(DeployableSource.MAVEN);
    }

    @Ready
    public void init() {
        super.init(uiContext, artifactModelManager);

        repositoryManager.addRepository("https://forge.peergreen.com/repository/content/repositories/releases/", "Peergreen Releases", RepositoryType.MAVEN);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(true);

        final TextField filterG = new TextField();
        filterG.setInputPrompt("Filter by group id");
        filterG.addTextChangeListener(new FilterFiles(MVN_GROUP_ID, getContainer()));
        header.addComponent(filterG);
        header.setComponentAlignment(filterG, Alignment.TOP_LEFT);

        final TextField filterA = new TextField();
        filterA.setInputPrompt("Filter by artifact id");
        filterA.addTextChangeListener(new FilterFiles(MVN_ARTIFACT_ID, getContainer()));
        header.addComponent(filterA);
        header.setComponentAlignment(filterA, Alignment.TOP_LEFT);

        final TextField filterV = new TextField();
        filterV.setInputPrompt("Filter by version");
        filterV.addTextChangeListener(new FilterFiles(MVN_VERSION, getContainer()));
        header.addComponent(filterV);
        header.setComponentAlignment(filterV, Alignment.TOP_LEFT);

        HorizontalLayout actionArea = new HorizontalLayout();
        final NativeSelect actionSelection = new NativeSelect();
        actionSelection.addItem(DeploymentActions.DEPLOY);
        actionSelection.addItem(DeploymentActions.DEPLOYMENT_PLAN);
        actionSelection.addItem(CLEAR_FILTER);
        actionSelection.setWidth("100px");
        actionSelection.setNullSelectionAllowed(false);

        Button doButton = new Button("Do");
        doButton.addStyleName("default");
        doButton.addClickListener(new DoClickListener(artifactBuilder, getTree(), actionSelection, deploymentViewManager));
        doButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (CLEAR_FILTER.equals(actionSelection.getValue())) {
                    getContainer().removeAllContainerFilters();
                }
            }
        });

        actionArea.addComponent(actionSelection);
        actionArea.addComponent(doButton);
        header.addComponent(actionArea);
        header.setExpandRatio(actionArea, 2);
        header.setComponentAlignment(actionArea, Alignment.TOP_RIGHT);
        addComponent(header);

        HorizontalLayout repositoryInfo = new HorizontalLayout();
        repositoryInfo.setWidth("100%");
        repositoryInfo.addComponent(getFetching());
        repositoryInfo.setComponentAlignment(getFetching(), Alignment.MIDDLE_LEFT);
        addComponent(repositoryInfo);

        getTree().addExpandListener(new TreeItemExpandListener(this, mavenRepositoryService));
        addComponent(getTree());
        setExpandRatio(getTree(), 1.5f);

        updateTree();
    }

    @Override
    protected void updateTree() {
        uiContext.getUI().access(new Runnable() {
            @Override
            public void run() {
                updateTree(mavenRepositoryService);
            }
        });
    }
}
