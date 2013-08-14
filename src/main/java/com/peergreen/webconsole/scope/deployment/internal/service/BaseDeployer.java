package com.peergreen.webconsole.scope.deployment.internal.service;

import com.peergreen.deployment.Artifact;
import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.deployment.ArtifactProcessRequest;
import com.peergreen.deployment.DeploymentMode;
import com.peergreen.deployment.report.DeploymentStatusReport;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.scope.deployment.internal.service.facade.DeploymentManager;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.Collections;

/**
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides
public class BaseDeployer implements Deployer {

    private final static String DEPLOYED_STATE = "deployed";
    private final static String UNDEPLOYED_STATE = "undeployed";
    private final static String UPDATE_STATE = "updated";
    private final static String FAILED_STATE = "failed";

    @Requires
    private ArtifactBuilder artifactBuilder;
    @Requires
    private DeploymentManager deploymentManager;
    @Requires
    private INotifierService notifierService;

    @Override
    public void process(Artifact artifact, DeploymentMode deploymentMode) {
        DeployerTask task = new DeployerTask(artifact, deploymentMode);
        task.start();
    }

    public class DeployerTask extends Thread {

        private Artifact artifact;
        private DeploymentMode deploymentMode;

        public DeployerTask(Artifact artifact, DeploymentMode deploymentMode) {
            this.artifact = artifact;
            this.deploymentMode = deploymentMode;
        }

        @Override
        public void run() {
            String state = "";
            switch (deploymentMode) {
                case DEPLOY:
                    state = DEPLOYED_STATE;
                    break;
                case UNDEPLOY:
                    state = UNDEPLOYED_STATE;
                    break;
                case UPDATE:
                    state = UPDATE_STATE;
                    break;
            }

            ArtifactProcessRequest artifactProcessRequest = new ArtifactProcessRequest(artifact);
            artifactProcessRequest.setDeploymentMode(deploymentMode);
            notifierService.addNotification(String.format("'%s' is being %s.", artifact.name(), state));
            DeploymentStatusReport report = deploymentManager.process(Collections.singleton(artifactProcessRequest));
            if (report.hasFailed()) {
                state = FAILED_STATE;
                notifierService.addNotification(String.format("'%s' has %s.", artifact.name(), state));
            } else {

                notifierService.addNotification(String.format("'%s' was %s.", artifact.name(), state));
            }
        }
    }
}