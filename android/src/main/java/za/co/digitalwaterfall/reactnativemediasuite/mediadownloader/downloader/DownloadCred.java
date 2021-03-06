package za.co.digitalwaterfall.reactnativemediasuite.mediadownloader.downloader;

public class DownloadCred {

    public String queryParams;
    public String cookie;

    public DownloadCred(String queryParams, String cookie) {
        this.queryParams = queryParams;
        this.cookie = cookie;
    }
}
