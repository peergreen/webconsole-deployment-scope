package com.peergreen.webconsole.scope.deployment.internal.deployable.repository;

import com.peergreen.webconsole.scope.deployment.internal.container.AbstractDeployableContainer;

/**
 * @author Mohammed Boukada
 */
public interface RepositoryManagerPanel {

    /**
     * Set directory deployables container
     * @param directoryContainer directory container
     */
    void setDirectoryContainer(AbstractDeployableContainer directoryContainer);

    /**
     * Set maven deployables container
     * @param mavenContainer maven container
     */
    void setMavenContainer(AbstractDeployableContainer mavenContainer);
}
