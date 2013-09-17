/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.dd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.notifier.Task;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainer;
import com.vaadin.server.StreamVariable;

/**
 * @author Mohammed Boukada
 */
public class DesktopDragAndDropStream implements StreamVariable {
    private FileOutputStream fos;
    private String fileName;
    private INotifierService notifierService;
    private DeployableContainer deployableContainer;
    private Task uploadTask;

    public DesktopDragAndDropStream(FileOutputStream fos,
                                    String fileName,
                                    INotifierService notifierService,
                                    DeployableContainer deployableContainer) {
        this.fos = fos;
        this.fileName = fileName;
        this.notifierService = notifierService;
        this.deployableContainer = deployableContainer;
    }

    @Override
    public OutputStream getOutputStream() {
        return fos;
    }

    @Override
    public boolean listenProgress() {
        return true;
    }

    @Override
    public void onProgress(StreamingProgressEvent streamingProgressEvent) {
        // hope it lasts ...
    }

    @Override
    public void streamingStarted(StreamVariable.StreamingStartEvent event) {
        uploadTask = notifierService.createTask("Uploading '" + event.getFileName() + "'");
    }

    @Override
    public void streamingFinished(StreamVariable.StreamingEndEvent event) {
        uploadTask.stop();
        URI uri = new File(Constants.STORAGE_DIRECTORY + File.separator + fileName).toURI();
        deployableContainer.receive(uri);
        notifierService.addNotification("'" + fileName + "' was uploaded.");
    }

    @Override
    public void streamingFailed(StreamVariable.StreamingErrorEvent event) {
        uploadTask.stop();
        notifierService.addNotification("Fail to upload '" + fileName + "'.");
    }

    @Override
    public boolean isInterrupted() {
        return false;
    }
}
