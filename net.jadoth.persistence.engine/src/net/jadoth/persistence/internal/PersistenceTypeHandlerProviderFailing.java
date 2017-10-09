package net.jadoth.persistence.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.persistence.types.PersistenceTypeHandlerProvider;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.types.SwizzleTypeLink;

/**
 * Trivial implementation that throws a {@link PersistenceExceptionTypeNotPersistable} for every type.
 * <p>
 * Useful if only pre-registered types shall be handleable and every unhandled type shall be indicated to be
 * unpersistable.
 *
 * @author Thomas Muenz
 *
 * @param <M>
 */
public class PersistenceTypeHandlerProviderFailing<M> implements PersistenceTypeHandlerProvider<M>
{
	@Override
	public <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		final PersistenceTypeHandlerManager<M> typeHandlerManager,
		final Class<T> type
	)
		throws PersistenceExceptionTypeNotPersistable
	{
		throw new PersistenceExceptionTypeNotPersistable(type);
	}

	@Override
	public <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		final PersistenceTypeHandlerManager<M> typeHandlerManager,
		final long typeId
	)
	{
		throw new PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(typeId);
	}

	@Override
	public long ensureTypeId(final Class<?> type)
	{
		throw new PersistenceExceptionTypeNotPersistable(type);
	}

	@Override
	public <T> Class<T> ensureType(final long typeId)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME SwizzleTypeManager#ensureType
	}

	@Override
	public long currentTypeId()
	{
		/* this is not an API misdesign abuse of this exception (like in the JDK), but instead
		 * this implementation actually does intentionally not support that operation.
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean registerType(final long tid, final Class<?> type) throws SwizzleExceptionConsistency
	{
		throw new PersistenceExceptionTypeNotPersistable(type);
	}

	@Override
	public long lookupTypeId(final Class<?> type)
	{
		throw new PersistenceExceptionTypeNotPersistable(type);
	}

	@Override
	public <T> Class<T> lookupType(final long typeId)
	{
		throw new PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(typeId);
	}
	
	@Override
	public void validateExistingTypeMapping(final long typeId, final Class<?> type)
	{
		/*
		 * this is not an API misdesign abuse of this exception (like in the JDK), but instead
		 * this implementation actually does intentionally not support that operation.
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public void validatePossibleTypeMapping(final long typeId, final Class<?> type)
	{
		/*
		 * this is not an API misdesign abuse of this exception (like in the JDK), but instead
		 * this implementation actually does intentionally not support that operation.
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public void validateExistingTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency
	{
		/*
		 * this is not an API misdesign abuse of this exception (like in the JDK), but instead
		 * this implementation actually does intentionally not support that operation.
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public void validatePossibleTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency
	{
		/*
		 * this is not an API misdesign abuse of this exception (like in the JDK), but instead
		 * this implementation actually does intentionally not support that operation.
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCurrentHighestTypeId(final long highestTypeId)
	{
		/*
		 * this is not an API misdesign abuse of this exception (like in the JDK), but instead
		 * this implementation actually does intentionally not support that operation.
		 */
		throw new UnsupportedOperationException();
	}
	
	@Override
	public long typeCount()
	{
		/*
		 * this is not an API misdesign abuse of this exception (like in the JDK), but instead
		 * this implementation actually does intentionally not support that operation.
		 */
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(final Class<T> type) throws PersistenceExceptionTypeNotPersistable
	{
		/*
		 * this is not an API misdesign abuse of this exception (like in the JDK), but instead
		 * this implementation actually does intentionally not support that operation.
		 */
		throw new UnsupportedOperationException();
	}

}
