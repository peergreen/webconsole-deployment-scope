package com.peergreen.webconsole.scope.deployment.internal.container.entry;

import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.deployment.repository.maven.MavenArtifactInfo;
import com.peergreen.webconsole.scope.deployment.internal.container.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher.DeployableFetcher;
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
    private DeployableFetcher currentMavenFetcher;
    private DeployableFetcher currentDirectoryFetcher;

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
        if (DeployableSource.MAVEN.equals(parent.getSource()) && !isFetching(currentMavenFetcher)) {
            MavenDeployableEntry mavenDeployableEntry = (MavenDeployableEntry) parent;
            MavenArtifactInfo mavenArtifactInfo = mavenDeployableEntry.getArtifactInfo();
            MavenDeployableFetcher fetcher = new MavenDeployableFetcher(deployableContainer, mavenRepositoryService);
            fetcher.setUri(parent.getUri());
            fetcher.setType(mavenArtifactInfo.type);
            fetcher.setParent(parent);
            currentMavenFetcher = fetcher;
            fetcher.start();
        } else if (DeployableSource.FILE.equals(parent.getSource()) && !isFetching(currentDirectoryFetcher)) {
            DirectoryDeployableFetcher fetcher = new DirectoryDeployableFetcher(deployableContainer, directoryRepositoryService);
            fetcher.setUri(parent.getUri());
            fetcher.setParent(parent);
            currentDirectoryFetcher = fetcher;
            fetcher.start();
        }
    }

    private boolean isFetching(DeployableFetcher deployableFetcher) {
        return deployableFetcher != null && deployableFetcher.isFetching();
    }
}
