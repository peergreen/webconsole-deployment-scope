/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
