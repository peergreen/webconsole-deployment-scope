/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.service.tracker;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;

import com.peergreen.deployment.Artifact;
import com.peergreen.deployment.DeploymentMode;
import com.peergreen.deployment.model.ArtifactModel;
import com.peergreen.deployment.report.ArtifactStatusReport;
import com.peergreen.deployment.tracker.DeploymentServiceTracker;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;

/**
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides
public class DeploymentTracker implements DeploymentServiceTracker {

    private final List<DeploymentViewManager> deploymentViewManagers = new CopyOnWriteArrayList<>();

    @Override
    public void beforeProcessing(Artifact artifact, DeploymentMode deploymentMode) {
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

    @Override
    public void afterProcessing(ArtifactModel artifactModel, DeploymentMode deploymentMode,
            ArtifactStatusReport artifactStatusReport) {

    }
}
