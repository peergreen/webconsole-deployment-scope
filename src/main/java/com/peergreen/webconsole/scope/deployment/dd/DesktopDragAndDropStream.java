package com.peergreen.webconsole.scope.deployment.dd;

import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.scope.deployment.IDeploymentView;
import com.vaadin.server.StreamVariable;

import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author Mohammed Boukada
 */
public class DesktopDragAndDropStream implements StreamVariable {
    private FileOutputStream fos;
    private String fileName;
    private INotifierService notifierService;
    private IDeploymentView deploymentView;
    private boolean deploy;

    public DesktopDragAndDropStream(FileOutputStream fos,
                                    String fileName,
                                    INotifierService notifierService,
                                    IDeploymentView deploymentView,
                                    boolean deploy
    ) {
        this.fos = fos;
        this.fileName = fileName;
        this.notifierService = notifierService;
        this.deploymentView = deploymentView;
        this.deploy = deploy;
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
        if (deploy) {
            deploymentView.addToDeployed(fileName);
        }
        else {
            deploymentView.addDeployable(fileName);
        }
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
