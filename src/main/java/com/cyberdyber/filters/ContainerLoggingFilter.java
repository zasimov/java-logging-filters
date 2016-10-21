package com.cyberdyber.filters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.apache.commons.io.IOUtils;

/**
 * Filter for HTTP(s) container requests/responses logging.
 *
 *
 * For each request writes
 *
 *   Incoming HTTP(s) request: POST http://localhost:8080/. Body: hello
 *
 * with INFO logging level.
 *
 * For each response writes
 *
 *   Outgoing HTTP(s) message: 200. Body: hello
 *
 * with INFO logging level.
 *
 *
 * You should to register this in your Application. For example:
 *
 * @ApplicationPath("/")
 * public class FiltersApplication extends Application {
 *   private Set<Object> singletons = new HashSet<Object>();
 *
 *   public FiltersApplication() {
 *       singletons.add(new ContainerFilter());
 *       singletons.add(new Servlet());
 *   }
 *
 *   @Override
 *   public Set<Object> getSingletons() {
 *       return singletons;
 *   }
 * }
 *
 * Also you can use bootstrap manager (Guice?).
 *
 * @author zasimov
 *
 */
@Provider
public class ContainerLoggingFilter implements ContainerRequestFilter,
                                               ContainerResponseFilter,
                                               WriterInterceptor {

    private static final Logger LOGGER = Logger.getLogger(
            ContainerLoggingFilter.class.getName());

    /**
     * Logs request method, URI and body.
     *
     * Log level is INFO.
     *
     * @param requestContext
     * @param body
     */
    protected void logRequest(ContainerRequestContext requestContext, String body) {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo()
                                   .getRequestUri()
                                   .toASCIIString();
        String logMessage = String.format("Incoming HTTP(s) request: %s %s. Body: %s",
                method, uri, body);
        LOGGER.info(logMessage);
    }

    /**
     * Reads request body from requestContext.
     *
     * requestContext entity stream will be replaced by in-memory copy.
     *
     * @param requestContext
     * @return body (String)
     * @throws IOException
     */
    protected String readRequestBody(ContainerRequestContext requestContext) throws IOException {
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        final InputStream input = requestContext.getEntityStream();
        try {
            IOUtils.copy(input, temp);
            byte[] body = temp.toByteArray();
            requestContext.setEntityStream(new ByteArrayInputStream(body));
            return new String(body, StandardCharsets.UTF_8);
        } finally {
            input.close();
        }
    }

    /**
     * Logs container request.
     *
     */
    public void filter(ContainerRequestContext requestContext) throws IOException {
        /* Read request body to memory and replace entity stream to memory stream. */
        String body = readRequestBody(requestContext);
        /* Log request */
        logRequest(requestContext, body);
    }

    /**
     * Replaces entity stream by stream duplicator.
     *
     */
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        StreamDuplicator duplicator = new StreamDuplicator(responseContext.getEntityStream(),
                                                           responseContext);
        responseContext.setEntityStream(duplicator);
    }

    /**
     * Logs container response.
     *
     * Log level is INFO.
     *
     */
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        context.proceed();

        if (! (context.getOutputStream() instanceof StreamDuplicator)) {
            LOGGER.info("Outgoing HTTP(s) request: MISSED");
            return;
        }

        // Restore duplicator from context.
        StreamDuplicator duplicator = (StreamDuplicator) context.getOutputStream();

        String statusCode = "UNKNOWN";
        if (duplicator.getContext() instanceof ContainerResponseContext) {
            // Restore response context from duplicator to get response status code.
            ContainerResponseContext responseContext = (ContainerResponseContext) duplicator.getContext();
            statusCode = Integer.toString(responseContext.getStatus());
        }

        // Log outgoing HTTP request.
        String logMessage = String.format("Outgoing HTTP(s) request: %s. Body: %s",
                statusCode,
                duplicator.getEntityCopy());
        LOGGER.info(logMessage);
    }
}
