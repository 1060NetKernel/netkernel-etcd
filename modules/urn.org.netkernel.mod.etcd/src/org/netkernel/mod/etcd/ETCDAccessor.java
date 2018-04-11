package org.netkernel.mod.etcd;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.nkf.NKFException;
import org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.ClientBuilder;
import com.coreos.jetcd.api.KeyValue;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.DeleteResponse;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.options.GetOption;

public class ETCDAccessor extends StandardAccessorImpl
{
	public ETCDAccessor()
	{	this.declareThreadSafe();
	}
	
	private static Client getClient(INKFRequestContext aContext) throws Exception
	{
		String endpoints="http://127.0.0.1:2379";
		Client c= Client.builder().endpoints(endpoints).build();
		return c;
	}
	
	public void onSource(INKFRequestContext aContext) throws Exception
	{
		String path=aContext.getThisRequest().getArgumentValue("path");
		
		Client c=getClient(aContext);
		long rev=0L;
		
		GetResponse getResponse = c.getKVClient().get(
		        ByteSequence.fromString(path),
		        GetOption.newBuilder().withRevision(rev).build()
		    ).get();
		
		
		 if (getResponse.getKvs().isEmpty())
		 {
		      throw new NKFException("key not found");
		 }
		 else
		 {	String kv=getResponse.getKvs().get(0).getValue().toStringUtf8();
		 	aContext.createResponseFrom(kv).setExpiry(INKFResponse.EXPIRY_ALWAYS);
		 }
		
		
	}

	public void onSink(INKFRequestContext aContext) throws Exception
	{
		String path=aContext.getThisRequest().getArgumentValue("path");
		String primary =aContext.sourcePrimary(String.class);
		
		Client c=getClient(aContext);
		
		PutResponse pr=c.getKVClient().put(
		        ByteSequence.fromString(path),
		        ByteSequence.fromString(primary)
		    ).join();
		
	}
	
	public void onDelete(INKFRequestContext aContext) throws Exception
	{
		String path=aContext.getThisRequest().getArgumentValue("path");
		
		Client c=getClient(aContext);
		
		DeleteResponse pr=c.getKVClient().delete(
		        ByteSequence.fromString(path)
		    ).join();
		long deleted=pr.getDeleted();
		boolean result=deleted>0;
		aContext.createResponseFrom(result).setExpiry(INKFResponse.EXPIRY_ALWAYS);

	}
	
	
	public void onExists(INKFRequestContext aContext) throws Exception
	{
		String path=aContext.getThisRequest().getArgumentValue("path");
		
		Client c=getClient(aContext);
		long rev=0L;
		
		GetResponse getResponse = c.getKVClient().get(
		        ByteSequence.fromString(path),
		        GetOption.newBuilder().withRevision(rev).build()
		    ).get();
		
		
		boolean result=!getResponse.getKvs().isEmpty();
		aContext.createResponseFrom(result).setExpiry(INKFResponse.EXPIRY_ALWAYS);

	}
}
