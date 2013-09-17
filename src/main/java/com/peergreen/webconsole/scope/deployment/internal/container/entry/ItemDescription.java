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

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;

/**
 * @author Mohammed Boukada
 */
public class ItemDescription implements AbstractSelect.ItemDescriptionGenerator {
    @Override
    public String generateDescription(Component source, Object itemId, Object propertyId) {
        DeployableEntry deployableEntry = (DeployableEntry) itemId;
        if (deployableEntry.isDeployable()) {
            return "Double click to show more details";
        } else {
            return "";
        }
    }
}
