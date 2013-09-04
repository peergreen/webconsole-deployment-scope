package com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher;

import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.maven.MavenArtifactInfo;
import com.peergreen.deployment.repository.maven.MavenNode;
import com.peergreen.webconsole.scope.deployment.internal.container.AbstractDeployableContainer;

import java.util.List;

/**
 * @author Mohammed Boukada
 */
public class MavenDeployableFetcher extends DeployableFetcher {

    private MavenRepositoryService mavenRepositoryService;
    private MavenArtifactInfo.Type type;

    public MavenDeployableFetcher(AbstractDeployableContainer deployableContainer,
                                  MavenRepositoryService mavenRepositoryService) {
        super(deployableContainer, deployableContainer.getContainer(), deployableContainer.getArtifactModelManager());
        this.mavenRepositoryService = mavenRepositoryService;
    }


    public void setType(MavenArtifactInfo.Type type) {
        this.type = type;
    }

    @Override
    public void run() {
        getDeployableContainer().startFetching("Fetching maven repositories...");
        updateTree();
        getDeployableContainer().stopFetching();
    }

    protected void updateTree() {
        List<Node<MavenNode>> nodes = mavenRepositoryService.getChildren(getUri(), type);
        if (nodes != null) {
            for (Node<MavenNode> node : nodes) {
                buildNode(node, getParent());
            }
        }
    }
}
