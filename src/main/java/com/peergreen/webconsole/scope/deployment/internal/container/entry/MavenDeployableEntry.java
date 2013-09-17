/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
