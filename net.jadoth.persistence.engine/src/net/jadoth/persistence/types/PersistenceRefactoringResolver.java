package net.jadoth.persistence.types;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.collections.types.XImmutableEnum;
import net.jadoth.collections.types.XImmutableTable;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionResolveTypeName;
import net.jadoth.reflect.XReflect;
import net.jadoth.typing.KeyValue;

/**
 * A mapping that projects outdated identifiers (usually className#fieldName, but in case of root instances
 * also potentially arbitrary strings) to current identifiers.
 * 
 * @author TM
 *
 */
public interface PersistenceRefactoringResolver extends PersistenceTypeResolver
{
	/**
	 * Returns a key-value pair with the passed source identifier as the key and a mapped target identifier
	 * as the value. The value can be potentially null to indicate deletion.
	 * If the lookup did not yield any result, <code>null</code> is returned.
	 * 
	 * @param sourceIdentifier
	 * @return
	 */
	public KeyValue<String, String> lookup(String sourceIdentifier);
	
	/**
	 * Returns a key-value pair with the passed source member as the key and a mapped target member
	 * as the value. The value can be potentially null to indicate deletion.
	 * If the lookup did not yield any result, <code>null</code> is returned.
	 * 
	 * @param sourceType
	 * @param sourceMember
	 * @param targetType
	 * @return
	 */
	public KeyValue<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolveMember(
		PersistenceTypeDefinition<?>     sourceType  ,
		PersistenceTypeDescriptionMember sourceMember,
		PersistenceTypeDefinition<?>     targetType
	);
	

		
	public static PersistenceRefactoringResolver New(
		final XGettingTable<String, String>                                         entries                       ,
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
	)
	{
		return new Implementation(
			entries                       .immure(),
			sourceTypeIdentifierBuilders  .immure(),
			sourceMemberIdentifierBuilders.immure(),
			targetMemberIdentifierBuilders.immure()
		);
	}
	
	public final class Implementation implements PersistenceRefactoringResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XImmutableTable<String, String>                                         entries                       ;
		final XImmutableEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ;
		final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders;
		final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final XImmutableTable<String, String>                                         entries                       ,
			final XImmutableEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
			final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
			final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
		)
		{
			super();
			this.entries                        = entries                       ;
			this.sourceTypeIdentifierBuilders   = sourceTypeIdentifierBuilders  ;
			this.sourceMemberIdentifierBuilders = sourceMemberIdentifierBuilders;
			this.targetMemberIdentifierBuilders = targetMemberIdentifierBuilders;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private String ensureActualTypeString(final PersistenceTypeDescription typeDescription)
		{
			// search for a mapping entry with identifier builders in descending order of priority.
			final XImmutableTable<String, String> entries = this.entries;
			for(final PersistenceRefactoringTypeIdentifierBuilder idBuilder : this.sourceTypeIdentifierBuilders)
			{
				final String                   identifier = idBuilder.buildTypeIdentifier(typeDescription);
				final KeyValue<String, String> entry      = entries.lookup(identifier);
				if(entry == null)
				{
					continue;
				}
				
				// value might be null to indicate deletion
				return entry.value();
			}
			
			// if no refacting entry could be found, the original type name still applies.
			return typeDescription.typeName();
		}
		
		@Override
		public final Class<?> resolveType(final PersistenceTypeDescription typeDescription)
		{
			final String actualTypeString = this.ensureActualTypeString(typeDescription);
			if(actualTypeString == null)
			{
				// special case: mapped to null to indicate deletion, so return null.
				return null;
			}
			
			// every non-null type string MUST be resolvable or something is irreparably wrong (e.g. old mappings)
			try
			{
				return XReflect.classForName(actualTypeString);
			}
			catch(final ClassNotFoundException e)
			{
				throw new PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(actualTypeString, e);
			}
		}

		@Override
		public final KeyValue<String, String> lookup(final String sourceIdentifier)
		{
			return this.entries.lookup(sourceIdentifier);
		}

		@Override
		public KeyValue<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolveMember(
			final PersistenceTypeDefinition<?>     sourceType  ,
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDefinition<?>     targetType
		)
		{
			// search for a mapping entry with identifier builders in descending order of priority.
			final XImmutableTable<String, String> entries = this.entries;
			for(final PersistenceRefactoringMemberIdentifierBuilder idBuilder : this.sourceMemberIdentifierBuilders)
			{
				final String                   identifier = idBuilder.buildMemberIdentifier(sourceType, sourceMember);
				final KeyValue<String, String> entry      = entries.lookup(identifier);
				if(entry == null)
				{
					continue;
				}
				
				return this.resolveTarget(sourceType, sourceMember, targetType, entry.value());
			}

			// no refacting entry could be found
			return null;
		}
		
		private KeyValue<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolveTarget(
			final PersistenceTypeDefinition<?>     sourceType            ,
			final PersistenceTypeDescriptionMember sourceMember          ,
			final PersistenceTypeDefinition<?>     targetType            ,
			final String                           targetMemberIdentifier
		)
		{
			if(targetMemberIdentifier == null)
			{
				// indicated deletion
				return X.KeyValue(sourceMember, null);
			}
			
			for(final PersistenceTypeDescriptionMember targetMember : targetType.members())
			{
				for(final PersistenceRefactoringMemberIdentifierBuilder idBuilder : this.targetMemberIdentifierBuilders)
				{
					final String identifier = idBuilder.buildMemberIdentifier(targetType, targetMember);
					if(identifier.equals(targetMemberIdentifier))
					{
						return X.KeyValue(sourceMember, targetMember);
					}
				}
			}
			
			// if a target member mapping was found but cannot be resolved, something is wrong.
			// (05.10.2018 TM)EXCP: proper exception
			throw new RuntimeException(
				"Unresolvable type member refactoring mapping: "
				+ sourceType.toTypeIdentifier() + '#' + sourceMember.uniqueName()
				+ " -> \"" + targetMemberIdentifier + "\" in type "
				+ targetType.toRuntimeTypeIdentifier()
			);
		}
				
	}
	
}
