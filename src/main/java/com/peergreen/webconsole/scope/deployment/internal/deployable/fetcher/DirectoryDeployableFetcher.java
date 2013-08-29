package com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher;

import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.Node;
import com.peergreen.webconsole.scope.deployment.internal.deployable.AbstractDeployableContainer;

import java.util.List;

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
        deployableContainer.startFetching("Fetching directories...");
        updateTree();
        deployableContainer.stopFetching();
    }

    protected void updateTree() {
        List<Node<BaseNode>> nodes = directoryRepositoryService.getChildren(uri);
        if (nodes != null) {
            for (Node<BaseNode> node : nodes) {
                buildNode(node, parent);
            }
        }
    }
}
