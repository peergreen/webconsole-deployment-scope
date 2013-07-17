/*
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.components;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.scope.deployment.IDeploymentView;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author Mohammed Boukada
 */
public class FileUploader implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener, Upload.StartedListener, Upload.ProgressListener {

    private IDeploymentView deployment;
    private INotifierService notifierService;

    public FileUploader(IDeploymentView deployment, INotifierService notifierService) {
        (new File(Constants.STORAGE_DIRECTORY)).mkdirs();

        this.deployment = deployment;
        this.notifierService = notifierService;
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        try {
            String fileLocation = Constants.STORAGE_DIRECTORY + File.separator + filename;
            if ((new File(fileLocation)).exists()) {
                Notification.show("File '" + filename + "' already exists !");
                return null;
            }
            return new FileOutputStream(fileLocation, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        notifierService.stopTask(this);
        notifierService.addNotification("'" + event.getFilename() + "' was uploaded.");
        deployment.addDeployable(event.getFilename());
    }

    @Override
    public void uploadFailed(Upload.FailedEvent event) {
        notifierService.addNotification("Fail to upload '" + event.getFilename() + "'.");
    }

    @Override
    public void updateProgress(long readBytes, long contentLength) {
        notifierService.updateTask(this, readBytes);
    }

    @Override
    public void uploadStarted(Upload.StartedEvent event) {
        notifierService.startTask(this, "Uploading '" + event.getFilename() + "'", event.getContentLength());
    }
}
