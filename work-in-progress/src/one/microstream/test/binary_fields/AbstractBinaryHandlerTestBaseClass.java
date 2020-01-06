package one.microstream.test.binary_fields;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.internal.BinaryField;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;

public abstract class AbstractBinaryHandlerTestBaseClass<T extends TestBaseClass> extends AbstractBinaryHandlerCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final BinaryField<TestBaseClass>
		baseValue_long   = Field(e -> (int)e.baseValue_long, (e, v) -> e.baseValue_long = v),
		baseValue_double = Field(double.class),
		baseReference    = Field(String.class)
	//	cmplx   = FieldBytes(),
//		cmplx   = FieldComplex(
//			Field(String.class, "key"),
//			Field(String.class, "value")
//		)
	;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractBinaryHandlerTestBaseClass(final Class<T> type)
	{
		super(type);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public boolean hasInstanceReferences()
	{
		// FIXME PersistenceTypeHandler<Binary,T>#hasInstanceReferences()
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		// FIXME PersistenceTypeHandler<Binary,T>#iterateLoadableReferences()
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// FIXME PersistenceTypeHandler<Binary,T>#updateState()
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean hasPersistedReferences()
	{
		// FIXME PersistenceTypeDefinition#hasPersistedReferences()
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		// FIXME PersistenceTypeDefinition#hasVaryingPersistedLengthInstances()
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public void store(final Binary data, final T instance, final long objectId, final PersistenceStoreHandler handler)
	{
		this.baseValue_long  .store_long    (data, instance.baseValue_long  );
		this.baseValue_double.store_double  (data, instance.baseValue_double);
		this.baseReference   .storeReference(data, instance.baseReference, handler);
	}
	
}
