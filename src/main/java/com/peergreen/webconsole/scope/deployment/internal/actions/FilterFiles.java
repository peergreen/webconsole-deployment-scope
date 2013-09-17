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

import com.vaadin.data.Container;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents;

/**
 * @author Mohammed Boukada
 */
public class FilterFiles implements FieldEvents.TextChangeListener {

    private Container.Filterable[] containers;
    private String itemId;
    private Container.Filter filter;

    public FilterFiles(String itemId, Container.Filterable... containers) {
        this.containers = containers;
        this.itemId = itemId;
    }

    @Override
    public void textChange(FieldEvents.TextChangeEvent event) {
        for (Container.Filterable container : containers) {
            if (filter != null) {
                container.removeContainerFilter(filter);
            }
            filter = new SimpleStringFilter(itemId, event.getText().trim(), true, false);
            container.addContainerFilter(filter);
        }
    }

}
