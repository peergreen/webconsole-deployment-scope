package com.peergreen.webconsole.scope.deployment.internal.manager;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;

import com.peergreen.deployment.Artifact;
import com.peergreen.deployment.DeploymentMode;
import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableSource;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.MavenDeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.service.Deployer;
import com.peergreen.webconsole.vaadin.ConfirmDialog;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * @author Mohammed Boukada
 */
@Component
@Provides(properties = @StaticServiceProperty(name = Constants.UI_ID, type = "java.lang.String", mandatory = true))
public class BaseDeploymentViewManager implements DeploymentViewManager {

    private HorizontalLayout framesContainer;
    private Button deploymentPlanDraftViewer;
    private DeployableContainer deployableContainer;
    private DeployableContainer deployedContainer;
    private DeployableContainer deploymentPlanContainer;

    @Requires
    private Deployer deployer;
    @Requires
    private INotifierService notifierService;

    public BaseDeploymentViewManager(HorizontalLayout framesContainer) {
        this.framesContainer = framesContainer;
    }

    public void setDeployableContainer(DeployableContainer deployableContainer) {
        this.deployableContainer = deployableContainer;
    }

    public void setDeployedContainer(DeployableContainer deployedContainer) {
        this.deployedContainer = deployedContainer;
    }

    public void setDeploymentPlanContainer(DeployableContainer deploymentPlanContainer) {
        this.deploymentPlanContainer = deploymentPlanContainer;
    }

    public void setDeploymentPlanDraftViewer(Button deploymentPlanDraftViewer) {
        this.deploymentPlanDraftViewer = deploymentPlanDraftViewer;
    }

    @Override
    public void addToDeployable(URI uri) {
        if (deployableContainer == null) {
            notifierService.addNotification(String.format("Cannot add '%s' to deployables", uri));
            return;
        }

        DeployableEntry deployableEntry = deployedContainer.getDeployable(uri);
        if (deployableEntry == null) {
            deployableEntry = new DeployableEntry(uri, DeployableSource.FILE);
        } else if (DeployableSource.FILE.equals(deployableEntry.getSource())) {
            // may be the artifact has been deleted ?
            File file = new File(uri);
            if (!file.exists()) {
                deleteDeployable(deployableEntry);
                return;
            }
        }
        addToDeployable(deployableEntry);
    }

    @Override
    public void addToDeployed(URI uri) {
        if (deployedContainer == null) {
            notifierService.addNotification(String.format("Cannot add '%s' to deployed", uri));
            return;
        }

        DeployableEntry deployableEntry = deployableContainer.getDeployable(uri);
        if (deployableEntry == null) {
            deployableEntry = new DeployableEntry(uri, DeployableSource.FILE);
        }
        addToDeployed(deployableEntry);
    }

    @Override
    public void addToDeploymentPlan(URI uri) {
        if (deploymentPlanContainer == null) {
            notifierService.addNotification(String.format("Cannot add '%s' to deployment plan", uri));
            return;
        }

        DeployableEntry deployableEntry = deployableContainer.getDeployable(uri);
        if (deployableEntry == null) {
            deployableEntry = new DeployableEntry(uri, DeployableSource.FILE);
        }
        deploymentPlanContainer.addDeployable(deployableEntry);
        showDeploymentPlanView();
    }

    @Override
    public void deploy(Artifact artifact) {
        deployer.process(artifact, DeploymentMode.DEPLOY);
    }

    @Override
    public void undeploy(Artifact artifact) {
        deployer.process(artifact, DeploymentMode.UNDEPLOY);
    }

    private void addToDeployable(DeployableEntry deployableEntry) {
        if (deployableContainer == null) {
            notifierService.addNotification(String.format("Cannot undeploy '%s'", deployableEntry.getName()));
            return;
        }
        DeployableContainer sourceContainer = deployableEntry.getContainer();
        if (sourceContainer != null) {
            sourceContainer.removeDeployable(deployableEntry);
        }
        deployableContainer.addDeployable(deployableEntry);
    }

    private void addToDeployed(DeployableEntry deployableEntry) {
        if (deployedContainer == null) {
            notifierService.addNotification(String.format("Cannot deploy '%s'", deployableEntry.getName()));
            return;
        }
        DeployableContainer sourceContainer = deployableEntry.getContainer();
        if (sourceContainer != null) {
            sourceContainer.removeDeployable(deployableEntry);
        }
        deployedContainer.addDeployable(deployableEntry);
    }

    @Override
    public void update(Artifact artifact) {
        deployer.process(artifact, DeploymentMode.UPDATE);
    }

    @Override
    public void delete(final DeployableEntry deployableEntry) {
        final File file = new File(deployableEntry.getUri());
        if (file.exists()) {
            String message = String.format("Would you really want to delete<br /> <b>%s</b> ?", file.getName());
            ConfirmDialog.show(framesContainer.getUI(), new Label(message, ContentMode.HTML), new ConfirmDialog.Listener() {
                @Override
                public void onClose(boolean isConfirmed) {
                    if (isConfirmed && file.delete()) {
                        deleteDeployable(deployableEntry);
                    }
                }
            });
        }
    }

    private void deleteDeployable(DeployableEntry deployableEntry) {
        if (deployableEntry.getContainer() != null) {
            deployableEntry.getContainer().removeDeployable(deployableEntry);
        }
        notifierService.addNotification(String.format("'%s' was deleted.", deployableEntry.getName()));
    }

    @Override
    public void download(List<DeployableEntry> deployableEntries) {

    }

    @Override
    public void showDeploymentPlanView() {
        if (framesContainer.getComponentIndex(deploymentPlanContainer.getView()) == -1) {
            framesContainer.removeComponent(deployedContainer.getView());
            framesContainer.addComponent(deploymentPlanContainer.getView());
        }
    }

    @Override
    public void showDeployedView() {
        if (framesContainer.getComponentIndex(deployedContainer.getView()) == -1) {
            framesContainer.removeComponent(deploymentPlanContainer.getView());
            framesContainer.addComponent(deployedContainer.getView());

            if (deploymentPlanContainer.getContainer().getItemIds().size() > 0) {
                deploymentPlanDraftViewer.setVisible(true);
            } else {
                deploymentPlanDraftViewer.setVisible(false);
            }
        }
    }

    @Override
    public DeployableEntry getDeployableEntry(URI uri) {
        DeployableEntry deployableEntry = deployedContainer == null ? null : deployableContainer.getDeployable(uri);
        if (deployableEntry == null) {
            deployableEntry = deployedContainer == null ? null : deployedContainer.getDeployable(uri);
            if (deployableEntry == null) {
                DeployableSource deployableSource = getDeployableSource(uri);
                if (DeployableSource.MAVEN.equals(deployableSource)) {
                    deployableEntry = new MavenDeployableEntry(uri, deployableSource, null);
                } else {
                    deployableEntry = new DeployableEntry(uri, deployableSource);
                }
            }
        }
        return deployableEntry;
    }

    private DeployableSource getDeployableSource(URI uri) {
        if (uri.toString().startsWith("mvn:")) {
            return DeployableSource.MAVEN;
        }
        return DeployableSource.FILE;
    }
}
