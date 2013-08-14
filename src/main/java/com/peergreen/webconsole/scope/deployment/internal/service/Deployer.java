package com.peergreen.webconsole.scope.deployment.internal.service;

import com.peergreen.deployment.Artifact;
import com.peergreen.deployment.DeploymentMode;

/**
 * @author Mohammed Boukada
 */
public interface Deployer {
    void process(Artifact artifact, DeploymentMode deploymentMode);
}
