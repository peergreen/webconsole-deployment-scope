package com.peergreen.webconsole.scope.deployment.internal.dd;

import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;

/**
 * @author Mohammed Boukada
 */
public class DeploymentPlanMakerDropHandler implements DropHandler {

    private DropHandler delegate;
    private DeploymentViewManager deploymentViewManager;

    public DeploymentPlanMakerDropHandler(DeploymentViewManager deploymentViewManager, DeployableContainer deployableContainer, INotifierService notifierService) {
        delegate = new DeploymentDropHandler(deploymentViewManager, deployableContainer, notifierService);
        this.deploymentViewManager = deploymentViewManager;
    }

    @Override
    public void drop(DragAndDropEvent event) {
        deploymentViewManager.showDeploymentPlanView();
        delegate.drop(event);
    }

    @Override
    public AcceptCriterion getAcceptCriterion() {
        return AcceptAll.get();
    }
}
