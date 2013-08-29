package com.peergreen.webconsole.scope.deployment.internal.deployable;

import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableSource;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.MavenDeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.TreeItemClickListener;
import com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher.DirectoryDeployableFetcher;
import com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher.MavenDeployableFetcher;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.ClassResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Mohammed Boukada
 */
public abstract class AbstractDeployableContainer extends VerticalLayout implements DeployableContainer {
    public final static String DEPLOYABLE_NAME = "Deployables";
    public final static String MVN_GROUP_ID = "g";
    public final static String MVN_ARTIFACT_ID = "a";
    public final static String MVN_VERSION = "v";
    public final static String ICON = "icon";

    public final static String XML_EXTENSION = ".xml";
    public final static String JAR_EXTENSION = ".jar";
    public final static String WAR_EXTENSION = ".war";

    private DeployableSource deployableSource;

    protected TreeTable tree = new TreeTable();
    protected HierarchicalContainer container = new HierarchicalContainer();
    protected Label fetching = new Label("");

    private UIContext uiContext;
    private ArtifactModelManager artifactModelManager;

    protected AbstractDeployableContainer(DeployableSource deployableSource) {
        this.deployableSource = deployableSource;

        container.addContainerProperty(DEPLOYABLE_NAME, String.class, null);
        container.addContainerProperty(ICON, Resource.class, null);

        setSizeFull();
        setSpacing(true);
        setMargin(true);

        container.addContainerProperty(DEPLOYABLE_NAME, String.class, null);
        container.addContainerProperty(MVN_GROUP_ID, String.class, null);
        container.addContainerProperty(MVN_ARTIFACT_ID, String.class, null);
        container.addContainerProperty(MVN_VERSION, String.class, null);
        container.addContainerProperty(ICON, Resource.class, null);
        tree.setSizeFull();
        tree.setSelectable(true);
        tree.setImmediate(true);
        tree.setMultiSelect(true);
        tree.setContainerDataSource(container);
        tree.setDragMode(Table.TableDragMode.MULTIROW);
        tree.setItemCaptionPropertyId(DEPLOYABLE_NAME);
        tree.setItemIconPropertyId(ICON);
        tree.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        tree.setColumnCollapsingAllowed(true);
        tree.setColumnCollapsed(ICON, true);
        tree.setColumnCollapsed(MVN_GROUP_ID, true);
        tree.setColumnCollapsed(MVN_ARTIFACT_ID, true);
        tree.setColumnCollapsed(MVN_VERSION, true);
        tree.setSortContainerPropertyId(DEPLOYABLE_NAME);
        tree.setSortAscending(true);
        tree.addItemClickListener(new TreeItemClickListener());
        fetching.setVisible(false);
    }

    protected void init(UIContext uiContext, ArtifactModelManager artifactModelManager) {
        this.uiContext = uiContext;
        this.artifactModelManager = artifactModelManager;
    }

    @Override
    public void receive(URI uri) {
        DeployableEntry deployableEntry = new DeployableEntry(uri, DeployableSource.FILE);
        addDeployable(deployableEntry);
    }

    @Override
    public void receive(DeployableEntry deployableEntry) {
        addDeployable(deployableEntry);
    }

    @Override
    public void addDeployable(DeployableEntry deployableEntry) {
        if (getDeployable(deployableEntry.getUri()) == null
                && (deployableSource == null || deployableSource.equals(deployableEntry.getSource()))) {
            deployableEntry.setContainer(this);

            String url = deployableEntry.getUri().toString();
            String parentUrl = url.substring(0, url.lastIndexOf(File.separator));

            if (deployableEntry.getParent() == null) {
                try {
                    URI parentUri = new URI(parentUrl);
                    DeployableEntry parent = getDeployable(parentUri);
                    if (parent != null) {
                        deployableEntry.setParent(parent);
                    }
                } catch (URISyntaxException e) {
                    // do nothing
                }
            }

            if (!(deployableEntry instanceof MavenDeployableEntry && deployableEntry.getParent() == null)) {
                addItemToContainer(deployableEntry, getContainerProperties(deployableEntry));
            }
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
    public DeployableEntry getDeployable(URI uri) {
        if (uri != null) {
            List<DeployableEntry> deployableEntries = new CopyOnWriteArrayList<>((Collection<? extends DeployableEntry>) container.getItemIds());
            for (DeployableEntry deployableEntry : deployableEntries) {
                if (uri.equals(deployableEntry.getUri())) {
                    return deployableEntry;
                }
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

    protected Resource getResource(DeployableEntry deployableEntry) {
        if (!deployableEntry.isDeployable()) {
            return new ClassResource(getClass(), "/images/16x16/directory-icon.png");
        } else {
            String name = deployableEntry.getName();
            String extension = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";
            switch (extension) {
                case XML_EXTENSION :
                    return new ClassResource(getClass(), "/images/16x16/xml-icon.png");
                case JAR_EXTENSION :
                    return new ClassResource(getClass(), "/images/16x16/jar-icon.png");
                case WAR_EXTENSION :
                    return new ClassResource(getClass(), "/images/16x16/war-icon.png");
                default :
                    return new ClassResource(getClass(), "/images/16x16/file-icon.png");
            }
        }
    }

    protected abstract void updateTree();

    protected void updateTree(DirectoryRepositoryService directoryRepositoryService) {
        DirectoryDeployableFetcher fetcher = new DirectoryDeployableFetcher(this, directoryRepositoryService);
        fetcher.start();
    }

    protected void updateTree(MavenRepositoryService mavenRepositoryService) {
        MavenDeployableFetcher fetcher = new MavenDeployableFetcher(this, mavenRepositoryService);
        fetcher.start();
    }

    public void addItemToContainer(final DeployableEntry deployableEntry, final Map<String, Object> properties) {
        uiContext.getUI().access(new Runnable() {
            @Override
            public void run() {
                Item item = container.addItem(deployableEntry);
                for (Map.Entry<String, Object> property : properties.entrySet()) {
                    Property prop = item.getItemProperty(property.getKey());
                    if (prop != null) {
                            prop.setValue(property.getValue());
                    }
                }
                if (deployableEntry.getParent() != null) {
                    container.setParent(deployableEntry, deployableEntry.getParent());
                    tree.setCollapsed(deployableEntry.getParent(), false);
                }
                container.setChildrenAllowed(deployableEntry, !deployableEntry.isDeployable());
            }
        });
    }

    public Map<String, Object> getContainerProperties(DeployableEntry deployableEntry) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(DEPLOYABLE_NAME, deployableEntry.getName());
        properties.put(ICON, getResource(deployableEntry));
        if (DeployableSource.MAVEN.equals(deployableEntry.getSource())) {
            MavenDeployableEntry mavenDeployableEntry = (MavenDeployableEntry) deployableEntry;
            if (mavenDeployableEntry.getArtifactInfo() != null) {
                properties.put(MVN_GROUP_ID, mavenDeployableEntry.getArtifactInfo().groupId);
                properties.put(MVN_ARTIFACT_ID, mavenDeployableEntry.getArtifactInfo().artifactId);
                properties.put(MVN_VERSION, mavenDeployableEntry.getArtifactInfo().version);
            }
        }
        return properties;
    }

    public void startFetching(final String message) {
        uiContext.getUI().access(new Runnable() {
            @Override
            public void run() {
                fetching.setValue(message);
            }
        });
    }

    public void stopFetching() {
        uiContext.getUI().access(new Runnable() {
            @Override
            public void run() {
                fetching.setValue("");
                tree.sort();
            }
        });
    }

    public ArtifactModelManager getArtifactModelManager() {
        return artifactModelManager;
    }

    @Override
    public void attach() {
        super.attach();
        updateTree();
    }
}
