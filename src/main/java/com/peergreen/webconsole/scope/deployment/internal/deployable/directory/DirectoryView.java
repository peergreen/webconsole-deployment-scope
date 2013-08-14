package com.peergreen.webconsole.scope.deployment.internal.deployable.directory;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.scope.deployment.internal.DeploymentActions;
import com.peergreen.webconsole.scope.deployment.internal.actions.DeleteFileShortcutListener;
import com.peergreen.webconsole.scope.deployment.internal.actions.DoClickListener;
import com.peergreen.webconsole.scope.deployment.internal.actions.FilterFiles;
import com.peergreen.webconsole.scope.deployment.internal.deployable.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.deployable.Deployable;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.deployment.internal.deployable.DeployablePanel.directory")
@Deployable("directory")
public class DirectoryView extends AbstractDeployableContainer {

    public final static String DIRECTORY_FACTORY_PID = "com.peergreen.deployment.repository.directory";

    @Requires(from = DIRECTORY_FACTORY_PID)
    Factory directoryRepositoryFactory;
    @Requires(filter = "(repository.type=" + RepositoryType.FACADE + ")")
    private DirectoryRepositoryService directoryRepositoryService;
    private List<ComponentInstance> componentInstances = new ArrayList<>();

    @Inject
    private INotifierService notifierService;
    @Inject
    private ArtifactBuilder artifactBuilder;
    @Inject
    private ArtifactModelManager artifactModelManager;
    @Inject
    private UIContext uiContext;
    @Inject
    private DeploymentViewManager deploymentViewManager;
    @Inject
    BundleContext bundleContext;

    @Bind(aggregate = true, optional = true, filter = "(repository.type=" + RepositoryType.LOCAL +")")
    public void bindDirectoryRepositoryService(DirectoryRepositoryService directoryRepositoryService) {
        updateTree();
    }

    @Ready
    public void init() {
        super.init(uiContext, artifactModelManager, notifierService);

        checkDirectoryRepositoryService(Constants.STORAGE_DIRECTORY);
        checkDirectoryRepositoryService(System.getProperty("user.dir") + File.separator + "deploy");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(true);

        final TextField filter = new TextField();
        filter.setInputPrompt("Filter deployable files");
        filter.setWidth("100%");
        filter.addTextChangeListener(new FilterFiles(container, DEPLOYABLE_NAME));
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
        doButton.addClickListener(new DoClickListener(artifactBuilder, tree, actionSelection, deploymentViewManager));

        actionArea.addComponent(actionSelection);
        actionArea.addComponent(doButton);
        header.addComponent(actionArea);
        header.setExpandRatio(actionArea, 2);
        header.setComponentAlignment(actionArea, Alignment.TOP_RIGHT);
        addComponent(header);

        addComponent(fetching);

        tree.addShortcutListener(new DeleteFileShortcutListener(deploymentViewManager, tree, "Delete", ShortcutAction.KeyCode.DELETE, null));
        addComponent(tree);
        setExpandRatio(tree, 1.5f);
    }

    protected void updateTree() {
        uiContext.getUI().access(new Runnable() {
            @Override
            public void run() {
                updateTree(directoryRepositoryService);
            }
        });
    }

    private void checkDirectoryRepositoryService(String url) {
        //String filter = String.format("(&(%s=%s)(%s=%s))", "architecture.instance", DIRECTORY_FACTORY_PID + "*", "repository.url", url);
        //if (getServiceReferences(bundleContext.getBundle(), Architecture.class.getName(), filter).length == 0) {
            try {
                String name = url.substring(url.lastIndexOf(File.separator));
                Dictionary<String, Object> properties = new Hashtable<>();
                properties.put("repository.type", RepositoryType.LOCAL);
                properties.put("repository.name", name);
                properties.put("repository.url", url);
                componentInstances.add(directoryRepositoryFactory.createComponentInstance(properties));
            } catch (MissingHandlerException | ConfigurationException | UnacceptableConfiguration e) {
                // do nothing
            }
        //}
    }

    private static ServiceReference[] getServiceReferences(Bundle bundle, String itf, String filter) {
        ServiceReference[] refs;
        try {
            // Get all the service references
            refs = bundle.getBundleContext().getServiceReferences(itf, filter);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(
                    "Cannot get service references: " + e.getMessage());
        }
        if (refs == null) {
            return new ServiceReference[0];
        } else {
            return refs;
        }
    }

    @Invalidate
    public void stop() {
        for (ComponentInstance instance : componentInstances) {
            instance.stop();
            instance.dispose();
        }
    }

    @Override
    public void attach() {
        super.attach();
        updateTree();
    }
}
