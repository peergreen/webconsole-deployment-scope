package com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher;

import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.maven.MavenNode;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableSource;
import com.peergreen.webconsole.scope.deployment.internal.deployable.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.MavenDeployableEntry;
import com.vaadin.data.util.HierarchicalContainer;

import java.net.URI;

/**
 * @author Mohammed Boukada
 */
public abstract class DeployableFetcher extends Thread {

    protected AbstractDeployableContainer deployableContainer;
    protected HierarchicalContainer container;
    protected ArtifactModelManager artifactModelManager;
    protected DeployableEntry parent;
    protected URI uri;

    protected DeployableFetcher(AbstractDeployableContainer deployableContainer,
                                HierarchicalContainer container,
                                ArtifactModelManager artifactModelManager) {
        this.deployableContainer = deployableContainer;
        this.container = container;
        this.artifactModelManager = artifactModelManager;
    }

    protected void buildNode(Node<?> node, DeployableEntry parent) {
        DeployableEntry deployableEntry = addItemToTree(node, parent);
        if (!node.getChildren().isEmpty() && deployableEntry != null && parent != null) {
            for (Node<?> child : node.getChildren()) {
                buildNode(child, deployableEntry);
            }
        }
    }

    private DeployableEntry addItemToTree(Node<?> node, DeployableEntry parent) {
        BaseNode data = (BaseNode) node.getData();
        if (artifactModelManager.getDeployedRootURIs().contains(data.getUri())) {
            return null;
        }

        DeployableEntry deployableEntry = deployableContainer.getDeployable(data.getUri());
        if ( deployableEntry != null) {
            if (container.containsId(deployableEntry)) {
                return deployableEntry;
            }
        } else {
            if (data instanceof MavenNode) {
                deployableEntry = new MavenDeployableEntry(data.getUri(), data.getName(), DeployableSource.MAVEN, deployableContainer, parent, ((MavenNode) data).getArtifactInfo());
            } else {
                deployableEntry = new DeployableEntry(data.getUri(), data.getName(), DeployableSource.FILE, deployableContainer, parent);
            }
            deployableEntry.setDeployable(data.isLeaf());
        }
        deployableContainer.addItemToContainer(deployableEntry, deployableContainer.getContainerProperties(deployableEntry), false);

        return deployableEntry;
    }

    public void setParent(DeployableEntry parent) {
        this.parent = parent;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
