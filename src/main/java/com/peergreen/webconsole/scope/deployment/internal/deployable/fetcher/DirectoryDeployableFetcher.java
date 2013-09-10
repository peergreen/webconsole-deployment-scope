package com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher;

import java.util.List;

import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.Node;
import com.peergreen.webconsole.scope.deployment.internal.container.AbstractDeployableContainer;

/**
 * @author Mohammed Boukada
 */
public class DirectoryDeployableFetcher extends DeployableFetcher {

    private DirectoryRepositoryService directoryRepositoryService;

    public DirectoryDeployableFetcher(AbstractDeployableContainer deployableContainer,
                                      DirectoryRepositoryService directoryRepositoryService) {
        super(deployableContainer, deployableContainer.getContainer(), deployableContainer.getArtifactModelManager());
        this.directoryRepositoryService = directoryRepositoryService;
    }

    @Override
    public void run() {
        getDeployableContainer().startFetching("Fetching directories...");
        setFetching(true);
        updateTree();
        setFetching(false);
        getDeployableContainer().stopFetching();
    }

    protected void updateTree() {
        List<Node<BaseNode>> nodes = directoryRepositoryService.getChildren(getUri());
        if (nodes != null) {
            for (Node<BaseNode> node : nodes) {
                buildNode(node, getParent());
            }
        }
    }
}
