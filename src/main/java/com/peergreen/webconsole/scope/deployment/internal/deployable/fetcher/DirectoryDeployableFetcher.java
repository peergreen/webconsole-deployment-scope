package com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher;

import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.Graph;
import com.peergreen.deployment.repository.Node;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.scope.deployment.internal.deployable.AbstractDeployableContainer;

/**
 * @author Mohammed Boukada
 */
public class DirectoryDeployableFetcher extends DeployableFetcher {

    private DirectoryRepositoryService directoryRepositoryService;
    private INotifierService notifierService;

    public DirectoryDeployableFetcher(AbstractDeployableContainer deployableContainer,
                                      DirectoryRepositoryService directoryRepositoryService) {
        super(deployableContainer, deployableContainer.getContainer(), deployableContainer.getArtifactModelManager());
        this.notifierService = deployableContainer.getNotifierService();
        this.directoryRepositoryService = directoryRepositoryService;
    }

    @Override
    public void run() {
        deployableContainer.startFetching();
        notifierService.startTask(this, "Fetching directory deployables ...", (long) 1);
        updateTree();
        notifierService.stopTask(this);
        deployableContainer.stopFetching();
    }

    protected void updateTree() {
        Graph<BaseNode> graph = directoryRepositoryService.list("");
        for (Node<BaseNode> node : graph.getNodes()) {
            buildNode(node, null, null, true);
        }
    }
}
