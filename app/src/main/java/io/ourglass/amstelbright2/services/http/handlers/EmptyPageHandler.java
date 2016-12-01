package io.ourglass.amstelbright2.services.http.handlers;

/**
 * Created by mkahn on 11/22/16.
 */

public class EmptyPageHandler extends TemplatePageHandler {


    @Override
    public String getTemplate() {
        return htmlHeader+"<h3>System Info</h3>"+htmlFooter;
    }

}
