package org.scottishtecharmy.wishaw.quarkus;

import io.smallrye.common.annotation.Blocking;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
@Blocking
public class WishawApplication extends Application {
}

