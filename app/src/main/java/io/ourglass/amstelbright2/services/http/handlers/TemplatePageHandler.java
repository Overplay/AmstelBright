package io.ourglass.amstelbright2.services.http.handlers;

import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright2.services.http.OGRouterNanoHTTPD;

/**
 * Created by mkahn on 11/22/16.
 */

public abstract class TemplatePageHandler extends OGRouterNanoHTTPD.DefaultHandler{

    String htmlHeader = "<!DOCTYPE html><html lang=\"en\"><head> <meta charset=\"UTF-8\"> <style>html, body{background-color: black; color: white; margin: 0; padding: 10px; font-family: Arial;}</style> <title>Ourglass</title> <img style=\"width: 100px;\" src=\"data:image/png;base64,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\"/> <h3>Welcome to Ourglass</h3></head><body>";
    String htmlFooter = "<hr><p style=\"font-size: 0.5rem;\">&copy;2017 Ourglass Inc.</body></html>";

    public abstract String getTemplate();

    public String getText() {
        return getTemplate();
    }

    @Override
    public String getMimeType() {
        return "text/html";
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }

}
