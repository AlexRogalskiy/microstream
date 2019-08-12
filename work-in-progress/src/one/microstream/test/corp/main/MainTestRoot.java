package one.microstream.test.corp.main;

import one.microstream.collections.Singleton;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;
import one.microstream.test.corp.model.ClientCorporation;


public class MainTestRoot
{
	static final Singleton<ClientCorporation> ROOT = new Singleton<>(null);
	
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start(ROOT);

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(ROOT.get() == null)
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			ROOT.set(Test.generateModelData(1_000));
			
			Test.print("Storing ...");
			STORAGE.storeRoot();
			Test.print("Storing completed.");
		}
		else
		{
			// subsequent executions enter here (database reuse)
			
			System.out.println("is main root: " + (STORAGE.root() == ROOT));

			Test.printInitializationTime(STORAGE);
			Test.printOperationModeTime(STORAGE);
			Test.print("Model data loaded.");
			Test.print("Root instance: " + ROOT.get());
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
}