package com.peergreen.webconsole.scope.deployment.internal.deployable.entry;

import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.deployment.repository.maven.MavenArtifactInfo;
import com.peergreen.webconsole.scope.deployment.internal.deployable.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher.DirectoryDeployableFetcher;
import com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher.MavenDeployableFetcher;
import com.vaadin.ui.Tree;

/**
 * @author Mohammed Boukada
 */
public class TreeItemExpandListener implements Tree.ExpandListener {

    private AbstractDeployableContainer deployableContainer;
    private MavenRepositoryService mavenRepositoryService;
    private DirectoryRepositoryService directoryRepositoryService;

    public TreeItemExpandListener(AbstractDeployableContainer deployableContainer, MavenRepositoryService mavenRepositoryService) {
        this.deployableContainer = deployableContainer;
        this.mavenRepositoryService = mavenRepositoryService;
    }

    public TreeItemExpandListener(AbstractDeployableContainer deployableContainer, DirectoryRepositoryService directoryRepositoryService) {
        this.deployableContainer = deployableContainer;
        this.directoryRepositoryService = directoryRepositoryService;
    }

    @Override
    public void nodeExpand(Tree.ExpandEvent event) {
        DeployableEntry parent = (DeployableEntry) event.getItemId();
        if (DeployableSource.MAVEN.equals(parent.getSource())) {
            MavenDeployableEntry mavenDeployableEntry = (MavenDeployableEntry) parent;
            MavenArtifactInfo mavenArtifactInfo = mavenDeployableEntry.getArtifactInfo();
            MavenDeployableFetcher fetcher = new MavenDeployableFetcher(deployableContainer, mavenRepositoryService);
            fetcher.setUri(parent.getUri());
            fetcher.setType(mavenArtifactInfo.type);
            fetcher.setParent(parent);
            fetcher.start();
        } else if (DeployableSource.FILE.equals(parent.getSource())) {
            DirectoryDeployableFetcher fetcher = new DirectoryDeployableFetcher(deployableContainer, directoryRepositoryService);
            fetcher.setUri(parent.getUri());
            fetcher.setParent(parent);
            fetcher.start();
        }
    }
}
