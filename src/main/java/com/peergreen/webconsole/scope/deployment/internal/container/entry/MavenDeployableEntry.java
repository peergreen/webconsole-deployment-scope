package com.peergreen.webconsole.scope.deployment.internal.container.entry;

import com.peergreen.deployment.repository.maven.MavenArtifactInfo;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainer;

import java.net.URI;

/**
 * @author Mohammed Boukada
 */
public class MavenDeployableEntry extends DeployableEntry {

    private MavenArtifactInfo artifactInfo;

    public MavenDeployableEntry(URI uri, DeployableSource source, MavenArtifactInfo artifactInfo) {
        super(uri, source);
        this.artifactInfo = artifactInfo;
    }

    public MavenDeployableEntry(URI uri, String name, DeployableSource source, DeployableContainer container, DeployableEntry parent, MavenArtifactInfo artifactInfo) {
        super(uri, name, source, container, parent);
        this.artifactInfo = artifactInfo;
    }

    public MavenArtifactInfo getArtifactInfo() {
        return artifactInfo;
    }
}
