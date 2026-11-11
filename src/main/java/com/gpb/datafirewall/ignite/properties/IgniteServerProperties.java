package com.gpb.datafirewall.ignite.properties;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ignite")
public class IgniteServerProperties {

    private String instanceName = "ignite-server";
    private String consistentId = "ignite-server";
    private String localHost = "127.0.0.1";

    private Cluster cluster = new Cluster();
    private Discovery discovery = new Discovery();
    private Communication communication = new Communication();
    private ThinClient thinClient = new ThinClient();
    private Ssl ssl = new Ssl();
    private Authentication authentication = new Authentication();
    private Persistence persistence = new Persistence();
    private DataRegion dataRegion = new DataRegion();
    private Set<String> expectedServers;

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getConsistentId() {
        return consistentId;
    }

    public void setConsistentId(String consistentId) {
        this.consistentId = consistentId;
    }

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Discovery getDiscovery() {
        return discovery;
    }

    public void setDiscovery(Discovery discovery) {
        this.discovery = discovery;
    }

    public Communication getCommunication() {
        return communication;
    }

    public void setCommunication(Communication communication) {
        this.communication = communication;
    }

    public ThinClient getThinClient() {
        return thinClient;
    }

    public void setThinClient(ThinClient thinClient) {
        this.thinClient = thinClient;
    }

    public Ssl getSsl() {
        return ssl;
    }

    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public Persistence getPersistence() {
        return persistence;
    }

    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

    public DataRegion getDataRegion() {
        return dataRegion;
    }

    public void setDataRegion(DataRegion dataRegion) {
        this.dataRegion = dataRegion;
    }

    public static class Cluster {

        private boolean activateOnStart = false;

        private Set<String> expectedServers = new LinkedHashSet<>();

        public boolean isActivateOnStart() {
            return activateOnStart;
        }

        public void setActivateOnStart(boolean activateOnStart) {
            this.activateOnStart = activateOnStart;
        }

        public Set<String> getExpectedServers() {
            return expectedServers;
        }

        public void setExpectedServers(Set<String> expectedServers) {
            this.expectedServers = expectedServers;
        }
    }

    public static class Discovery {
        private List<String> addresses = new ArrayList<>();
        private int localPort = 47500;
        private int localPortRange = 20;

        public List<String> getAddresses() {
            return addresses;
        }

        public void setAddresses(List<String> addresses) {
            this.addresses = addresses;
        }

        public int getLocalPort() {
            return localPort;
        }

        public void setLocalPort(int localPort) {
            this.localPort = localPort;
        }

        public int getLocalPortRange() {
            return localPortRange;
        }

        public void setLocalPortRange(int localPortRange) {
            this.localPortRange = localPortRange;
        }
    }

    public static class Communication {
        private int localPort = 47100;
        private int localPortRange = 20;

        public int getLocalPort() {
            return localPort;
        }

        public void setLocalPort(int localPort) {
            this.localPort = localPort;
        }

        public int getLocalPortRange() {
            return localPortRange;
        }

        public void setLocalPortRange(int localPortRange) {
            this.localPortRange = localPortRange;
        }
    }

    public static class ThinClient {
        private String host;
        private int port = 10800;
        private int portRange = 100;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getPortRange() {
            return portRange;
        }

        public void setPortRange(int portRange) {
            this.portRange = portRange;
        }
    }

    public static class Ssl {
        private boolean enabled = false;
        private boolean clientAuth = false;

        private String keyStorePath;
        private String keyStorePassword;
        private String keyStoreType = "JKS";

        private String trustStorePath;
        private String trustStorePassword;
        private String trustStoreType = "JKS";

        /**
         * Protocol for SSLContext creation.
         * Обычно лучше оставить "TLS", а конкретные версии ограничивать через protocols.
         */
        private String protocol = "TLS";

        /**
         * Разрешенные TLS versions.
         */
        private List<String> protocols = new ArrayList<>();

        /**
         * Разрешенные cipher suites.
         */
        private List<String> cipherSuites = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isClientAuth() {
            return clientAuth;
        }

        public void setClientAuth(boolean clientAuth) {
            this.clientAuth = clientAuth;
        }

        public String getKeyStorePath() {
            return keyStorePath;
        }

        public void setKeyStorePath(String keyStorePath) {
            this.keyStorePath = keyStorePath;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public void setKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
        }

        public String getKeyStoreType() {
            return keyStoreType;
        }

        public void setKeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        public String getTrustStorePath() {
            return trustStorePath;
        }

        public void setTrustStorePath(String trustStorePath) {
            this.trustStorePath = trustStorePath;
        }

        public String getTrustStorePassword() {
            return trustStorePassword;
        }

        public void setTrustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
        }

        public String getTrustStoreType() {
            return trustStoreType;
        }

        public void setTrustStoreType(String trustStoreType) {
            this.trustStoreType = trustStoreType;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public List<String> getProtocols() {
            return protocols;
        }

        public void setProtocols(List<String> protocols) {
            this.protocols = protocols;
        }

        public List<String> getCipherSuites() {
            return cipherSuites;
        }

        public void setCipherSuites(List<String> cipherSuites) {
            this.cipherSuites = cipherSuites;
        }
    }

    public static class Authentication {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Persistence {
        private boolean enabled = false;
        private String storagePath;
        private String walPath;
        private String walArchivePath;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getStoragePath() {
            return storagePath;
        }

        public void setStoragePath(String storagePath) {
            this.storagePath = storagePath;
        }

        public String getWalPath() {
            return walPath;
        }

        public void setWalPath(String walPath) {
            this.walPath = walPath;
        }

        public String getWalArchivePath() {
            return walArchivePath;
        }

        public void setWalArchivePath(String walArchivePath) {
            this.walArchivePath = walArchivePath;
        }
    }

    public static class DataRegion {
        private String name = "Default_Region";
        private long initialSizeMb = 256;
        private long maxSizeMb = 512;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getInitialSizeMb() {
            return initialSizeMb;
        }

        public void setInitialSizeMb(long initialSizeMb) {
            this.initialSizeMb = initialSizeMb;
        }

        public long getMaxSizeMb() {
            return maxSizeMb;
        }

        public void setMaxSizeMb(long maxSizeMb) {
            this.maxSizeMb = maxSizeMb;
        }
    }
}