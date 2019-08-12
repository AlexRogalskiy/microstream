package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerAbstractType<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryHandlerAbstractType<T> New(final Class<T> type)
	{
		return new BinaryHandlerAbstractType<>(
			notNull(type)
		);
	}
	
	public static <T> BinaryHandlerAbstractType<T> New(
		final Class<T> type,
		final String   typeName
	)
	{
		return new BinaryHandlerAbstractType<>(
			notNull(type)    ,
			notNull(typeName)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerAbstractType(final Class<T> type)
	{
		super(type);
	}
	
	BinaryHandlerAbstractType(final Class<T> type, final String typeName)
	{
		super(type, typeName);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final T                       instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final T create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final void update(final Binary bytes, final T instance, final PersistenceObjectIdResolver idResolver)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}
	
	@Override
	public final void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}
	
	@Override
	public final boolean isSpecificInstanceViable()
	{
		return false;
	}

}