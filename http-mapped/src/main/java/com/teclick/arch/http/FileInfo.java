package com.teclick.arch.http;

/**
 * Created by Nelson on 2018-02-04.
 */
public class FileInfo {

    private String uri;
    private String repo;
    private String path;
    private String created;
    private String createdBy;
    private String lastModified;
    private String modifiedBy;
    private String lastUpdated;

    private String downloadUri;
    private String remoteUrl;
    private long size;
    private String mimeType;
    private CheckSums checksums;
    private CheckSums originalChecksums;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public CheckSums getChecksums() {
        return checksums;
    }

    public void setChecksums(CheckSums checksums) {
        this.checksums = checksums;
    }

    public CheckSums getOriginalChecksums() {
        return originalChecksums;
    }

    public void setOriginalChecksums(CheckSums originalChecksums) {
        this.originalChecksums = originalChecksums;
    }

}
