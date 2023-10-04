package org.limbo.flowjob.worker.core.executor;

/**
 * @author OttO
 * @since 2023/9/8
 */
public class ThreadLocalContext {

    private static final ThreadLocal<ExecuteContext> EXECUTE_CONTEXT = new ThreadLocal<>();

    public static void setExecuteContext(ExecuteContext executeContext) {
        EXECUTE_CONTEXT.set(executeContext);
    }

    public static ExecuteContext getExecuteContext() {
        return EXECUTE_CONTEXT.get();
    }

    public static void clear() {
        EXECUTE_CONTEXT.remove();
    }

}
