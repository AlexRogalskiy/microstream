package net.jadoth.storage.types;

import static net.jadoth.Jadoth.notNull;

import java.io.File;
import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.persistence.types.PersistenceRoots;
import net.jadoth.persistence.types.Storer;

public interface EmbeddedStorageManager extends StorageController, StorageConnection
{
	public StorageTypeDictionary typeDictionary();

	public StorageConnection createConnection();

	public StorageConfiguration configuration();

	public void initialize();

	@Override
	public default EmbeddedStorageManager start()
	{
		return this.start(null, null);
	}

	@Override
	public default EmbeddedStorageManager start(
		final StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
		final StorageTypeDictionary       oldTypes
	)
	{
		return this.start(entityInitializingCacheEvaluator, oldTypes, null);
	}

	public EmbeddedStorageManager start(
		StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
		StorageTypeDictionary       oldTypes                        ,
		XGettingEnum<File>          initialImportFiles
	);

	@Override
	public boolean shutdown();

	public void truncateData();



	public final class Implementation implements EmbeddedStorageManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageConfiguration                configuration    ;
		private final StorageManager                      storageManager   ;
		private final EmbeddedStorageConnectionFoundation connectionFactory;
		private final PersistenceRoots                    definedRoots     ;

		private StorageConnection singletonConnection;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final StorageConfiguration                configuration    ,
			final EmbeddedStorageConnectionFoundation connectionFactory,
			final PersistenceRoots                    definedRoots
		)
		{
			super();
			this.configuration     = notNull(configuration);
			this.storageManager    = notNull(connectionFactory.getStorageManager()); // to ensure consistency
			this.connectionFactory = notNull(connectionFactory);
			this.definedRoots      = notNull(definedRoots);
		}


		private synchronized StorageConnection singletonConnection()
		{
			if(this.singletonConnection == null)
			{
				this.singletonConnection = this.createConnection();
			}
			return this.singletonConnection;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public PersistenceManager<Binary> persistenceManager()
		{
			return this.singletonConnection().persistenceManager();
		}

		@Override
		public Storer createStorer()
		{
			return this.singletonConnection().createStorer();
		}

		@Override
		public final EmbeddedStorageManager.Implementation start(
			final StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
			final StorageTypeDictionary       oldTypes                        ,
			final XGettingEnum<File>          initialImportFiles
		)
		{
			this.storageManager.start(entityInitializingCacheEvaluator, oldTypes);

			// special initial import for refactoring purposes: after validation but before roots loading.
			if(initialImportFiles != null && !initialImportFiles.isEmpty())
			{
				this.initialImport(initialImportFiles);
			}

			this.initialize();
			return this;
		}

		private void initialImport(final XGettingEnum<File> initialImportFiles)
		{
			this.connectionFactory.createStorageConnection().importFiles(initialImportFiles);
		}

		private boolean synchronizeRoots(final PersistenceRoots loadedRoots)
		{
			final XGettingTable<String, Object> loadedEntries  = loadedRoots.entries();
			final XGettingTable<String, Object> definedEntries = this.definedRoots.entries();

			final boolean equalContent = loadedEntries.equalsContent(definedEntries, (e1, e2) ->
				// keys (identifier Strings) must be value-equal, root instance must be the same (identical)
				e1.key().equals(e2.key()) && e1.value() == e2.value()
			);
			
			/* (09.04.2018 TM)TODO: OGS-41
			 * The following cases have to be covered:
			 * 1.) Loaded and defined roots are perfectly equal. -> return true (roots are synchronous)
			 * 2.) Loaded and defined roots are equal, but the loaded roots have changes. -> return false (roots must be stored)
			 * 3.) L&D roots are not equal, but the defined contains all the loaded and more. -> add new instances, return false (roots must be stored)
			 * 4.) L&D roots are not equal and some instances of loaded are missing in defined. -> throw exception
			 * 
			 * Or is really it an exception?
			 * What about changed order of otherwise matching entries?
			 * See design notes.
			 */
			if(!equalContent)
			{
				// (14.09.2015 TM)EXCP: proper exception
				throw new RuntimeException("Mismatch of loaded roots and defined roots");
			}
			
			// if both have equal content, no updates have to be made.
//			if()
//			{
//				// if loadedRoots has changes, it must be stored nonetheless
//				if(loadedRoots.hasChanged())
//				{
//					return false;
//				}
//
//				return true;
//			}

			/*
			 * To ensure removal of old, addition of new and same order, it is best to simply clear and add all.
			 * Important is: the loaded entries instance has to be updated as the loadedRoots instance has to be
			 * the one that gets saved to maintain the associated ObjectId.
			 */
//			loadedEntries.clear();
//			loadedEntries.addAll(definedEntries);
			return false;
		}

		@Override
		public final void initialize()
		{
			final StorageConnection initConnection = this.createConnection();

			/* (22.09.2014 TM)NOTE: Constants OID consistency conflict
			 * If more than one roots instance exists, all are loaded and all are built,
			 * even if afterwards only the first one is considered.
			 * Of course for multiple roots instances, different OIDs can point to the
			 * same (<- !) runtime constant instance, hence causing a consistency error on validation in the registry.
			 * Solution:
			 * Storage may only send at most one roots instance. Must be strictly ensured:
			 * - independently from roots GC
			 * - GC must collect all but one roots instance (already does but not testet yet)
			 */
			PersistenceRoots loadedRoots = initConnection.persistenceManager().createLoader().loadRoots();
			if(loadedRoots == null)
			{
				// no loaded roots is only valid if there is no data yet at all (no database files / content)
				final StorageRawFileStatistics statistics = initConnection.createStorageStatistics();
				if(statistics.liveDataLength() != 0)
				{
					// (14.09.2015 TM)EXCP: proper exception
					throw new RuntimeException("No roots found for existing data.");
				}

				loadedRoots = this.definedRoots;
			}
			else if(this.synchronizeRoots(loadedRoots))
			{
				// (09.04.2018 TM)FIXME: refactor logic to cover changed roots correctly.
				return; // loaded roots are equal to defined roots, no store update required
			}

			// either the loaded roots instance shall be updated or the defined roots have to be stored initially
			initConnection.storeFull(loadedRoots); // (05.11.2013 TM)TODO: really always deep? Not on demand?
		}

		@Override
		public final boolean shutdown()
		{
			return this.storageManager.shutdown();
		}

		@Override
		public void truncateData()
		{
			this.storageManager.truncateData();
		}

		@Override
		public final boolean isAcceptingTasks()
		{
			return this.storageManager.isAcceptingTasks();
		}

		@Override
		public final boolean isRunning()
		{
			return this.storageManager.isRunning();
		}

		@Override
		public final boolean isStartingUp()
		{
			return this.storageManager.isStartingUp();
		}

		@Override
		public final boolean isShuttingDown()
		{
			return this.storageManager.isShuttingDown();
		}

		@Override
		public final boolean isShutdown()
		{
			return this.storageManager.isShutdown();
		}

		@Override
		public final void checkAcceptingTasks()
		{
			this.storageManager.checkAcceptingTasks();
		}

		@Override
		public final StorageConfiguration configuration()
		{
			return this.configuration;
		}

		@Override
		public final StorageTypeDictionary typeDictionary()
		{
			return this.storageManager.typeDictionary();
		}

		@Override
		public final StorageConnection createConnection()
		{
			return this.connectionFactory.createStorageConnection();
		}

		@Override
		public void issueFullGarbageCollection()
		{
			this.singletonConnection().issueFullGarbageCollection();
		}

		@Override
		public boolean issueGarbageCollection(final long nanoTimeBudget)
		{
			return this.singletonConnection().issueGarbageCollection(nanoTimeBudget);
		}

		@Override
		public void issueFullFileCheck()
		{
			this.singletonConnection().issueFullFileCheck();
		}

		@Override
		public void issueFullFileCheck(final StorageDataFileDissolvingEvaluator fileDissolvingEvaluator)
		{
			this.singletonConnection().issueFullFileCheck(fileDissolvingEvaluator);
		}

		@Override
		public boolean issueFileCheck(final long nanoTimeBudgetBound)
		{
			return this.singletonConnection().issueFileCheck(nanoTimeBudgetBound);
		}

		@Override
		public boolean issueFileCheck(
			final long nanoTimeBudgetBound,
			final StorageDataFileDissolvingEvaluator fileDissolvingEvaluator)
		{
			return this.singletonConnection().issueFileCheck(nanoTimeBudgetBound, fileDissolvingEvaluator);
		}

		@Override
		public void issueFullCacheCheck()
		{
			this.singletonConnection().issueFullCacheCheck();
		}

		@Override
		public void issueFullCacheCheck(final StorageEntityCacheEvaluator entityEvaluator)
		{
			this.singletonConnection().issueFullCacheCheck(entityEvaluator);
		}

		@Override
		public boolean issueCacheCheck(final long nanoTimeBudgetBound)
		{
			return this.singletonConnection().issueCacheCheck(nanoTimeBudgetBound);
		}

		@Override
		public boolean issueCacheCheck(
			final long                        nanoTimeBudgetBound,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			return this.singletonConnection().issueCacheCheck(nanoTimeBudgetBound, entityEvaluator);
		}

		@Override
		public StorageRawFileStatistics createStorageStatistics()
		{
			return this.singletonConnection().createStorageStatistics();
		}

		@Override
		public void exportChannels(final StorageIoHandler fileHandler, final boolean performGarbageCollection)
		{
			this.singletonConnection().exportChannels(fileHandler, performGarbageCollection);
		}

		@Override
		public StorageEntityTypeExportStatistics exportTypes(
			final StorageEntityTypeExportFileProvider            exportFileProvider,
			final Predicate<? super StorageEntityTypeHandler<?>> isExportType
		)
		{
			return this.singletonConnection().exportTypes(exportFileProvider, isExportType);
		}

		@Override
		public void importFiles(final XGettingEnum<File> importFiles)
		{
			this.singletonConnection().importFiles(importFiles);
		}


	}

}
