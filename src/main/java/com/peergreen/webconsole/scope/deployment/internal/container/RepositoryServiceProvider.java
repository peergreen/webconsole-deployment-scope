package com.peergreen.webconsole.scope.deployment.internal.container;

import com.peergreen.deployment.repository.RepositoryService;

/**
 * @author Mohammed Boukada
 */
public interface RepositoryServiceProvider {

    RepositoryService getRepositoryService();

}
