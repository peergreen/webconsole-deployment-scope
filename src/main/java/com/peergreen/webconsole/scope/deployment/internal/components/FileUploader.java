/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.components;

import static com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainerType.DEPLOYED;
import static com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainerType.DEPLOYMENT_PLAN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.notifier.Task;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Upload;


/**
 * @author Mohammed Boukada
 */
public class FileUploader implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener, Upload.StartedListener {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(FileUploader.class);

    private DeploymentViewManager deploymentViewService;
    private INotifierService notifierService;
    private ArtifactBuilder artifactBuilder;
    private OptionGroup option;
    private Task uploadTask;

    public FileUploader(DeploymentViewManager deploymentViewService, INotifierService notifierService, ArtifactBuilder artifactBuilder, OptionGroup option) {
        (new File(Constants.STORAGE_DIRECTORY)).mkdirs();

        this.deploymentViewService = deploymentViewService;
        this.notifierService = notifierService;
        this.artifactBuilder = artifactBuilder;
        this.option = option;
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        try {
            String fileLocation = Constants.STORAGE_DIRECTORY + File.separator + filename;
            File file = new File(fileLocation);
            if (file.exists()) {
                file.delete();
            }
            return new FileOutputStream(fileLocation, true);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        uploadTask.stop();
        notifierService.addNotification(String.format("'%s' was uploaded.", event.getFilename()));
        URI uri = new File(Constants.STORAGE_DIRECTORY + File.separator + event.getFilename()).toURI();
        String optionValue = (String) option.getValue();

        if (DEPLOYED.attribute().equals(optionValue)) {
            deploymentViewService.deploy(artifactBuilder.build(event.getFilename(), uri));
        } else if (DEPLOYMENT_PLAN.attribute().equals(optionValue)) {
            deploymentViewService.addToDeployable(uri);
            deploymentViewService.addToDeploymentPlan(uri);
            deploymentViewService.showDeploymentPlanView();
        } else {
            deploymentViewService.addToDeployable(uri);
        }
    }

    @Override
    public void uploadFailed(Upload.FailedEvent event) {
        uploadTask.stop();
        notifierService.addNotification("Fail to upload '" + event.getFilename() + "'.");
    }

    @Override
    public void uploadStarted(Upload.StartedEvent event) {
        uploadTask = notifierService.createTask("Uploading '" + event.getFilename() + "'");
    }
}
