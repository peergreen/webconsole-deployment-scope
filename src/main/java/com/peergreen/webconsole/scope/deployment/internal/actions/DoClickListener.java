package com.peergreen.webconsole.scope.deployment.internal.actions;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.webconsole.scope.deployment.internal.DeploymentActions;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableEntry;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.NativeSelect;

import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
public class DoClickListener implements Button.ClickListener {

    private ArtifactBuilder artifactBuilder;
    private AbstractSelect container;
    private NativeSelect actionSelection;
    private DeploymentViewManager deploymentViewService;

    public DoClickListener(ArtifactBuilder artifactBuilder, AbstractSelect container, NativeSelect actionSelection, DeploymentViewManager deploymentViewService) {
        this.artifactBuilder = artifactBuilder;
        this.container = container;
        this.actionSelection = actionSelection;
        this.deploymentViewService = deploymentViewService;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        Collection<DeployableEntry> selectedItems = (Collection<DeployableEntry>) container.getValue();
        for (DeployableEntry deployableEntry : selectedItems) {
            doAction((String) actionSelection.getValue(), deployableEntry);
        }
    }

    private void doAction(String action, DeployableEntry deployableEntry) {
        if (action != null && !action.equals("")) {
            switch (action) {
                case DeploymentActions.DEPLOY:
                    deploymentViewService.deploy(artifactBuilder.build(deployableEntry.getName(), deployableEntry.getUri()));
                    break;
                case DeploymentActions.UPDATE:
                    deploymentViewService.update(artifactBuilder.build(deployableEntry.getName(), deployableEntry.getUri()));
                    break;
                case DeploymentActions.UNDEPLOY:
                    deploymentViewService.undeploy(artifactBuilder.build(deployableEntry.getName(), deployableEntry.getUri()));
                    break;
                case DeploymentActions.DELETE:
                    deploymentViewService.delete(deployableEntry);
                    break;
                case DeploymentActions.DEPLOYMENT_PLAN:
                    deploymentViewService.addToDeploymentPlan(deployableEntry.getUri());
                    break;
            }
        }
    }
}
