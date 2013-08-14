package com.peergreen.webconsole.scope.deployment.internal.service.facade;

import com.peergreen.deployment.ArtifactProcessRequest;
import com.peergreen.deployment.report.ArtifactStatusReport;
import com.peergreen.deployment.report.ArtifactStatusReportException;
import com.peergreen.deployment.report.DeploymentStatusReport;

import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
public interface DeploymentManager {
    DeploymentStatusReport process(Collection<ArtifactProcessRequest> artifactProcessRequests);

    ArtifactStatusReport getReport(String s) throws ArtifactStatusReportException;
}
