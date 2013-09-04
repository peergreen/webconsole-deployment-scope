package com.peergreen.webconsole.scope.deployment.internal.deployable.directory;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.RepositoryManager;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.scope.deployment.internal.DeploymentActions;
import com.peergreen.webconsole.scope.deployment.internal.actions.DeleteFileShortcutListener;
import com.peergreen.webconsole.scope.deployment.internal.actions.DoClickListener;
import com.peergreen.webconsole.scope.deployment.internal.actions.FilterFiles;
import com.peergreen.webconsole.scope.deployment.internal.container.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableSource;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.TreeItemExpandListener;
import com.peergreen.webconsole.scope.deployment.internal.deployable.Deployable;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import org.apache.felix.ipojo.annotations.Requires;

import java.io.File;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.deployment.internal.deployable.DeployablePanel.directory")
@Deployable("directory")
public class DirectoryView extends AbstractDeployableContainer {

    @Requires(filter = "(repository.type=" + RepositoryType.FACADE + ")")
    private DirectoryRepositoryService directoryRepositoryService;
    @Inject
    private RepositoryManager repositoryManager;
    @Inject
    private ArtifactBuilder artifactBuilder;
    @Inject
    private ArtifactModelManager artifactModelManager;
    @Inject
    private UIContext uiContext;
    @Inject
    private DeploymentViewManager deploymentViewManager;

    protected DirectoryView() {
        super(DeployableSource.FILE);
    }

    @Ready
    public void init() {
        super.init(uiContext, artifactModelManager);

        File deploy = new File(System.getProperty("user.dir") + File.separator + "deploy");
        repositoryManager.addRepository(formatUrl(deploy), "Deploy", RepositoryType.DIRECTORY);

        File tmp = new File(Constants.STORAGE_DIRECTORY);
        if (!tmp.exists()) {
            tmp.mkdirs();
        }
        repositoryManager.addRepository(formatUrl(tmp), "Temporary directory", RepositoryType.DIRECTORY);

        File m2 = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
        if (m2.exists()) {
            repositoryManager.addRepository(formatUrl(m2), "Local M2 repository", RepositoryType.DIRECTORY);
        }

        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(true);

        final TextField filter = new TextField();
        filter.setInputPrompt("Filter deployable files");
        filter.setWidth("100%");
        filter.addTextChangeListener(new FilterFiles(DEPLOYABLE_NAME, getContainer()));
        header.addComponent(filter);
        header.setComponentAlignment(filter, Alignment.TOP_LEFT);
        header.setExpandRatio(filter, 3);

        HorizontalLayout actionArea = new HorizontalLayout();
        final NativeSelect actionSelection = new NativeSelect();
        actionSelection.addItem(DeploymentActions.DEPLOY);
        actionSelection.addItem(DeploymentActions.DELETE);
        actionSelection.addItem(DeploymentActions.DEPLOYMENT_PLAN);
        actionSelection.setWidth("100px");
        actionSelection.setNullSelectionAllowed(false);

        Button doButton = new Button("Do");
        doButton.addStyleName("default");
        doButton.addClickListener(new DoClickListener(artifactBuilder, getTree(), actionSelection, deploymentViewManager));

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

        getTree().addShortcutListener(new DeleteFileShortcutListener(deploymentViewManager, getTree(), "Delete", ShortcutAction.KeyCode.DELETE, null));
        getTree().addExpandListener(new TreeItemExpandListener(this, directoryRepositoryService));

        addComponent(getTree());
        setExpandRatio(getTree(), 1.5f);
    }

    protected void updateTree() {
        uiContext.getUI().access(new Runnable() {
            @Override
            public void run() {
                updateTree(directoryRepositoryService);
            }
        });
    }

    private String formatUrl(File file) {
        String url = file.toURI().toString();
        if (url.charAt(url.length() - 1) != '/') {
            url += '/';
        }
        return url;
    }
}
