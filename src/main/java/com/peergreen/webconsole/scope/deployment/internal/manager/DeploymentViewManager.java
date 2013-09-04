package com.peergreen.webconsole.scope.deployment.internal.manager;

import com.peergreen.deployment.Artifact;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;

import java.net.URI;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
public interface DeploymentViewManager {
    void addToDeployable(URI uri);
    void addToDeployed(URI uri);
    void addToDeploymentPlan(URI uri);

    void deploy(Artifact artifact);
    void undeploy(Artifact artifact);
    void update(Artifact artifact);
    void delete(DeployableEntry deployableEntry);
    void download(List<DeployableEntry> deployableEntries);

    void showDeploymentPlanView();
    void showDeployedView();

    DeployableEntry getDeployableEntry(URI uri);
}
