package com.gpb.datafirewall.ignite.config;

import java.util.List;

import com.gpb.datafirewall.ignite.properties.IgniteServerProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.ssl.SslContextFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class IgniteServerConfig {

    private final IgniteServerProperties props;

    @Bean(destroyMethod = "close")
    public Ignite ignite() {
        IgniteConfiguration cfg = createIgniteConfiguration();

        Ignite ignite = Ignition.start(cfg);

        if (
            props.getCluster().isActivateOnStart()
                    && ignite.cluster().state() == ClusterState.INACTIVE
        ) {
            ignite.cluster().state(ClusterState.ACTIVE);
            log.info("Ignite cluster activated");
        }

        log.info("Ignite server started. nodeId={}, clusterSize={}",
                ignite.cluster().localNode().id(),
                ignite.cluster().nodes().size());

        return ignite;
    }

    private IgniteConfiguration createIgniteConfiguration() {
        validate();

        IgniteConfiguration cfg = new IgniteConfiguration();

        cfg.setIgniteInstanceName(props.getInstanceName());
        cfg.setConsistentId(props.getConsistentId());
        cfg.setClientMode(false);
        cfg.setLocalHost(props.getLocalHost());

        cfg.setDiscoverySpi(discoverySpi());
        cfg.setCommunicationSpi(communicationSpi());
        cfg.setClientConnectorConfiguration(clientConnectorConfiguration());
        cfg.setAuthenticationEnabled(props.getAuthentication().isEnabled());

        cfg.setWorkDirectory(props.getWorkDirectory());

        if (props.getSsl().isEnabled()) {
            cfg.setSslContextFactory(sslContextFactory());
        }

        if (props.getPersistence().isEnabled()) {
            cfg.setDataStorageConfiguration(dataStorageConfiguration());
        }

        if (props.getAuthentication().isEnabled() && !props.getPersistence().isEnabled()) {
            throw new IllegalStateException(
                    "ignite.authentication.enabled=true requires ignite.persistence.enabled=true"
            );
        }

        logStartup();

        return cfg;
    }

    private TcpDiscoverySpi discoverySpi() {
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        ipFinder.setAddresses(props.getDiscovery().getAddresses());

        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        spi.setIpFinder(ipFinder);

        /*
         * Можно не задавать localAddress отдельно.
         * Ignite возьмет cfg.setLocalHost(...).
         * Это снижает риск случайно переопределить адрес SPI.
         */
        spi.setLocalPort(props.getDiscovery().getLocalPort());
        spi.setLocalPortRange(props.getDiscovery().getLocalPortRange());

        return spi;
    }

    private TcpCommunicationSpi communicationSpi() {
        TcpCommunicationSpi spi = new TcpCommunicationSpi();

        /*
         * Аналогично: localAddress не задаем.
         * Используется IgniteConfiguration.localHost.
         */
        spi.setLocalPort(props.getCommunication().getLocalPort());
        spi.setLocalPortRange(props.getCommunication().getLocalPortRange());

        return spi;
    }

    private ClientConnectorConfiguration clientConnectorConfiguration() {
        ClientConnectorConfiguration cfg = new ClientConnectorConfiguration();

        String host = props.getThinClient().getHost();
        if (host == null || host.isBlank()) {
            host = props.getLocalHost();
        }

        cfg.setHost(host);
        cfg.setPort(props.getThinClient().getPort());
        cfg.setPortRange(props.getThinClient().getPortRange());

        if (props.getSsl().isEnabled()) {
            cfg.setSslEnabled(true);
            cfg.setSslClientAuth(props.getSsl().isClientAuth());
            cfg.setUseIgniteSslContextFactory(true);
        }

        return cfg;
    }

    private DataStorageConfiguration dataStorageConfiguration() {
        IgniteServerProperties.Persistence persistence = props.getPersistence();
        IgniteServerProperties.DataRegion region = props.getDataRegion();

        DataRegionConfiguration defaultRegion = new DataRegionConfiguration()
                .setName(region.getName())
                .setInitialSize(toBytes(region.getInitialSizeMb()))
                .setMaxSize(toBytes(region.getMaxSizeMb()))
                .setPersistenceEnabled(true);

        return new DataStorageConfiguration()
                .setStoragePath(persistence.getStoragePath())
                .setWalPath(persistence.getWalPath())
                .setWalArchivePath(persistence.getWalArchivePath())
                .setDefaultDataRegionConfiguration(defaultRegion);
    }

    private SslContextFactory sslContextFactory() {
        IgniteServerProperties.Ssl ssl = props.getSsl();

        SslContextFactory factory = new SslContextFactory();

        factory.setKeyStoreFilePath(ssl.getKeyStorePath());
        factory.setKeyStorePassword(ssl.getKeyStorePassword().toCharArray());
        factory.setTrustStoreFilePath(ssl.getTrustStorePath());
        factory.setTrustStorePassword(ssl.getTrustStorePassword().toCharArray());

        if (ssl.getKeyStoreType() != null && !ssl.getKeyStoreType().isBlank()) {
            factory.setKeyStoreType(ssl.getKeyStoreType());
        }

        if (ssl.getTrustStoreType() != null && !ssl.getTrustStoreType().isBlank()) {
            factory.setTrustStoreType(ssl.getTrustStoreType());
        }

        if (ssl.getProtocol() != null && !ssl.getProtocol().isBlank()) {
            factory.setProtocol(ssl.getProtocol());
        }

        if (ssl.getProtocols() != null && !ssl.getProtocols().isEmpty()) {
            factory.setProtocols(ssl.getProtocols().toArray(new String[0]));
        }

        if (ssl.getCipherSuites() != null && !ssl.getCipherSuites().isEmpty()) {
            factory.setCipherSuites(ssl.getCipherSuites().toArray(new String[0]));
        }

        return factory;
    }

    private void validate() {
        requireText(props.getInstanceName(), "ignite.instance-name");
        requireText(props.getConsistentId(), "ignite.consistent-id");
        requireText(props.getLocalHost(), "ignite.local-host");

        List<String> discoveryAddresses = props.getDiscovery().getAddresses();
        if (discoveryAddresses == null || discoveryAddresses.isEmpty()) {
            throw new IllegalStateException("ignite.discovery.addresses must not be empty");
        }

        if (props.getPersistence().isEnabled()) {
            requireText(props.getPersistence().getStoragePath(), "ignite.persistence.storage-path");
            requireText(props.getPersistence().getWalPath(), "ignite.persistence.wal-path");
            requireText(props.getPersistence().getWalArchivePath(), "ignite.persistence.wal-archive-path");
        }

        if (props.getSsl().isEnabled()) {
            requireText(props.getSsl().getKeyStorePath(), "ignite.ssl.key-store-path");
            requireText(props.getSsl().getKeyStorePassword(), "ignite.ssl.key-store-password");
            requireText(props.getSsl().getTrustStorePath(), "ignite.ssl.trust-store-path");
            requireText(props.getSsl().getTrustStorePassword(), "ignite.ssl.trust-store-password");
        }

        if (props.getDataRegion().getMaxSizeMb() < props.getDataRegion().getInitialSizeMb()) {
            throw new IllegalStateException(
                    "ignite.data-region.max-size-mb must be >= ignite.data-region.initial-size-mb"
            );
        }
    }

    private void logStartup() {
        log.info("=== Ignite configuration ===");
        log.info("instanceName={}", props.getInstanceName());
        log.info("consistentId={}", props.getConsistentId());
        log.info("localHost={}", props.getLocalHost());
        log.info("discoveryAddresses={}", props.getDiscovery().getAddresses());
        log.info("discoveryPort={} range={}",
                props.getDiscovery().getLocalPort(),
                props.getDiscovery().getLocalPortRange());
        log.info("communicationPort={} range={}",
                props.getCommunication().getLocalPort(),
                props.getCommunication().getLocalPortRange());
        log.info("thinClientHost={}", props.getThinClient().getHost());
        log.info("thinClientPort={} range={}",
                props.getThinClient().getPort(),
                props.getThinClient().getPortRange());
        log.info("sslEnabled={}", props.getSsl().isEnabled());
        log.info("authenticationEnabled={}", props.getAuthentication().isEnabled());
        log.info("persistenceEnabled={}", props.getPersistence().isEnabled());

        if (props.getPersistence().isEnabled()) {
            log.info("storagePath={}", props.getPersistence().getStoragePath());
            log.info("walPath={}", props.getPersistence().getWalPath());
            log.info("walArchivePath={}", props.getPersistence().getWalArchivePath());
            log.info("dataRegion={} initialSizeMb={} maxSizeMb={}",
                    props.getDataRegion().getName(),
                    props.getDataRegion().getInitialSizeMb(),
                    props.getDataRegion().getMaxSizeMb());
        }

        log.info("============================");
    }

    private static void requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required property is not set: " + propertyName);
        }
    }

    private static long toBytes(long mb) {
        return mb * 1024L * 1024L;
    }
}