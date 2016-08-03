package io.ourglass.amstelbright.services.ssdp;

import android.util.Log;
import android.widget.Toast;

import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.model.message.header.UserAgentHeader;
import org.fourthline.cling.model.profile.RemoteClientInfo;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import java.net.InetAddress;

import io.ourglass.amstelbright.services.amstelbright.AmstelBrightService;

/**
 * Created by ethan on 8/2/16.
 */
@UpnpService(
        serviceId = @UpnpServiceId("Registration"),
        serviceType = @UpnpServiceType(value = "Registration", version = 1)
)
public class Registration {

    private List<String> registeredIpAddresses;

    private final PropertyChangeSupport propertyChangeSupport;

    public Registration(){
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        registeredIpAddresses = new ArrayList<String>();
    }

    public PropertyChangeSupport getPropertyChangeSupport(){
        return propertyChangeSupport;
    }

    @UpnpAction
    public void registerDevice(RemoteClientInfo clientInfo){
        if(clientInfo != null) {
            InetAddress clientAddress = clientInfo.getRemoteAddress();
            Log.v("Registration", "recieved registration attempt from " + clientAddress.toString());

            if (registeredIpAddresses.contains(clientAddress.toString())) {
                Log.w("Registration", "Device is already registered");
                clientInfo.getExtraResponseHeaders().add("BAd", "BADDD");
                return;
            }
            //Toast.makeText(AmstelBrightService.context, clientAddress.toString() + " successfully registered", Toast.LENGTH_SHORT).show();
            Log.v("Registration", "device successfully registered");
            registeredIpAddresses.add(clientAddress.toString());
            clientInfo.getExtraResponseHeaders().add("Auth-token", "ABCDEFG");

        }
        else {
            Log.e("Registration", "no client info recieved, aborting");
        }
    }
}
