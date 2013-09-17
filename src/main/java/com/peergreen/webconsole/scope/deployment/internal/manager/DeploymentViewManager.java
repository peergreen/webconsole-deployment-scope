/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.manager;

import com.peergreen.deployment.Artifact;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;

import java.net.URI;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
public interface DeploymentViewManager {
    void addToDeployable(URI uri);

    void addToDeployed(URI uri);

    void addToDeploymentPlan(URI uri);

    void deploy(Artifact artifact);

    void undeploy(Artifact artifact);

    void update(Artifact artifact);

    void delete(DeployableEntry deployableEntry);

    void download(List<DeployableEntry> deployableEntries);

    void showDeploymentPlanView();

    void showDeployedView();

    DeployableEntry getDeployableEntry(URI uri);
}
