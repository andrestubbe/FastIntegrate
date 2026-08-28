package fastintegrate.bus;

/**
 * Functional interface for subscribing to events on the SidecarEventBus.
 */
@FunctionalInterface
public interface EventSubscriber {

    /**
     * Invoked when a matching event is delivered.
     *
     * @param event the received SidecarEvent
     * @throws Exception if handler processing fails
     */
    void onEvent(SidecarEvent event) throws Exception;
}
