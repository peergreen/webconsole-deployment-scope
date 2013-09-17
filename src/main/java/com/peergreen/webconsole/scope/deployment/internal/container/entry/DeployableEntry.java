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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainer;

/**
 * @author Mohammed Boukada
 */
public class DeployableEntry {
    private URI uri;
    private String name;
    private DeployableSource source;
    private DeployableContainer container;
    private DeployableEntry parent;
    private boolean isDeployable = true;
    private Collection<DeployableEntry> children = new ArrayList<>();

    public DeployableEntry(URI uri, DeployableSource source) {
        this(uri, null, source, null, null);
    }

    public DeployableEntry(URI uri, String name, DeployableSource source, DeployableContainer container, DeployableEntry parent) {
        this.uri = uri;
        if (name == null || "".equals(name)) {
            try {
                this.name = new File(uri).getName();
            } catch (IllegalArgumentException e) {
                this.name = uri.toString();
            }
        } else {
            this.name = name;
        }
        this.source = source;
        this.container = container;
        setParent(parent);
    }

    public String getName() {
        return name;
    }

    public DeployableSource getSource() {
        return source;
    }

    public void setSource(DeployableSource source) {
        this.source = source;
    }

    public URI getUri() {
        return uri;
    }

    public DeployableContainer getContainer() {
        return container;
    }

    public void setContainer(DeployableContainer container) {
        this.container = container;
    }

    public DeployableEntry getParent() {
        return parent;
    }

    public void setParent(DeployableEntry parent) {
        // remove child from previous parent
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        // add child to new parent
        if (parent != null) {
            parent.addChild(this);
        }
        // set new parent
        this.parent = parent;
    }

    public boolean isDeployable() {
        return isDeployable;
    }

    public void setDeployable(boolean deployable) {
        isDeployable = deployable;
    }

    public void addChild(DeployableEntry deployableEntry) {
        if (!children.contains(deployableEntry)) {
            children.add(deployableEntry);
        }
    }

    public void removeChild(DeployableEntry deployableEntry) {
        if (children.contains(deployableEntry)) {
            children.remove(deployableEntry);
        }
    }

    public Collection<DeployableEntry> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) ||
                ((obj != null) && ((DeployableEntry) obj).getUri().equals(getUri()));
    }

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }
}
