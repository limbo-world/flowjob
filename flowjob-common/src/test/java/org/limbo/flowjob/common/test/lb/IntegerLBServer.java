package org.limbo.flowjob.common.test.lb;

import lombok.Getter;
import org.limbo.flowjob.common.lb.LBServer;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Brozen
 * @since 2022-12-14
 */
public class IntegerLBServer implements LBServer {

    @Getter
    private final int value;

    public IntegerLBServer(int value) {
        this.value = value;
    }

    @Override
    public String getServerId() {
        return String.valueOf(value);
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public URL getUrl() {
        try {
            return new URL("test://" + value);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
