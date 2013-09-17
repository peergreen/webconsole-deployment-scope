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

import com.peergreen.webconsole.scope.deployment.internal.components.DeployableWindow;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.TreeTable;

/**
 * @author Mohammed Boukada
 */
public class TreeItemClickListener implements ItemClickEvent.ItemClickListener {
    @Override
    public void itemClick(ItemClickEvent event) {
        DeployableEntry item = (DeployableEntry) event.getItemId();
        if (!item.isDeployable()) {
            TreeTable treeTable = (TreeTable) event.getSource();
            treeTable.setCollapsed(event.getItemId(), !treeTable.isCollapsed(event.getItemId()));
        } else if (event.isDoubleClick()) {
            event.getComponent().getUI().addWindow(new DeployableWindow(item).getWindow());
        }
    }
}
