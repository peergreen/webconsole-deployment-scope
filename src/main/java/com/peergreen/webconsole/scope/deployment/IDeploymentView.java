package com.peergreen.webconsole.scope.deployment;

import com.peergreen.webconsole.scope.deployment.components.DeployableEntry;
import com.vaadin.ui.Button;

import java.util.List;

/**
 * @author Mohammed Boukada
 */
public interface IDeploymentView {
    void addDeployable(String filename);
    void addToDeployed(String filename);
    void deploy(List<DeployableEntry> deployableEntries);
    void deploy(DeployableEntry deployableEntry);
    void undeploy(List<DeployableEntry> deployableEntries);
    void undeploy(DeployableEntry deployableEntry);
    void update(List<DeployableEntry> deployableEntries);
    void update(DeployableEntry deployableEntry);
    void delete(List<DeployableEntry> deployableEntries);
    void delete(DeployableEntry deployableEntry);
    void download(List<DeployableEntry> deployableEntries, Button download);
}
