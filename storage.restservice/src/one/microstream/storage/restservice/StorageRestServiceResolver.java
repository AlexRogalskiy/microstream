package one.microstream.storage.restservice;

import java.util.Iterator;
import java.util.ServiceLoader;

import one.microstream.storage.restadapter.StorageRestAdapter;
import one.microstream.storage.types.EmbeddedStorageManager;

/**
 * Service loader for {@link StorageRestService}s
 *
 */
public final class StorageRestServiceResolver
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Get the first found implementation of the StorageRestService interface
	 *
	 * @param storage storage to initialize the service with
	 * @return StorageRestService instance
	 */
	public static StorageRestService resolve(final EmbeddedStorageManager storage)
	{
		final StorageRestServiceProvider provider = resolveProvider();
		if(provider != null)
		{
			return provider.provideService(
				StorageRestAdapter.New(storage)
			);
		}
		
		throw new StorageRestServiceNotFoundException("No StorageRestServer implementation found");
	}

	/**
	 * Get the first found implementation of the StorageRestService interface
	 *
	 * @param storageRestAdapter rest adapter to initialize the service with
	 * @return StorageRestService instance
	 */
	public static StorageRestService resolve(final StorageRestAdapter storageRestAdapter)
	{
		final StorageRestServiceProvider provider = resolveProvider();
		if(provider != null)
		{
			return provider.provideService(storageRestAdapter);
		}
		
		throw new StorageRestServiceNotFoundException("No StorageRestServer implementation found");
	}
	
	public static StorageRestServiceProvider resolveProvider()
	{
		final ServiceLoader<StorageRestServiceProvider> serviceLoader = 
			ServiceLoader.load(StorageRestServiceProvider.class);
		final Iterator<StorageRestServiceProvider> iterator = serviceLoader.iterator();
		return iterator.hasNext()
			? iterator.next()
			: null
		;	
	}


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private StorageRestServiceResolver()
	{
		throw new Error();
	}
}