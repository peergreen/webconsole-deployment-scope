/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.deployable.repository;

import com.peergreen.deployment.repository.view.Repository;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Mohammed Boukada
 */
public class RepositoryEntry extends VerticalLayout {

    private Repository repository;
    private HorizontalLayout defaultView;
    private VerticalLayout detailsView;

    public RepositoryEntry(Repository repository) {
        this.repository = repository;
        setStyleName("repository-entry");
        showDefaultView();
    }

    public Repository getRepository() {
        return repository;
    }

    private void showDefaultView() {
        if (defaultView == null) {
            defaultView = new HorizontalLayout();
            defaultView.setWidth("100%");
            Label name = new Label(repository.getName());
            defaultView.addComponent(name);
            defaultView.setComponentAlignment(name, Alignment.TOP_LEFT);
            Button details = new Button("Details");
            details.addStyleName("link");
            details.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    showDetailsView();
                }
            });
            defaultView.addComponent(details);
            defaultView.setComponentAlignment(details, Alignment.TOP_RIGHT);
        }

        removeAllComponents();
        addComponent(defaultView);
    }

    private void showDetailsView() {
        if (detailsView == null) {
            detailsView = new VerticalLayout();
            detailsView.setWidth("100%");

            Button hideDetails = new Button("Hide");
            hideDetails.addStyleName("link");
            hideDetails.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    showDefaultView();
                }
            });
            detailsView.addComponent(hideDetails);
            detailsView.setComponentAlignment(hideDetails, Alignment.TOP_RIGHT);

            GridLayout layout = new GridLayout(3, 1);
            layout.addComponent(new Label("<b>Name</b>", ContentMode.HTML));
            layout.addComponent(new Label("&nbsp;", ContentMode.HTML));
            layout.addComponent(new Label(repository.getName()));
            layout.newLine();

            layout.addComponent(new Label("<b>URL</b>", ContentMode.HTML));
            layout.addComponent(new Label("&nbsp;", ContentMode.HTML));
            layout.addComponent(new Label(repository.getUrl()));
            layout.newLine();

            layout.addComponent(new Label("<b>Type</b>", ContentMode.HTML));
            layout.addComponent(new Label("&nbsp;", ContentMode.HTML));
            layout.addComponent(new Label(repository.getType()));

            detailsView.addComponent(layout);
            detailsView.setComponentAlignment(layout, Alignment.TOP_LEFT);
        }

        removeAllComponents();
        addComponent(detailsView);
    }
}
