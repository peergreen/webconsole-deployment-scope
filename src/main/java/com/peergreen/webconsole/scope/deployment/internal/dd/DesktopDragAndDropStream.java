package com.peergreen.webconsole.scope.deployment.internal.dd;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainer;
import com.vaadin.server.StreamVariable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author Mohammed Boukada
 */
public class DesktopDragAndDropStream implements StreamVariable {
    private FileOutputStream fos;
    private String fileName;
    private INotifierService notifierService;
    private DeployableContainer deployableContainer;

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
    public void onProgress(StreamVariable.StreamingProgressEvent event) {
        notifierService.updateTask(this, event.getBytesReceived());
    }

    @Override
    public void streamingStarted(StreamVariable.StreamingStartEvent event) {
        notifierService.startTask(this, "Uploading '" + event.getFileName() + "'", event.getContentLength());
    }

    @Override
    public void streamingFinished(StreamVariable.StreamingEndEvent event) {
        notifierService.stopTask(this);
        URI uri = new File(Constants.STORAGE_DIRECTORY + File.separator + fileName).toURI();
        deployableContainer.receive(uri);
        notifierService.addNotification("'" + fileName + "' was uploaded.");
    }

    @Override
    public void streamingFailed(StreamVariable.StreamingErrorEvent event) {
        notifierService.addNotification("Fail to upload '" + fileName + "'.");
    }

    @Override
    public boolean isInterrupted() {
        return false;
    }
}
