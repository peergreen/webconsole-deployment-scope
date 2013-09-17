/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher;

import java.net.URI;

import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.maven.MavenNode;
import com.peergreen.webconsole.scope.deployment.internal.container.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableSource;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.MavenDeployableEntry;
import com.vaadin.data.util.HierarchicalContainer;

/**
 * @author Mohammed Boukada
 */
public abstract class DeployableFetcher extends Thread {

    private ArtifactModelManager artifactModelManager;
    private AbstractDeployableContainer deployableContainer;
    private HierarchicalContainer container;
    private DeployableEntry parent;
    private URI uri;
    private boolean fetching;

    protected DeployableFetcher(AbstractDeployableContainer deployableContainer,
                                HierarchicalContainer container,
                                ArtifactModelManager artifactModelManager) {
        this.deployableContainer = deployableContainer;
        this.container = container;
        this.artifactModelManager = artifactModelManager;
    }

    protected void buildNode(Node<?> node, DeployableEntry parent) {
        DeployableEntry deployableEntry = addItemToTree(node, parent);
        if (!node.getChildren().isEmpty() && deployableEntry != null && parent != null) {
            for (Node<?> child : node.getChildren()) {
                buildNode(child, deployableEntry);
            }
        }
    }

    private DeployableEntry addItemToTree(Node<?> node, DeployableEntry parent) {
        BaseNode data = (BaseNode) node.getData();
        if (artifactModelManager.getDeployedRootURIs().contains(data.getUri())) {
            return null;
        }

        DeployableEntry deployableEntry = deployableContainer.getDeployable(data.getUri());
        if (deployableEntry != null) {
            if (container.containsId(deployableEntry)) {
                return deployableEntry;
            }
        } else {
            if (data instanceof MavenNode) {
                deployableEntry = new MavenDeployableEntry(data.getUri(), data.getName(), DeployableSource.MAVEN, deployableContainer, parent, ((MavenNode) data).getArtifactInfo());
            } else {
                deployableEntry = new DeployableEntry(data.getUri(), data.getName(), DeployableSource.FILE, deployableContainer, parent);
            }
            deployableEntry.setDeployable(data.isLeaf());
        }
        deployableContainer.addItemToContainer(deployableEntry, deployableContainer.getContainerProperties(deployableEntry), false);

        return deployableEntry;
    }

    public void setParent(DeployableEntry parent) {
        this.parent = parent;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public AbstractDeployableContainer getDeployableContainer() {
        return deployableContainer;
    }

    public DeployableEntry getParent() {
        return parent;
    }

    public URI getUri() {
        return uri;
    }

    public boolean isFetching() {
        return fetching;
    }

    public void setFetching(boolean fetching) {
        this.fetching = fetching;
    }
}
