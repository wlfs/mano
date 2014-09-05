/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;

/**
 *
 * @author jun <jun@diosay.com>
 */
public final class HttpStatus {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Documented
    public @interface HttpStatusDescription {

        public String desc() default "";
    }

    @HttpStatusDescription(desc = "Continue")
    public final static int Continue = 100;
    @HttpStatusDescription(desc = "Switching Protocols")
    public final static int SwitchingProtocols = 101;

    @HttpStatusDescription(desc = "OK")
    public final static int OK = 200;
    @HttpStatusDescription(desc = "Created")
    public final static int Created = 201;
    @HttpStatusDescription(desc = "Accepted")
    public final static int Accepted = 202;
    @HttpStatusDescription(desc = "Non-Authoritative Information")
    public final static int NonAuthoritativeInformation = 203;
    @HttpStatusDescription(desc = "No Content")
    public final static int NoContent = 204;
    @HttpStatusDescription(desc = "Reset Content")
    public final static int ResetContent = 205;
    @HttpStatusDescription(desc = "Partial Content")
    public final static int PartialContent = 206;

    @HttpStatusDescription(desc = "Multiple Choices")
    public final static int MultipleChoices = 300;
    @HttpStatusDescription(desc = "Moved Permanently")
    public final static int MovedPermanently = 301;
    @HttpStatusDescription(desc = "Found")
    public final static int Found = 302;
    @HttpStatusDescription(desc = "See Other")
    public final static int SeeOther = 303;
    @HttpStatusDescription(desc = "Not Modified")
    public final static int NotModified = 304;
    @HttpStatusDescription(desc = "Use Proxy")
    public final static int UseProxy = 305;
    @HttpStatusDescription(desc = "Temporary Redirect")
    public final static int TemporaryRedirect = 307;

    @HttpStatusDescription(desc = "Bad Request")
    public final static int BadRequest = 400;
    @HttpStatusDescription(desc = "Unauthorized")
    public final static int Unauthorized = 401;
    @HttpStatusDescription(desc = "Forbidden")
    public final static int Forbidden = 403;
    @HttpStatusDescription(desc = "Not Found")
    public final static int NotFound = 404;
    @HttpStatusDescription(desc = "Method Not Allowed")
    public final static int MethodNotAllowed = 405;
    @HttpStatusDescription(desc = "Not Acceptable")
    public final static int NotAcceptable = 406;
    @HttpStatusDescription(desc = "Proxy Authentication Required")
    public final static int ProxyAuthenticationRequired = 407;
    @HttpStatusDescription(desc = "Request Timeout")
    public final static int RequestTimeout = 408;
    @HttpStatusDescription(desc = "Conflict")
    public final static int Conflict = 409;
    @HttpStatusDescription(desc = "Gone")
    public final static int Gone = 410;
    @HttpStatusDescription(desc = "Length Required")
    public final static int LengthRequired = 411;
    @HttpStatusDescription(desc = "Precondition Failed")
    public final static int PreconditionFailed = 412;
    @HttpStatusDescription(desc = "Request Entity Too Large")
    public final static int RequestEntityTooLarge = 413;
    @HttpStatusDescription(desc = "Request URI Too Long")
    public final static int RequestURITooLong = 414;
    @HttpStatusDescription(desc = "Requested Range Not Satisfiable")
    public final static int RequestedRangeNotSatisfiable = 416;

    @HttpStatusDescription(desc = "Internal Server Error")
    public final static int InternalServerError = 500;

    @HttpStatusDescription(desc = "Not Implemented")
    public final static int NotImplemented = 501;
    @HttpStatusDescription(desc = "Bad Gateway")
    public final static int BadGateway = 502;
    @HttpStatusDescription(desc = "Service Unavailable")
    public final static int ServiceUnavailable = 503;
    @HttpStatusDescription(desc = "Gateway Timeout")
    public final static int GatewayTimeout = 504;
    @HttpStatusDescription(desc = "HTTP Version Not Supported")
    public final static int HTTPVersionNotSupported = 505;

    public final static String getKnowDescription(int status) throws NullPointerException {
        for (Field m : HttpStatus.class.getFields()) {

            try {
                if (m.getInt(null) == status) {
                    HttpStatusDescription anno = m.getAnnotation(HttpStatusDescription.class);
                    if (anno != null) {
                        return anno.desc();
                    }
                }
            } catch (Exception e) {
            }
        }

        throw new NullPointerException("状态 " + status + " 未定义");
    }
}
