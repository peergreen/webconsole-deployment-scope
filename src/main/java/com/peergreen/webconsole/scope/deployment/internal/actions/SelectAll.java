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
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;

import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
public class SelectAll implements Property.ValueChangeListener {

    private AbstractSelect container;

    public SelectAll(AbstractSelect container) {
        this.container = container;
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Boolean state = (Boolean) event.getProperty().getValue();
        Collection<DeployableEntry> deployableEntries = (Collection<DeployableEntry>) container.getItemIds();
        if (state) {
            for (DeployableEntry deployableEntry : deployableEntries) {
                container.select(deployableEntry);
            }
        } else {
            for (DeployableEntry deployableEntry : deployableEntries) {
                container.unselect(deployableEntry);
            }
        }
    }
}
