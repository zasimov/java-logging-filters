package com.cyberdyber.filters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.apache.commons.io.IOUtils;

/**
 * Logger writes information about external HTTP(s) requests and responses.
 *
 * For example like this:
 *
 * INFO: External HTTP(s) request: POST http://localhost:5008/send. Body: {"a":"eugene"}
 * INFO: External HTTP(s) response from http://localhost:5008/send: 200, body: hello
 *
 * To use logger in your project you need to register logger filter:
 *
 *      ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
 *              .register(new ClientLoggingFilter());
 *      client = clientBuilder.build();
 *
 * @author zasimov
 *
 */
@Provider
public class ClientLoggingFilter implements ClientRequestFilter,
                                            ClientResponseFilter,
                                            WriterInterceptor {

    private static final Logger LOGGER = Logger.getLogger(
            ContainerLoggingFilter.class.getName());

    /**
     * Logs outgoing request method, URI and body.
     *
     * Log level is INFO.
     *
     * @param requestContext
     * @param body
     */
    protected void logExternalRequest(ClientRequestContext requestContext, String body) {
        String method = requestContext.getMethod();
        String uri = requestContext.getUri()
                                   .toASCIIString();
        String logMessage = String.format("External HTTP(s) request: %s %s. Body: %s",
                method, uri, body);
        LOGGER.info(logMessage);
    }

    /**
     * Logs request or replace entity string with stream duplicator.
     *
     */
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (requestContext.getEntity() == null) {
            // We have request without body
            logExternalRequest(requestContext, "<empty>");
        } else {
            StreamDuplicator duplicator = new StreamDuplicator(requestContext.getEntityStream(),
                    requestContext);
            requestContext.setEntityStream(duplicator);
        }
    }

    /**
     * Logs request to external system.
     *
     */
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        context.proceed();

        if (! (context.getOutputStream() instanceof StreamDuplicator)) {
            LOGGER.info("External HTTP(s) request: MISSED");
            return;
        }

        // Restore duplicator from context.
        StreamDuplicator duplicator = (StreamDuplicator) context.getOutputStream();

        // Restore response context from duplicator to get response status code.
        if (! (duplicator.getContext() instanceof ClientRequestContext)) {
            LOGGER.info("External HTTP(s) request: MISSED");
            return;
        }

        ClientRequestContext requestContext = (ClientRequestContext) duplicator.getContext();
        String body = duplicator.getEntityCopy();
        // Log external HTTP request.
        logExternalRequest(requestContext, body);
    }

    /**
     * Reads response body from responseContext.
     *
     * responseContext entity stream will be replaced by in-memory copy.
     *
     * @param responseContext
     * @return body (String)
     * @throws IOException
     */
    protected String readResponseBody(ClientResponseContext responseContext) throws IOException {
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        final InputStream input = responseContext.getEntityStream();
        try {
            IOUtils.copy(input, temp);
            byte[] body = temp.toByteArray();
            responseContext.setEntityStream(new ByteArrayInputStream(body));
            return new String(body, StandardCharsets.UTF_8);
        } finally {
            input.close();
        }
    }

    /**
     * Logs external HTTP(s) response.
     *
     * Log level is INFO.
     */
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        String body = readResponseBody(responseContext);
        String logMessage = String.format("External HTTP(s) response from %s: %d, body: %s",
                requestContext.getUri().toASCIIString(),
                responseContext.getStatus(),
                body);
        LOGGER.info(logMessage);
    }

}
