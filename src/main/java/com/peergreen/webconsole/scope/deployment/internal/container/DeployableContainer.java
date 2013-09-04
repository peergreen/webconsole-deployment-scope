package com.peergreen.webconsole.scope.deployment.internal.container;

import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Component;

import java.net.URI;

/**
 * @author Mohammed Boukada
 */
public interface DeployableContainer {

    void receive(URI uri);
    void receive(DeployableEntry deployableEntry);

    void addDeployable(DeployableEntry deployableEntry);
    void removeDeployable(DeployableEntry deployableEntry);

    DeployableEntry getDeployable(URI uri);

    Component getView();
    HierarchicalContainer getContainer();
}
