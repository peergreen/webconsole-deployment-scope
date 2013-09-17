/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.service;

import com.peergreen.deployment.Artifact;
import com.peergreen.deployment.DeploymentMode;

/**
 * @author Mohammed Boukada
 */
public interface Deployer {
    void process(Artifact artifact, DeploymentMode deploymentMode);
}
