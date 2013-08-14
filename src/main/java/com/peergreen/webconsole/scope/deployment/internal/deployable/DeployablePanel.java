package com.peergreen.webconsole.scope.deployment.internal.deployable;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Link;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.Unlink;
import com.peergreen.webconsole.scope.deployment.internal.dd.DeploymentDropHandler;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.osgi.service.cm.ConfigurationAdmin;

import java.net.URI;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.deployment.internal.DeploymentScope.deployable")
public class DeployablePanel extends Panel implements DeployableContainer {

    @Inject
    ArtifactBuilder artifactBuilder;
    @Inject
    private ArtifactModelManager artifactModelManager;
    @Inject
    private ConfigurationAdmin configurationAdmin;
    @Inject
    private INotifierService notifierService;
    @Inject
    private UIContext uiContext;
    @Inject
    private DeploymentViewManager deploymentViewManager;

    private DeployableContainer directoryView;
    private DeployableContainer mavenView;
    private DeployableContainer storeView;
    private List<DeployableContainer> containers = new CopyOnWriteArrayList<>();
    private TabSheet tabSheet = new TabSheet();

    @Ready
    public void init() {

        setSizeFull();

        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSpacing(true);
        mainContent.setMargin(true);
        mainContent.setStyleName("deployable-style");
        mainContent.setSizeFull();
        mainContent.setCaption("Deployed artifacts :");

        tabSheet.setSizeFull();
        mainContent.addComponent(tabSheet);

        DragAndDropWrapper mainContentWrapper = new DragAndDropWrapper(mainContent);
        mainContentWrapper.setDropHandler(new DeploymentDropHandler(deploymentViewManager, this, notifierService));
        mainContentWrapper.setSizeFull();

        setContent(mainContentWrapper);
    }

    @Override
    public void receive(URI uri) {
        directoryView.receive(uri);
    }

    @Override
    public void receive(DeployableEntry deployableEntry) {
        if (artifactModelManager.getDeployedRootURIs().contains(deployableEntry.getUri())) {
            deploymentViewManager.undeploy(artifactBuilder.build(deployableEntry.getName(), deployableEntry.getUri()));
        } else {
            deploymentViewManager.addToDeployable(deployableEntry.getUri());
        }
    }

    @Override
    public void addDeployable(DeployableEntry deployableEntry) {
        switch (deployableEntry.getSource()) {
            case FILE:
                directoryView.addDeployable(deployableEntry);
                break;
            case MAVEN:
                mavenView.addDeployable(deployableEntry);
                break;
        }
    }

    @Override
    public void removeDeployable(DeployableEntry deployableEntry) {
        switch (deployableEntry.getSource()) {
            case FILE:
                directoryView.removeDeployable(deployableEntry);
                break;
            case MAVEN:
                mavenView.removeDeployable(deployableEntry);
                break;
        }
    }

    @Override
    public DeployableEntry getDeployable(URI uri) {
        DeployableEntry deployableEntry = directoryView.getDeployable(uri);
        if (deployableEntry == null) {
            deployableEntry = mavenView.getDeployable(uri);
        }
        return deployableEntry;
    }

    @Override
    public Component getView() {
        return this;
    }

    @Link("all")
    public void addAllView(DeployableContainer allPanel, Dictionary properties) {
        tabSheet.addTab(allPanel.getView(), getDeployableCaption(properties), null, 0);
        tabSheet.setSelectedTab(0);
        containers.add(allPanel);
    }

    @Unlink("all")
    public void removeAllView(DeployableContainer allPanel) {
        if (containers.contains(allPanel)) {
            tabSheet.removeComponent(allPanel.getView());
        }
    }

    @Link("directory")
    public void addDirectoryView(DeployableContainer directoryView, Dictionary properties) {
        this.directoryView = directoryView;
        int position = (containers.size() >= 1) ? 1 : containers.size();
        tabSheet.addTab(directoryView.getView(), getDeployableCaption(properties), null, position);
        containers.add(directoryView);
    }

    @Unlink("directory")
    public void removeDirectoryView(DeployableContainer directoryView) {
        if (containers.contains(directoryView)) {
            this.directoryView = null;
            tabSheet.removeComponent(directoryView.getView());
        }
    }

    @Link("maven")
    public void addMavenView(DeployableContainer mavenView, Dictionary properties) {
        this.mavenView = mavenView;
        int position = (containers.size() >= 2) ? 2 : containers.size();
        tabSheet.addTab(mavenView.getView(), getDeployableCaption(properties), null, position);
        containers.add(mavenView);
    }

    @Unlink("maven")
    public void removeMavenView(DeployableContainer mavenView) {
        if (containers.contains(mavenView)) {
            this.mavenView = null;
            tabSheet.removeComponent(mavenView.getView());
        }
    }

    @Link("store")
    public void addStoreView(DeployableContainer storeView, Dictionary properties) {
        this.storeView = storeView;
        int position = (containers.size() >= 3) ? 3 : containers.size();
        tabSheet.addTab(storeView.getView(), getDeployableCaption(properties), null, position);
        containers.add(storeView);
    }

    @Unlink("store")
    public void removeStoreView(DeployableContainer storeView) {
        if (containers.contains(storeView)) {
            this.storeView = null;
            tabSheet.removeComponent(storeView.getView());
        }
    }

    private String getDeployableCaption(Dictionary properties) {
        String caption = (String) properties.get("deployable.value");
        if (caption == null) {
            caption = "deployable";
        }
        return caption;
    }
}
