package com.peergreen.webconsole.scope.deployment.internal.service.tracker;

import com.peergreen.deployment.Artifact;
import com.peergreen.deployment.DeploymentMode;
import com.peergreen.deployment.tracker.DeploymentServiceTracker;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides
public class DeploymentTracker implements DeploymentServiceTracker {

    private List<DeploymentViewManager> deploymentViewManagers = new CopyOnWriteArrayList<>();

    @Override
    public void onChange(Artifact artifact, DeploymentMode deploymentMode) {
        switch (deploymentMode) {
            case DEPLOY:
                for (DeploymentViewManager deploymentViewManager : deploymentViewManagers) {
                    deploymentViewManager.addToDeployed(artifact.uri());
                }
                break;
            case UNDEPLOY:
                for (DeploymentViewManager deploymentViewManager : deploymentViewManagers) {
                    deploymentViewManager.addToDeployable(artifact.uri());
                }
                break;
        }
    }

    @Bind(aggregate = true, optional = true)
    public void bindDeploymentViewManager(DeploymentViewManager deploymentViewManager) {
        deploymentViewManagers.add(deploymentViewManager);
    }

    @Unbind
    public void unbindDeploymentViewManager(DeploymentViewManager deploymentViewManager) {
        deploymentViewManagers.remove(deploymentViewManager);
    }
}
