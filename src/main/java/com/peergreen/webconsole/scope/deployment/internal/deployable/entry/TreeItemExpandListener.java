package com.peergreen.webconsole.scope.deployment.internal.deployable.entry;

import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.deployment.repository.maven.MavenArtifactInfo;
import com.peergreen.deployment.repository.search.Queries;
import com.peergreen.deployment.repository.search.Query;
import com.peergreen.webconsole.scope.deployment.internal.deployable.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher.MavenDeployableFetcher;
import com.vaadin.ui.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
public class TreeItemExpandListener implements Tree.ExpandListener {

    private AbstractDeployableContainer deployableContainer;
    private MavenRepositoryService mavenRepositoryService;

    public TreeItemExpandListener(AbstractDeployableContainer deployableContainer, MavenRepositoryService mavenRepositoryService) {
        this.deployableContainer = deployableContainer;
        this.mavenRepositoryService = mavenRepositoryService;
    }

    @Override
    public void nodeExpand(Tree.ExpandEvent event) {
        DeployableEntry parent = (DeployableEntry) event.getItemId();
        if (DeployableSource.MAVEN.equals(parent.getSource())) {
            MavenDeployableEntry mavenDeployableEntry = (MavenDeployableEntry) parent;
            MavenArtifactInfo mavenArtifactInfo = mavenDeployableEntry.getArtifactInfo();
            List<Query> queries = new ArrayList<>();
            queries.add(Queries.from(mavenArtifactInfo.repository));
            if (mavenArtifactInfo.groupId != null) {
                queries.add(Queries.groupId(mavenArtifactInfo.groupId));
            }
            if (mavenArtifactInfo.artifactId != null) {
                queries.add(Queries.artifactId(mavenArtifactInfo.artifactId));
            }
            if (mavenArtifactInfo.version != null) {
                queries.add(Queries.version(mavenArtifactInfo.version));
            }
            MavenDeployableFetcher fetcher = new MavenDeployableFetcher(deployableContainer, mavenRepositoryService, queries.toArray(new Query[queries.size()]));
            fetcher.setStopNode(parent);
            fetcher.start();
        }
    }
}
