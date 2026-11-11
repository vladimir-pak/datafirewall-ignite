package com.gpb.datafirewall.ignite.controller;

import com.gpb.datafirewall.ignite.cef.SvoiApiLog;
import com.gpb.datafirewall.ignite.properties.IgniteServerProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterState;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ignite")
@RequiredArgsConstructor
public class IgniteClusterController {

    private final Ignite ignite;
    private final IgniteServerProperties properties;

    @GetMapping("/state")
    @Tag(name = "Check state", description = "Check ignite cluster state")
    @SvoiApiLog(functionName = "CheckState")
    public Map<String, Object> state() {
        return clusterInfo();
    }

    @PostMapping("/activate")
    @Tag(name = "Activate", description = "Activate ignite cluster state")
    @SvoiApiLog(functionName = "ActivateCluster")
    public Map<String, Object> activate() {

        Set<String> expected = properties.getCluster().getExpectedServers();

        if (expected == null || expected.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "ignite.cluster.expected-servers is not configured"
            );
        }

        Set<String> online = onlineServerIds();

        if (!online.equals(expected)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unexpected cluster topology. Expected: "
                            + expected + ", online: " + online
            );
        }

        Set<String> baseline = baselineServerIds();

        if (!baseline.isEmpty() && !baseline.equals(expected)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Baseline topology mismatch: " + baseline
            );
        }

        ClusterState currentState = ignite.cluster().state();

        if (currentState == ClusterState.INACTIVE) {
            ignite.cluster().state(ClusterState.ACTIVE);
        } else if (currentState != ClusterState.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unexpected cluster state: " + currentState
            );
        }

        if (!baselineServerIds().equals(expected)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cluster activated, but baseline topology does not match expected servers"
            );
        }

        return clusterInfo();
    }

    private Set<String> onlineServerIds() {
        return ignite.cluster().nodes().stream()
                .filter(node -> !node.isClient())
                .map(node -> String.valueOf(node.consistentId()))
                .collect(Collectors.toSet());
    }

    private Set<String> baselineServerIds() {
        var baseline = ignite.cluster().currentBaselineTopology();

        if (baseline == null) {
            return Set.of();
        }

        return baseline.stream()
                .map(node -> String.valueOf(node.consistentId()))
                .collect(Collectors.toSet());
    }

    private Map<String, Object> clusterInfo() {
        return Map.of(
                "state", ignite.cluster().state().name(),
                "onlineServers", onlineServerIds(),
                "baselineServers", baselineServerIds()
        );
    }
}