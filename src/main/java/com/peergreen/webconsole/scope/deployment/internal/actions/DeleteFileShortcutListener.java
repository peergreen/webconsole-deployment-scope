/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.actions;

import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Table;

import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
public class DeleteFileShortcutListener extends ShortcutListener {

    private DeploymentViewManager deploymentViewManager;
    private Table source;

    public DeleteFileShortcutListener(DeploymentViewManager deploymentViewManager, Table source, String caption, int keyCode, int... modifierKeys) {
        super(caption, keyCode, modifierKeys);
        this.deploymentViewManager = deploymentViewManager;
        this.source = source;
    }

    @Override
    public void handleAction(Object sender, Object target) {
        Table table = (Table) target;
        if (source.equals(table)) {
            Collection<DeployableEntry> deployableEntries = (Collection<DeployableEntry>) table.getValue();
            for (DeployableEntry deployableEntry : deployableEntries) {
                deploymentViewManager.delete(deployableEntry);
            }
        }
    }
}
