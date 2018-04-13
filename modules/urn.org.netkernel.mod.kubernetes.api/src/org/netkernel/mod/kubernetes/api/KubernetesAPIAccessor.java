package org.netkernel.mod.kubernetes.api;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;

public class KubernetesAPIAccessor extends StandardAccessorImpl
{
	public KubernetesAPIAccessor()
	{	super.declareThreadSafe();
	}
	
	public void onSource(INKFRequestContext aContext) throws Exception
	{
		ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        IHDSMutator m=HDSFactory.newDocument();
        m.pushNode("pods");
        CoreV1Api api = new CoreV1Api();
        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
        for (V1Pod item : list.getItems()) {
            //System.out.println(item.getMetadata().getName());
        		m.addNode("pod", item.getMetadata().getName());
        }
        
        INKFResponse resp=aContext.createResponseFrom(m.toDocument(false));
        resp.setExpiry(INKFResponse.EXPIRY_ALWAYS);
	}
}
