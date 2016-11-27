package io.ourglass.amstelbright2.services.http.handlers;

import org.json.JSONObject;

import io.ourglass.amstelbright2.core.OGSystem;

/**
 * Created by mkahn on 11/22/16.
 */

public class MainInfoPageHandler extends TemplatePageHandler {


    @Override
    public String getTemplate() {

        JSONObject sysinfo = OGSystem.getSystemInfo();
        String label = "<p style=\"margin-top: 0px;\"><span style=\"color: #ccff99; font-weight: bold;\">";
        String sysinfoHtml = "<div style=\"margin: 20px; border: 1px solid white; border-radius: 3px;\">";
        sysinfoHtml += label+"Name:</span>&nbsp"+sysinfo.optString("name")+"</p>";
        sysinfoHtml += label+"Location:</span>&nbsp"+sysinfo.optString("locationWithinVenue")+"</p>";
        sysinfoHtml += label+"Paired:</span>&nbsp"+(sysinfo.optBoolean("isPairedToSTB")?"Yes":"No")+"</p>";
        sysinfoHtml += label+"AB Version:</span>&nbsp"+sysinfo.optString("abVersionName")+"</p>";
        sysinfoHtml += label+"Resolution:</span>&nbsp"+sysinfo.optString("outputRes")+"</p>";
        sysinfoHtml += label+"VID:</span>&nbsp"+sysinfo.optString("venueId")+"</p>";
        sysinfoHtml += "</div>";

        return htmlHeader+"<h3>System Info</h3>"+sysinfoHtml+htmlFooter;
    }

}
