/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.java.sip.communicator.impl.argdelegation;

import net.java.sip.communicator.launchutils.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.util.*;

import net.java.sip.communicator.util.osgi.ServiceUtils;
import org.apache.commons.lang3.SystemUtils;
import org.osgi.framework.*;

import com.apple.eawt.*;

/**
 * Activates the <tt>ArgDelegationService</tt> and registers a URI delegation
 * peer with the util package arg manager so that we would be notified when the
 * application receives uri arguments.
 *
 * @author Emil Ivov
 */
public class ArgDelegationActivator
    extends AbstractServiceDependentActivator<UIService>
{
    /**
     * A reference to the bundle context that is currently in use.
     */
    private static BundleContext bundleContext = null;

    /**
     * A reference to the delegation peer implementation that is currently
     * handling uri arguments.
     */
    private ArgDelegationPeerImpl delegationPeer = null;

    /**
     * A reference to the <tt>UIService</tt> currently in use in
     * SIP Communicator.
     */
    private static UIService uiService = null;

    /**
     * Starts the arg delegation bundle and registers the delegationPeer with
     * the util package URI manager.
     *
     * @param dependentService the service this activator is waiting.
     * @throws Exception if starting the arg delegation bundle and registering
     * the delegationPeer with the util package URI manager fails
     */
    public void start(UIService dependentService)
    {
        delegationPeer = new ArgDelegationPeerImpl(bundleContext);
        bundleContext.addServiceListener(delegationPeer);

        //register our instance of delegation peer.
        LaunchArgHandler.getInstance().setDelegationPeer(delegationPeer);

        if(SystemUtils.IS_OS_MAC)
        {
            Application application = Application.getApplication();

            if(application != null)
            {
                try
                {
                    application.setOpenURIHandler(evt ->
                        delegationPeer.handleUri(evt.getURI().toString()));
                }
                catch (Exception ex)
                {
                    // ignore
                }
            }
        }
    }

    /**
     * The dependent class. We are waiting for the ui service.
     * @return the ui service class.
     */
    @Override
    public Class<UIService> getDependentServiceClass()
    {
        return UIService.class;
    }

    /**
     * Sets the bundle context to use.
     * @param context a reference to the currently active bundle context.
     */
    @Override
    public void setBundleContext(BundleContext context)
    {
        bundleContext = context;
    }

    /**
     * Unsets the delegation peer instance that we set when we start this
     * bundle.
     *
     * @param bc an instance of the currently valid bundle context.
     * @throws Exception if unsetting the delegation peer instance that we set
     * when we start this bundle fails
     */
    public void stop(BundleContext bc) throws Exception
    {
        uiService = null;
        if (delegationPeer != null)
        {
            bc.removeServiceListener(delegationPeer);
            delegationPeer = null;
        }

        LaunchArgHandler.getInstance().setDelegationPeer(null);
    }

    /**
     * Returns a reference to an UIService implementation currently registered
     * in the bundle context or null if no such implementation was found.
     *
     * @return a reference to an UIService implementation currently registered
     * in the bundle context or null if no such implementation was found.
     */
    public static UIService getUIService()
    {
        if(uiService == null)
            uiService = ServiceUtils.getService(bundleContext, UIService.class);
        return uiService;
    }
}
