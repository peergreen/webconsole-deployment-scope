/**
 * Peergreen S.A.S. All rights reserved.
 * Proprietary and confidential.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.deployed;

import javax.annotation.PostConstruct;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import com.peergreen.deployment.report.ArtifactStatusReport;
import com.peergreen.deployment.report.ArtifactStatusReportException;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.scope.deployment.internal.components.DeployableWindow;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainerType;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.ItemDescription;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.ItemStyle;
import com.peergreen.webconsole.scope.deployment.internal.service.facade.DeploymentManager;
import com.peergreen.webconsole.scope.home.Frame;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.home.HomeScope.top.right")
@Frame("Deployed artifacts")
public class DeployedHomeFrame extends Table {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(DeployedHomeFrame.class);

    @Inject
    DeployedPanel deployedPanel;
    @Inject
    private DeploymentManager deploymentManager;

    public DeployedHomeFrame() {
        setSizeFull();
        setImmediate(true);
        setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
    }

    @PostConstruct
    public void init() {
        setContainerDataSource(deployedPanel.getContainer());
        setItemCaptionPropertyId(DeployedPanel.TREE_ITEM_ID);
        setCellStyleGenerator(new ItemStyle(DeployableContainerType.DEPLOYED, deploymentManager));
        setItemDescriptionGenerator(new ItemDescription());
        addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    DeployableEntry deployableEntry = (DeployableEntry) event.getItemId();
                    try {
                        ArtifactStatusReport report = deploymentManager.getReport(deployableEntry.getUri().toString());
                        event.getComponent().getUI().addWindow(new DeployableWindow(deployableEntry, report).getWindow());
                    } catch (ArtifactStatusReportException e) {
                        LOGGER.warn("Cannot get artifact status report for ''{0}''", deployableEntry.getUri(), e);
                        event.getComponent().getUI().addWindow(new DeployableWindow(deployableEntry).getWindow());
                    }
                }
            }
        });
        deployedPanel.getView().addAttachListener(new DeployedPanelAttachListener(this));
    }


    private class DeployedPanelAttachListener implements AttachListener {

        private Table table;

        private DeployedPanelAttachListener(Table table) {
            this.table = table;
        }

        @Override
        public void attach(AttachEvent attachEvent) {
            table.attach();
        }
    }
}
