package com.cyberdyber.filters;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class FiltersApplication extends Application {
    private Set<Object> singletons = new HashSet<Object>();

    public FiltersApplication() {
        singletons.add(new ContainerLoggingFilter());
        singletons.add(new Servlet());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
