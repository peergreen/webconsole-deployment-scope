/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.container;

import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Component;

import java.net.URI;

/**
 * @author Mohammed Boukada
 */
public interface DeployableContainer {

    void receive(URI uri);

    void receive(DeployableEntry deployableEntry);

    void addDeployable(DeployableEntry deployableEntry);

    void removeDeployable(DeployableEntry deployableEntry);

    DeployableEntry getDeployable(URI uri);

    Component getView();

    HierarchicalContainer getContainer();
}
