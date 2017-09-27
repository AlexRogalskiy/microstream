package net.jadoth.storage.types;

import java.util.function.Consumer;

import net.jadoth.collections.HashMapIdObject;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeDescriptionRegistrationCallback;
import net.jadoth.persistence.types.PersistenceTypeDictionary;


public interface StorageTypeDictionary extends PersistenceTypeDictionary, PersistenceTypeDescriptionRegistrationCallback
{
	public <P extends Consumer<? super StorageEntityTypeHandler<?>>> P iterateTypeHandlers(P procedure);

	public StorageEntityTypeHandler<?> lookupTypeHandler(long typeId);

	public void validateEntityTypeId(long typeId);

	public StorageEntityTypeHandler<?> validateEntity(long length, long typeId, long objectId);

	public void validate(PersistenceTypeDictionary typeDictionary);
	
	public <D extends PersistenceTypeDictionary> D initialize(D typeDictionary);



	public final class Implementation implements StorageTypeDictionary
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final HashMapIdObject<StorageEntityTypeHandler<?>> registry   = HashMapIdObject.New();
		private       PersistenceTypeDictionary                    dictionary;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Implementation()
		{
			super();
		}
		


		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final <P extends Consumer<? super StorageEntityTypeHandler<?>>> P iterateTypeHandlers(
			final P procedure
		)
		{
			synchronized(this.registry)
			{
				this.registry.iterateObjects(procedure);
			}
			return procedure;
		}

		@Override
		public final StorageEntityTypeHandler<?> lookupTypeHandler(final long typeId)
		{
			synchronized(this.registry)
			{
				return this.registry.get(typeId);
			}
		}

		@Override
		public final boolean registerType(final PersistenceTypeDefinition<?> typeDescription)
		{
			synchronized(this.registry)
			{
				return this.dictionary.registerType(typeDescription);
			}
		}

		@Override
		public final boolean registerTypes(
			final XGettingCollection<? extends PersistenceTypeDefinition<?>> typeDescriptions
		)
		{
			synchronized(this.registry)
			{
				return this.dictionary.registerTypes(typeDescriptions);
			}
		}

		@Override
		public final void validate(final PersistenceTypeDictionary typeDictionary)
		{
			synchronized(this.registry)
			{
				for(final PersistenceTypeDefinition<?> typeDesc : typeDictionary.allTypes().values())
				{
					if(PersistenceTypeDefinition.isEqualDescription(typeDesc, this.registry.get(typeDesc.typeId())))
					{
						continue;
					}
					throw new RuntimeException(
						"Invalid type description: " + typeDesc.typeId() + " " + typeDesc.typeName()
					);
				}
			}
		}

		@Override
		public final void validateEntityTypeId(final long typeId)
		{
			synchronized(this.registry)
			{
				if(this.registry.get(typeId) == null)
				{
					throw new PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(typeId);
				}
			}
		}

		@Override
		public final StorageEntityTypeHandler<?> validateEntity(
			final long length  ,
			final long typeId  ,
			final long objectId
		)
		{
			synchronized(this.registry)
			{
				final StorageEntityTypeHandler<?> typeHandler = this.lookupTypeHandler(typeId);
				if(typeHandler == null)
				{
					// (05.05.2014)EXCP: proper exception
					throw new RuntimeException(
						"Unknown type id " + typeId + " of entity with oid " + objectId + " and length " + length
					);
				}
				typeHandler.validateEntityGuaranteedType(length, objectId);
				return typeHandler;
			}
		}

		@Override
		public final XGettingTable<Long, PersistenceTypeDefinition<?>> allTypes()
		{
			return this.dictionary.allTypes();
		}
		
		@Override
		public final XGettingTable<String, PersistenceTypeDefinition<?>> currentTypesByName()
		{
			return this.dictionary.currentTypesByName();
		}

		@Override
		public final PersistenceTypeDefinition<?> lookupTypeByName(final String typeName)
		{
			return this.dictionary.lookupTypeByName(typeName);
		}

		@Override
		public final PersistenceTypeDefinition<?> lookupTypeById(final long typeId)
		{
			return this.dictionary.lookupTypeById(typeId);
		}

		@Override
		public final long determineHighestTypeId()
		{
			return this.dictionary.determineHighestTypeId();
		}

		@Override
		public final void setTypeDescriptionRegistrationCallback(
			final PersistenceTypeDescriptionRegistrationCallback callback
		)
		{
			// as a storage type dictionary is a registration callback itself, this method is only valid for this
			if(callback != this)
			{
				// (06.12.2014)EXCP: proper exception
				throw new RuntimeException(
					"Inconsistent " + PersistenceTypeDescriptionRegistrationCallback.class.getSimpleName()
				);
			}
		}

		@Override
		public final PersistenceTypeDescriptionRegistrationCallback getTypeDescriptionRegistrationCallback()
		{
			return this;
		}


		@Override
		public <D extends PersistenceTypeDictionary> D initialize(final D typeDictionary)
		{
			synchronized(this.registry)
			{
				if(this.dictionary != null)
				{
					// ensure idempotency just in case some architecture triggers multiple calls
					if(this.dictionary == typeDictionary)
					{
						return typeDictionary;
					}
					// (06.12.2014)EXCP: proper exception
					throw new RuntimeException("type dictionary already initialized.");
				}
				for(final PersistenceTypeDefinition<?> td : typeDictionary.allTypes().values())
				{
					this.registerTypeDescription(td);
				}
				this.dictionary = typeDictionary;
				
				return typeDictionary;
			}
		}

		@Override
		public final void registerTypeDescription(final PersistenceTypeDefinition<?> typeDescription)
		{
			synchronized(this.registry)
			{
				final StorageEntityTypeHandler<?> typeHandler = StorageEntityTypeHandler.New(typeDescription);
				final long                        typeId      = typeHandler.typeId();
				
				if(!this.registry.add(typeId, typeHandler))
				{
					throw new RuntimeException("TypeId is already associated with a typeHandler: " + typeId);
				}
				
				// (13.04.2017 TM)TODO: why is the "ignorant" put() used instead of a validating add()?
				
				// (13.04.2017 TM)NOTE: old version
//				this.registry.put(
//					typeDescription.typeId(),
//					StorageEntityTypeHandler.New(typeDescription)
//				);
			}
		}

	}

}
