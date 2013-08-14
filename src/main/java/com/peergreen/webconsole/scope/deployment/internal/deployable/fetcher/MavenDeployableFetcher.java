package com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher;

import com.peergreen.deployment.repository.Graph;
import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.maven.MavenNode;
import com.peergreen.deployment.repository.search.Query;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.scope.deployment.internal.deployable.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableEntry;

/**
 * @author Mohammed Boukada
 */
public class MavenDeployableFetcher extends DeployableFetcher {

    private MavenRepositoryService mavenRepositoryService;
    private INotifierService notifierService;
    private Query[] queries;
    private DeployableEntry stopNode;

    public MavenDeployableFetcher(AbstractDeployableContainer deployableContainer,
                                  MavenRepositoryService mavenRepositoryService,
                                  Query... queries) {
        super(deployableContainer, deployableContainer.getContainer(), deployableContainer.getArtifactModelManager());
        this.notifierService = deployableContainer.getNotifierService();
        this.mavenRepositoryService = mavenRepositoryService;
        this.queries = queries;
    }

    @Override
    public void run() {
        deployableContainer.startFetching();
        notifierService.startTask(this, "Fetching maven deployables ...", (long) 1);
        updateTree();
        notifierService.stopTask(this);
        deployableContainer.stopFetching();
    }

    protected void updateTree() {
        Graph<MavenNode> graph = mavenRepositoryService.list(queries);
        for (Node<MavenNode> node : graph.getNodes()) {
            buildNode(node, null, stopNode, false);
        }
    }

    public void setStopNode(DeployableEntry stopNode) {
        this.stopNode = stopNode;
    }
}
