package main.java.com.example.ztna_connector.protocol;


public class ConnectFrame {
    private String service;
    private String jwt;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public ConnectFrame(String service, String jwt) {
        this.service = service;
        this.jwt = jwt;
    }
}
