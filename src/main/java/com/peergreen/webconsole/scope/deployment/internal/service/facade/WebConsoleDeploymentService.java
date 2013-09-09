package com.peergreen.webconsole.scope.deployment.internal.service.facade;

import com.peergreen.deployment.ArtifactProcessRequest;
import com.peergreen.deployment.DeploymentService;
import com.peergreen.deployment.report.ArtifactStatusReport;
import com.peergreen.deployment.report.ArtifactStatusReportException;
import com.peergreen.deployment.report.DeploymentStatusReport;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides
public class WebConsoleDeploymentService implements DeploymentManager {

    @Requires
    private DeploymentService deploymentService;

    @Override
    public DeploymentStatusReport process(Collection<ArtifactProcessRequest> artifactProcessRequests) {
        return deploymentService.process(artifactProcessRequests);
    }

    @Override
    public ArtifactStatusReport getReport(String s) throws ArtifactStatusReportException {
        return deploymentService.getReport(s);
    }
}
