package com.peergreen.webconsole.scope.deployment.internal.deployable;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Link;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.Unlink;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.dd.DeploymentDropHandler;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.ClassResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

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
    private ArtifactBuilder artifactBuilder;
    @Inject
    private ArtifactModelManager artifactModelManager;
    @Inject
    private INotifierService notifierService;
    @Inject
    private DeploymentViewManager deploymentViewManager;

    private DeployableContainer directoryView;
    private DeployableContainer mavenView;
    private Panel manager;
//    private DeployableContainer storeView;
    private List<Component> containers = new CopyOnWriteArrayList<>();
    private TabSheet tabSheet = new TabSheet();

    @Ready
    public void init() {

        setSizeFull();

        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSpacing(true);
        mainContent.setMargin(true);
        mainContent.setStyleName("deployable-style");
        mainContent.setSizeFull();

        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        Button openManager = new Button("Edit repositories");
        openManager.addStyleName("link");
        openManager.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (tabSheet.getTab(manager) == null) {
                    tabSheet.addTab(manager, "Manager", new ClassResource(getClass(), "/images/22x22/configuration.png"), containers.size()).setClosable(true);
                    tabSheet.setSelectedTab(manager);
                }
            }
        });
        header.addComponent(openManager);
        header.setComponentAlignment(openManager, Alignment.MIDDLE_RIGHT);
        mainContent.addComponent(header);

        tabSheet.setSizeFull();
        mainContent.addComponent(tabSheet);
        mainContent.setExpandRatio(tabSheet, 1.5f);

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

    @Override
    public HierarchicalContainer getContainer() {
        return null;
    }

//  FIXME how display deployables in this view?
//    @Link("all")
//    public void addAllView(DeployableContainer allPanel, Dictionary properties) {
//        tabSheet.addTab(allPanel.getView(), getDeployableCaption(properties), null, 0);
//        tabSheet.setSelectedTab(0);
//        containers.add(allPanel);
//    }
//
//    @Unlink("all")
//    public void removeAllView(DeployableContainer allPanel) {
//        if (containers.contains(allPanel)) {
//            tabSheet.removeComponent(allPanel.getView());
//        }
//    }

    @Link("directory")
    public void addDirectoryView(DeployableContainer directoryView, Dictionary properties) {
        this.directoryView = directoryView;
        int position = 0;
        tabSheet.addTab(directoryView.getView(), getDeployableCaption(properties), new ClassResource(getClass(), "/images/22x22/directory.png"), position);
        containers.add(directoryView.getView());
    }

    @Unlink("directory")
    public void removeDirectoryView(DeployableContainer directoryView) {
        if (containers.contains(directoryView.getView())) {
            this.directoryView = null;
            tabSheet.removeComponent(directoryView.getView());
        }
    }

    @Link("maven")
    public void addMavenView(DeployableContainer mavenView, Dictionary properties) {
        this.mavenView = mavenView;
        int position = (containers.size() >= 1) ? 1 : containers.size();
        tabSheet.addTab(mavenView.getView(), getDeployableCaption(properties), new ClassResource(getClass(), "/images/22x22/maven.png"), position);
        containers.add(mavenView.getView());
    }

    @Unlink("maven")
    public void removeMavenView(DeployableContainer mavenView) {
        if (containers.contains(mavenView.getView())) {
            this.mavenView = null;
            tabSheet.removeComponent(mavenView.getView());
        }
    }

    @Link("manager")
    public void addRepositoryManagerView(Panel repositoryManagerView, Dictionary properties) {
        this.manager = repositoryManagerView;
    }

    @Unlink("manager")
    public void removeRepositoryManagerView(Panel repositoryManagerView) {
        if (containers.contains(repositoryManagerView)) {
            manager = null;
            tabSheet.removeComponent(repositoryManagerView);
        }
    }

//    @Link("store")
//    public void addStoreView(DeployableContainer storeView, Dictionary properties) {
//        this.m2View = storeView;
//        int position = (containers.size() >= 3) ? 3 : containers.size();
//        tabSheet.addTab(storeView.getView(), getDeployableCaption(properties), null, position);
//        containers.add(storeView);
//    }

//    @Unlink("store")
//    public void removeStoreView(DeployableContainer storeView) {
//        if (containers.contains(storeView)) {
//            this.m2View = null;
//            tabSheet.removeComponent(storeView.getView());
//        }
//    }

    private String getDeployableCaption(Dictionary properties) {
        String caption = (String) properties.get("deployable.value");
        if (caption == null) {
            caption = "deployable";
        }
        return caption;
    }
}
