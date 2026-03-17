package integrations;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;


public abstract class AbstractRabbitContainerTest {

    /*
     * Testcontainer starts a real RabbitMQ instance for the duration of the test suite.
     * This allows to test the application against a real broker instead of mocks.
     */
    protected static final RabbitMQContainer rabbit =  new RabbitMQContainer("rabbitmq:3-management");

    // Start container
    static {
        rabbit.start();
        System.out.println("RabbitMQ started at: " + rabbit.getHost() + ":" + rabbit.getAmqpPort());
    }

    protected static void stopContainer() {
        try {
            System.out.println("Delaying container shutdown...");
            Thread.sleep(90000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        rabbit.stop();
    }

    /*
     * Spring Boot dynamic properties override.
     * The application will connect to the RabbitMQ container started by Testcontainers.
     */
    @DynamicPropertySource
    static void configureRabbit(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }
}