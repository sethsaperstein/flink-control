package com.sethsaperstein.flinkcontrolapi.util;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;
import org.apache.flink.kubernetes.operator.api.lifecycle.ResourceLifecycleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MAX_LENGTH = 45;
    private static final Duration RETRY_INTERVAL = Duration.ofSeconds(1);

    public static String generateRandomName() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // Generate first character
        char firstChar = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        sb.append(firstChar);

        // Generate remaining characters
        int remainingLength = MAX_LENGTH - 2;  // Subtract 2 to leave space for first and last characters
        while (remainingLength > 0) {
            char nextChar = ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length()));
            sb.append(nextChar);
            remainingLength--;
        }

        // Generate last character
        char lastChar = ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length()));
        sb.append(lastChar);

        return sb.toString();
    }

    public static void waitForFlinkDeploymentStatus(
        String name,
        String namespace,
        Duration timeout,
        List<ResourceLifecycleState> targetStates,
        MixedOperation<FlinkDeployment, KubernetesResourceList<FlinkDeployment>, Resource<FlinkDeployment>> client
    ) throws Throwable {
        Instant startTime = Instant.now();
        Instant endTime = startTime.plus(timeout);

        Exception lastException = null;
        List<String> targetStatesReadable = targetStates.stream().map(Enum::toString).toList();

        while (Instant.now().isBefore(endTime)) {
            try {
                FlinkDeployment flinkDeployment = client
                    .inNamespace(namespace)
                    .withName(name)
                    .get();

                if (flinkDeployment != null) {
                    ResourceLifecycleState lifecycleState = flinkDeployment
                        .getStatus()
                        .getLifecycleState();
                    if (targetStates.contains(lifecycleState)) {
                        return;
                    }
                }

            } catch (Exception e) {
                lastException = e;
            }

            try {
                logger.info("Waiting for FlinkDeployment {} in namespace {} to reach {} states", name, namespace, targetStatesReadable);
                Thread.sleep(RETRY_INTERVAL.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting", e);
            }
        }

        // Timeout reached, throw the last exception with the timeout message if it exists
        if (lastException != null) {
            throw new TimeoutException("Timeout reached, resource did not reach the desired state: " + targetStatesReadable)
                .initCause(lastException);
        } else {
            throw new TimeoutException("Timeout reached, resource did not reach the desired state: " + targetStatesReadable);
        }
    }

    public static void waitForFlinkDeploymentTeardown(
        String name,
        String namespace,
        Duration timeout,
        MixedOperation<FlinkDeployment, KubernetesResourceList<FlinkDeployment>, Resource<FlinkDeployment>> client
    ) throws TimeoutException {
        Instant startTime = Instant.now();
        Instant endTime = startTime.plus(timeout);

        while (Instant.now().isBefore(endTime)) {
            try {
                Resource<FlinkDeployment> resource = client
                    .inNamespace(namespace)
                    .withName(name);

                if (resource.get() == null) {
                    return;
                }
                else {
                    resource.delete();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                logger.info("Waiting for FlinkDeployment {} in namespace {} to terminate", name, namespace);
                Thread.sleep(RETRY_INTERVAL.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting", e);
            }
        }
        throw new TimeoutException("Timeout reached, resource did not terminate");
     }

    public static <T extends HasMetadata & Namespaced> void deleteKubernetesResource(
        Duration timeout,
        KubernetesClient client,
        String resourceName,
        String resourceType,
        ResourceProcessor<T> processor
    ) throws TimeoutException {
        Instant startTime = Instant.now();
        Instant endTime = startTime.plus(timeout);

        while (Instant.now().isBefore(endTime)) {
            try {
                Resource<T> resource = processor.process(client);
                if (resource.get() == null) {
                    return;
                }
                else {
                    resource.delete();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                logger.info("Waiting for {} {} to terminate", resourceType, resourceName);
                Thread.sleep(RETRY_INTERVAL.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting", e);
            }
        }
        throw new TimeoutException("Timeout reached, resource did not terminate");
    }

    @FunctionalInterface
    interface ResourceProcessor<T extends HasMetadata & Namespaced> {
        Resource<T> process(KubernetesClient client);
    }
}
